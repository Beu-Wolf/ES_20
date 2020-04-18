let APPROVED = 'Approved';
let REJECTED = 'Rejected';
let WAITING_FOR_APPROVAL = 'Waiting for Approval';

describe('Student Question Evaluation', () => {
  let questionTitle = 'Question #' + Date.now().toString();
  let questionContent = 'To be or not to be?';
  let topics = ['Adventure Builder'];
  let options = ['AAAA', 'BBBB', 'CCCC', 'DDDD'];

  beforeEach(() => {
    questionTitle = 'Question #' + Date.now().toString();
    // log in as student
    cy.demoStudentLogin();

    // create student question
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();
    cy.createStudentQuestion(
      questionTitle,
      questionContent,
      topics,
      options,
      [1]
    );

    // logout
    cy.contains('Logout').click();
  });

  afterEach(() => {
    cy.contains('Demo Course').click();
    cy.contains('Logout').click();
  });

  it('Check an approved question', () => {
    let justification = null;
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = APPROVED;

    // approve question (as teacher)
    cy.demoTeacherLogin();

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );

    cy.assertStudentQuestionEvaluation(
      questionTitle,
      status,
      justification === null ? '' : justification
    );
    cy.contains('Logout').click();

    // Check approved question (as student)
    cy.demoStudentLogin();
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.studentAssertEvaluation(questionTitle, status, justification);
  });

  it('Check a rejected question and justification', () => {
    let justification = 'Supercalifragilisticexpialidocious';
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = REJECTED;

    // approve question (as teacher)
    cy.demoTeacherLogin();

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle,
      prevStatus,
      status,
      justification
    );

    cy.assertStudentQuestionEvaluation(
      questionTitle,
      status,
      justification === null ? '' : justification
    );
    cy.contains('Logout').click();

    // Check approved question (as student)
    cy.demoStudentLogin();
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();


    cy.studentAssertEvaluation(questionTitle, status, justification);
  });
});
