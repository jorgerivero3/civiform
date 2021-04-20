package services.applicant.question;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import models.LifecycleStage;
import org.junit.Test;
import org.junit.runner.RunWith;
import services.Path;
import services.applicant.ApplicantData;
import services.question.QuestionOption;
import services.question.exceptions.UnsupportedQuestionTypeException;
import services.question.types.AddressQuestionDefinition;
import services.question.types.CheckboxQuestionDefinition;
import services.question.types.DropdownQuestionDefinition;
import services.question.types.FileUploadQuestionDefinition;
import services.question.types.NameQuestionDefinition;
import services.question.types.NumberQuestionDefinition;
import services.question.types.QuestionDefinitionBuilder;
import services.question.types.QuestionType;
import services.question.types.RadioButtonQuestionDefinition;
import services.question.types.TextQuestionDefinition;

@RunWith(JUnitParamsRunner.class)
public class ApplicantQuestionTest {

  private static final CheckboxQuestionDefinition checkboxQuestionDefinition =
      new CheckboxQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"),
          ImmutableList.of(
              QuestionOption.create(1L, ImmutableMap.of(Locale.US, "option 1")),
              QuestionOption.create(2L, ImmutableMap.of(Locale.US, "option 2"))));
  private static final DropdownQuestionDefinition dropdownQuestionDefinition =
      new DropdownQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"),
          ImmutableList.of(
              QuestionOption.create(
                  1L, ImmutableMap.of(Locale.US, "option 1", Locale.FRANCE, "un")),
              QuestionOption.create(
                  2L, ImmutableMap.of(Locale.US, "option 2", Locale.FRANCE, "deux"))));
  private static final FileUploadQuestionDefinition fileUploadQuestionDefinition =
      new FileUploadQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final TextQuestionDefinition textQuestionDefinition =
      new TextQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final NameQuestionDefinition nameQuestionDefinition =
      new NameQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final NumberQuestionDefinition numberQuestionDefinition =
      new NumberQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final AddressQuestionDefinition addressQuestionDefinition =
      new AddressQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"));
  private static final RadioButtonQuestionDefinition radioButtonQuestionDefinition =
      new RadioButtonQuestionDefinition(
          1L,
          "question name",
          Path.create("applicant.my.path.name"),
          Optional.empty(),
          "description",
          LifecycleStage.ACTIVE,
          ImmutableMap.of(Locale.US, "question?"),
          ImmutableMap.of(Locale.US, "help text"),
          ImmutableList.of(
              QuestionOption.create(1L, ImmutableMap.of(Locale.US, "option 1")),
              QuestionOption.create(1L, ImmutableMap.of(Locale.US, "option 2"))));

  // TODO(https://github.com/seattle-uat/civiform/issues/405): Change this to just use
  // @Parameters(source = QuestionType.class) once RepeatedQuestionDefinition exists.
  @Test
  @Parameters(method = "types")
  public void errorsPresenterExtendedForAllTypes(QuestionType type)
      throws UnsupportedQuestionTypeException {
    QuestionDefinitionBuilder builder = QuestionDefinitionBuilder.sample(type);
    ApplicantQuestion question = new ApplicantQuestion(builder.build(), new ApplicantData());

    assertThat(question.errorsPresenter().hasTypeSpecificErrors()).isFalse();
  }

  private EnumSet<QuestionType> types() {
    return EnumSet.complementOf(EnumSet.of(QuestionType.REPEATER));
  }

  @Test
  public void getsExpectedQuestionType() {
    ApplicantQuestion addressApplicantQuestion =
        new ApplicantQuestion(addressQuestionDefinition, new ApplicantData());
    assertThat(addressApplicantQuestion.createAddressQuestion())
        .isInstanceOf(AddressQuestion.class);

    ApplicantQuestion fileUploadApplicantQuestion =
        new ApplicantQuestion(fileUploadQuestionDefinition, new ApplicantData());
    assertThat(fileUploadApplicantQuestion.createFileUploadQuestion())
        .isInstanceOf(FileUploadQuestion.class);

    ApplicantQuestion nameApplicantQuestion =
        new ApplicantQuestion(nameQuestionDefinition, new ApplicantData());
    assertThat(nameApplicantQuestion.createNameQuestion()).isInstanceOf(NameQuestion.class);

    ApplicantQuestion numberApplicantQuestion =
        new ApplicantQuestion(numberQuestionDefinition, new ApplicantData());
    assertThat(numberApplicantQuestion.createNumberQuestion()).isInstanceOf(NumberQuestion.class);

    ApplicantQuestion singleSelectApplicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, new ApplicantData());
    assertThat(singleSelectApplicantQuestion.createSingleSelectQuestion())
        .isInstanceOf(SingleSelectQuestion.class);

    ApplicantQuestion textApplicantQuestion =
        new ApplicantQuestion(textQuestionDefinition, new ApplicantData());
    assertThat(textApplicantQuestion.createTextQuestion()).isInstanceOf(TextQuestion.class);

    ApplicantQuestion radioApplicantQuestion =
        new ApplicantQuestion(radioButtonQuestionDefinition, new ApplicantData());
    assertThat(radioApplicantQuestion.createSingleSelectQuestion())
        .isInstanceOf(SingleSelectQuestion.class);

    ApplicantQuestion checkboxApplicantQuestion =
        new ApplicantQuestion(checkboxQuestionDefinition, new ApplicantData());
    assertThat(checkboxApplicantQuestion.createMultiSelectQuestion())
        .isInstanceOf(MultiSelectQuestion.class);
  }

  @Test
  public void equals() {
    ApplicantData dataWithAnswers = new ApplicantData();
    dataWithAnswers.putString(Path.create("applicant.color"), "blue");

    new EqualsTester()
        .addEqualityGroup(
            new ApplicantQuestion(addressQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(addressQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(checkboxQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(checkboxQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(dropdownQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(dropdownQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(fileUploadQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(fileUploadQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(nameQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(nameQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(numberQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(numberQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(radioButtonQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(radioButtonQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(textQuestionDefinition, new ApplicantData()),
            new ApplicantQuestion(textQuestionDefinition, new ApplicantData()))
        .addEqualityGroup(
            new ApplicantQuestion(textQuestionDefinition, dataWithAnswers),
            new ApplicantQuestion(textQuestionDefinition, dataWithAnswers))
        .testEquals();
  }
}