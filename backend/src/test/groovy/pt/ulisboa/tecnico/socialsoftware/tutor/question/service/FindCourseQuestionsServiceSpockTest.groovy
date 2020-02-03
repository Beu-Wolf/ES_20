package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import spock.lang.Specification

@DataJpaTest
class FindCourseQuestionsServiceSpockTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'

    @Autowired
    QuestionService questionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    ImageRepository imageRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    def course
    def courseExecution

    def setup() {
        course = new Course(COURSE_NAME)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM)
        courseExecutionRepository.save(courseExecution)
    }

    def "create a question with image and two options and a quiz questions with two answers"() {
        given: "createQuestion a question"
        def question = new Question()
        question.setKey(1)
        question.setContent(QUESTION_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(0)
        question.setNumberOfCorrect(0)
        question.setCourse(course)
        and: 'an image'
        def image = new Image()
        image.setUrl(URL)
        image.setWidth(20)
        imageRepository.save(image)
        question.setImage(image)
        and: 'two options'
        def optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setQuestion(question)
        optionRepository.save(optionOK)
        def options = new ArrayList<>()
        options.add(optionOK)
        def optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setQuestion(question)
        optionRepository.save(optionKO)
        options.add(optionKO)
        question.setOptions(options)
        questionRepository.save(question)
        def quizQuestion = new QuizQuestion()
        quizQuestionRepository.save(quizQuestion)
        question.addQuizQuestion(quizQuestion)
        def questionAnswer = new QuestionAnswer()
        questionAnswer.setOption(optionOK)
        questionAnswerRepository.save(questionAnswer)
        quizQuestion.addQuestionAnswer(questionAnswer)
        questionAnswer = new QuestionAnswer()
        questionAnswer.setOption(optionKO)
        questionAnswerRepository.save(questionAnswer)
        quizQuestion.addQuestionAnswer(questionAnswer)


        when:
        def result = questionService.findQuestions(COURSE_NAME)

        then: "the returned data are correct"
        result.size() == 1
        def resQuestion = result.get(0)
        resQuestion.getId() != null
        resQuestion.getStatus() == Question.Status.AVAILABLE.name()
        resQuestion.getContent() == QUESTION_CONTENT
        resQuestion.getNumberOfAnswers() == 2
        resQuestion.getNumberOfCorrect() == 1
        resQuestion.getDifficulty() == 50
        resQuestion.getImage().getId() != null
        resQuestion.getImage().getUrl() == URL
        resQuestion.getImage().getWidth() == 20
        resQuestion.getOptions().size() == 2
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }

}
