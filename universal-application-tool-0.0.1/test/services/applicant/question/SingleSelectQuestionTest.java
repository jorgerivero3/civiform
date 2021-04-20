package services.applicant.question;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Optional;
import models.Applicant;
import models.LifecycleStage;
import org.junit.Before;
import org.junit.Test;
import services.Path;
import services.applicant.ApplicantData;
import services.question.LocalizedQuestionOption;
import services.question.QuestionOption;
import services.question.types.DropdownQuestionDefinition;

public class SingleSelectQuestionTest {

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

  private Applicant applicant;
  private ApplicantData applicantData;

  @Before
  public void setUp() {
    applicant = new Applicant();
    applicantData = applicant.getApplicantData();
  }

  @Test
  public void withEmptyApplicantData() {
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, applicantData);

    SingleSelectQuestion singleSelectQuestion = new SingleSelectQuestion(applicantQuestion);

    assertThat(singleSelectQuestion.getOptions())
        .containsOnly(
            LocalizedQuestionOption.create(1L, "option 1", Locale.US),
            LocalizedQuestionOption.create(2L, "option 2", Locale.US));
    assertThat(applicantQuestion.hasErrors()).isFalse();
  }

  @Test
  public void withPresentApplicantData() {
    applicantData.putLong(dropdownQuestionDefinition.getSelectionPath(), 1L);
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, applicantData);

    SingleSelectQuestion singleSelectQuestion = applicantQuestion.createSingleSelectQuestion();

    assertThat(singleSelectQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(singleSelectQuestion.hasQuestionErrors()).isFalse();
    assertThat(singleSelectQuestion.getSelectedOptionValue())
        .hasValue(LocalizedQuestionOption.create(1L, "option 1", Locale.US));
  }

  @Test
  public void withPresentApplicantData_selectedInvalidOption_hasErrors() {
    applicantData.putLong(dropdownQuestionDefinition.getSelectionPath(), 9L);
    ApplicantQuestion applicantQuestion =
        new ApplicantQuestion(dropdownQuestionDefinition, applicantData);

    SingleSelectQuestion singleSelectQuestion = applicantQuestion.createSingleSelectQuestion();

    assertThat(singleSelectQuestion.hasTypeSpecificErrors()).isFalse();
    assertThat(singleSelectQuestion.hasQuestionErrors()).isFalse();
    assertThat(singleSelectQuestion.getSelectedOptionValue()).isEmpty();
  }
}