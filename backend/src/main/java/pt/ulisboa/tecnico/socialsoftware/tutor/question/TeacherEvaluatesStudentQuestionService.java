package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;

import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TeacherEvaluatesStudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;


    public void TeacherEvaluatesStudentQuestionService() {}

    public void acceptStudentQuestion(Integer studentQuestionId) {
        // not checking justification because it was not provided
        StudentQuestion studentQuestion = findStudentQuestionById(studentQuestionId);

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED);
        studentQuestion.setJustification("");
    }

    public void acceptStudentQuestion(Integer studentQuestionId, String justification) {
        checkJustification(justification);

        StudentQuestion studentQuestion = findStudentQuestionById(studentQuestionId);

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED);
        studentQuestion.setJustification(justification);
    }


    public void rejectStudentQuestion(Integer studentQuestionId, String justification) {
        checkJustification(justification);

        StudentQuestion studentQuestion = findStudentQuestionById(studentQuestionId);

        if(!canReject(studentQuestion)) {
            throw new TutorException(CANNOT_REJECT_ACCEPTED_SUGGESTION);
        }

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.REJECTED);
        studentQuestion.setJustification(justification);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private StudentQuestion findStudentQuestionById(Integer id) {
         return studentQuestionRepository.findById(id).orElseThrow(() -> new TutorException(STUDENT_QUESTION_NOT_FOUND, id));
    }

    private void checkJustification(String justification) {
        if (justification == null || justification.isBlank()) {
            throw new TutorException(INVALID_JUSTIFICATION, justification == null ? "" : justification);
        }
    }

    private boolean canReject(StudentQuestion studentQuestion) {
        // if already accepted throw error
        // change later. the question may exist in quizzes, etc
        return studentQuestion.getSubmittedStatus() != StudentQuestion.SubmittedStatus.APPROVED;
    }
}