const APPROVED = 'Approved';
const REJECTED = 'Rejected';
const WAITING_FOR_APPROVAL = 'Waiting for Approval';
const PRIVATE = 'Private';

describe('Student Question Submission', () => {
  let ts;
  let questionContent = 'Test Question Content';
  let questionContent2 = 'New Test Question Content';
  let topics = ['Adventure Builder', 'Chrome'];
  let topics2 = ['Hadoop', 'Infinispan'];
  let options = [
    'Test Option 1',
    'Test Option 2',
    'Test Option 3',
    'Test Option 4'
  ];
  let options2 = [
    'New Test Option 1',
    'New Test Option 2',
    'New Test Option 3',
    'New Test Option 4'
  ];

  const demoStudent = 'Demo Student';

  beforeEach(() => {
    cy.exec('psql tutordb < ../backend/resetStudentQuestions.sql');
    cy.demoStudentLogin();
  });

  afterEach(() => {
    // clean exit
    cy.contains('Demo Course').click();
    cy.logout();
  });

  it('change demo student dashboard stats visibility', () => {
    // make the 2 stats private and check
    cy.get('[data-cy="dashboardButton"]').click();
    const submittedQuestionsDiv = '[data-cy="submittedQuestionsDiv"]';
    const approvedQuestionsDiv = '[data-cy="approvedQuestionsDiv"]';
    const submittedQuestionsRow = '[data-cy="submittedQuestionsRow"]';
    const approvedQuestionsRow = '[data-cy="approvedQuestionsRow"]';

    cy.makePrivate(submittedQuestionsDiv);
    cy.makePrivate(approvedQuestionsDiv);

    cy.openDashboardStatsDialog(demoStudent);
    cy.checkVisibility(submittedQuestionsRow, PRIVATE);
    cy.checkVisibility(approvedQuestionsRow, PRIVATE);
    cy.get('[data-cy="closeDashboardTable"]').click();

    // make the 2 stats public and check
    cy.makePublic(submittedQuestionsDiv);
    cy.makePublic(approvedQuestionsDiv);

    cy.openDashboardStatsDialog(demoStudent);
    cy.checkVisibility(submittedQuestionsRow, '0');
    cy.checkVisibility(approvedQuestionsRow, '0');
    cy.get('[data-cy="closeDashboardTable"]').click();
  });

  it('check no approved and submitted questions', () => {
    cy.get('[data-cy="dashboardButton"]').click();
    cy.get('[data-cy="submittedQuestions"]').contains('0');
    cy.get('[data-cy="approvedQuestions"]').contains('0');
  });

  it('create 2 questions, teacher approves one, reflected in dashboard', () => {
    cy.get('[data-cy="my-area"]').click();
    cy.get('[data-cy="student-questions"]').click();

    // question1
    ts = Date.now().toString();
    let questionTitle = 'Question #' + ts;

    cy.createStudentQuestion(questionTitle, questionContent, topics, options, [
      1
    ]);

    // question2
    ts = Date.now().toString();
    let questionTitle2 = 'Question 2 #' + ts;
    cy.createStudentQuestion(
      questionTitle2,
      questionContent2,
      topics2,
      options2,
      [1]
    );

    // login as a teacher
    cy.logout();
    cy.demoTeacherLogin();

    let justification = 'Good question!';
    let prevStatus = WAITING_FOR_APPROVAL;
    let status = APPROVED;

    cy.get('[data-cy="management"]').click();
    cy.get('[data-cy="student-questions"]').click();

    cy.evaluateStudentQuestion(
      questionTitle2,
      prevStatus,
      status,
      justification
    );

    // login as student again
    cy.logout();
    cy.demoStudentLogin();

    cy.get('[data-cy="dashboardButton"]').click();
    cy.get('[data-cy="submittedQuestions"]').contains('2');
    cy.get('[data-cy="approvedQuestions"]').contains('1');
  });
});
