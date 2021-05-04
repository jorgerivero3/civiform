package repository;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import com.google.common.collect.ImmutableList;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.inject.Provider;
import models.Account;
import models.Applicant;
import models.Application;
import models.LifecycleStage;
import models.Program;
import models.TrustedIntermediaryGroup;
import play.db.ebean.EbeanConfig;
import services.program.ProgramDefinition;
import services.ti.NoSuchTrustedIntermediaryError;
import services.ti.NoSuchTrustedIntermediaryGroupError;

public class UserRepository {

  private final EbeanServer ebeanServer;
  private final DatabaseExecutionContext executionContext;
  private final Provider<VersionRepository> versionRepositoryProvider;

  @Inject
  public UserRepository(
      EbeanConfig ebeanConfig,
      DatabaseExecutionContext executionContext,
      Provider<VersionRepository> versionRepositoryProvider) {
    this.ebeanServer = Ebean.getServer(checkNotNull(ebeanConfig).defaultServer());
    this.executionContext = checkNotNull(executionContext);
    this.versionRepositoryProvider = checkNotNull(versionRepositoryProvider);
  }

  public CompletionStage<Set<Applicant>> listApplicants() {
    return supplyAsync(() -> ebeanServer.find(Applicant.class).findSet(), executionContext);
  }

  public CompletionStage<Optional<Applicant>> lookupApplicant(long id) {
    return supplyAsync(
        () -> ebeanServer.find(Applicant.class).setId(id).findOneOrEmpty(), executionContext);
  }

  /**
   * Returns all programs that are appropriate to serve to an applicant - which is any active
   * program, plus any program where they have an application in the draft stage.
   */
  public CompletionStage<ImmutableList<ProgramDefinition>> programsForApplicant(long applicantId) {
    return supplyAsync(
            () -> {
              List<Program> inProgressPrograms =
                  ebeanServer
                      .find(Program.class)
                      .alias("p")
                      .where()
                      .exists(
                          ebeanServer
                              .find(Application.class)
                              .where()
                              .eq("applicant.id", applicantId)
                              .eq("lifecycle_stage", LifecycleStage.DRAFT)
                              .raw("program.id = p.id")
                              .query())
                      .endOr()
                      .findList();
              List<Program> activePrograms =
                  versionRepositoryProvider.get().getActiveVersion().getPrograms();
              return new ImmutableList.Builder<Program>()
                  .addAll(activePrograms)
                  .addAll(inProgressPrograms)
                  .build();
            },
            executionContext.current())
        .thenApplyAsync(
            (programs) ->
                programs.stream()
                    .map(program -> program.getProgramDefinition())
                    .collect(ImmutableList.toImmutableList()));
  }

  public Optional<Account> lookupAccount(String emailAddress) {
    if (emailAddress == null || emailAddress.isEmpty()) {
      return Optional.empty();
    }
    return ebeanServer
        .find(Account.class)
        .where()
        .eq("email_address", emailAddress)
        .findOneOrEmpty();
  }

  public CompletionStage<Optional<Applicant>> lookupApplicant(String emailAddress) {
    return supplyAsync(
        () -> {
          Optional<Account> accountMaybe = lookupAccount(emailAddress);
          // Return the applicant which was most recently created.
          return accountMaybe.flatMap(
              account ->
                  account.getApplicants().stream()
                      .max(Comparator.comparing(compared -> compared.getWhenCreated())));
        },
        executionContext);
  }

  public CompletionStage<Void> insertApplicant(Applicant applicant) {
    return supplyAsync(
        () -> {
          ebeanServer.insert(applicant);
          return null;
        },
        executionContext);
  }

  public CompletionStage<Void> updateApplicant(Applicant applicant) {
    return supplyAsync(
        () -> {
          ebeanServer.update(applicant);
          return null;
        },
        executionContext);
  }

  public Optional<Applicant> lookupApplicantSync(long id) {
    return ebeanServer.find(Applicant.class).setId(id).findOneOrEmpty();
  }

  /** Merge the older applicant data into the newer applicant, and set both to the given account. */
  public CompletionStage<Applicant> mergeApplicants(
      Applicant left, Applicant right, Account account) {
    return supplyAsync(
        () -> {
          left.setAccount(account);
          left.save();
          right.setAccount(account);
          right.save();
          Applicant merged = mergeApplicants(left, right);
          merged.save();
          return merged;
        },
        executionContext);
  }

  /** Merge the applicant data from older applicant into the newer applicant. */
  private Applicant mergeApplicants(Applicant left, Applicant right) {
    if (left.getWhenCreated().isAfter(right.getWhenCreated())) {
      Applicant tmp = left;
      left = right;
      right = tmp;
    }
    // At this point, "left" is older, "right" is newer, we will merge "left" into "right", because
    // the newer applicant is always preferred when more than one applicant matches an account.
    right.getApplicantData().mergeFrom(left.getApplicantData());
    return right;
  }

  public List<TrustedIntermediaryGroup> listTrustedIntermediaryGroups() {
    return ebeanServer.find(TrustedIntermediaryGroup.class).findList();
  }

  public TrustedIntermediaryGroup createNewTrustedIntermediaryGroup(
      String name, String description) {
    TrustedIntermediaryGroup tiGroup = new TrustedIntermediaryGroup(name, description);
    tiGroup.save();
    return tiGroup;
  }

  public void deleteTrustedIntermediaryGroup(long id) throws NoSuchTrustedIntermediaryGroupError {
    Optional<TrustedIntermediaryGroup> tiGroup = getTrustedIntermediaryGroup(id);
    if (tiGroup.isEmpty()) {
      throw new NoSuchTrustedIntermediaryGroupError();
    }
    ebeanServer.delete(tiGroup.get());
  }

  public Optional<TrustedIntermediaryGroup> getTrustedIntermediaryGroup(long id) {
    return ebeanServer.find(TrustedIntermediaryGroup.class).setId(id).findOneOrEmpty();
  }

  /**
   * Adds the given email address to the TI group. If the email address does not correspond to an
   * existing account, then create an account and associate it, so it will be ready when the TI
   * signs in for the first time.
   */
  public void addTrustedIntermediaryToGroup(long id, String emailAddress)
      throws NoSuchTrustedIntermediaryGroupError {
    Optional<TrustedIntermediaryGroup> tiGroup = getTrustedIntermediaryGroup(id);
    if (tiGroup.isEmpty()) {
      throw new NoSuchTrustedIntermediaryGroupError();
    }
    Optional<Account> accountMaybe = lookupAccount(emailAddress);
    Account account =
        accountMaybe.orElseGet(
            () -> {
              Account a = new Account();
              a.setEmailAddress(emailAddress);
              a.save();
              return a;
            });
    account.setMemberOfGroup(tiGroup.get());
    account.save();
  }

  public void removeTrustedIntermediaryFromGroup(long id, long accountId)
      throws NoSuchTrustedIntermediaryGroupError, NoSuchTrustedIntermediaryError {
    Optional<TrustedIntermediaryGroup> tiGroup = getTrustedIntermediaryGroup(id);
    if (tiGroup.isEmpty()) {
      throw new NoSuchTrustedIntermediaryGroupError();
    }
    Optional<Account> accountMaybe = lookupAccount(accountId);
    if (accountMaybe.isEmpty()) {
      throw new NoSuchTrustedIntermediaryError();
    }
    Account account = accountMaybe.get();
    if (account.getMemberOfGroup().isPresent()
        && account.getMemberOfGroup().get().equals(tiGroup.get())) {
      account.setMemberOfGroup(null);
      account.save();
    } else {
      throw new NoSuchTrustedIntermediaryError();
    }
  }

  private Optional<Account> lookupAccount(long accountId) {
    return ebeanServer.find(Account.class).setId(accountId).findOneOrEmpty();
  }
}