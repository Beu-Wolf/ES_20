package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class SignUpInTournamentTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURNAMENT_TITLE = "tournament title"
    public static final String CREATOR_NAME = "user"
    public static final String CREATOR_USERNAME = "username"
    public static final String PARTICIPANT_NAME = "participant"
    public static final String PARTICIPANT_USERNAME = "participant username"

    @Autowired
    TournamentService tournamentService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    TournamentRepository tournamentRepository

    def tournamentDto
    def creator
    def participant
    def course
    def courseExecution
    def creationDate
    def availableDate
    def runningDate
    def conclusionDate
    def formatter
    def topicDtoList

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        creator = new User(CREATOR_NAME, CREATOR_USERNAME, 1, User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)
        def creatorDto = new UserDto(creator)

        participant = new User(PARTICIPANT_NAME, PARTICIPANT_USERNAME, 2, User.Role.STUDENT)
        participant.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(participant)
        userRepository.save(participant)

        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)
        topicRepository.save(topic)

        def topicDto = new TopicDto(topic)
        topicDtoList = new ArrayList<TopicDto>()
        topicDtoList.add(topicDto)

        tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setKey(1)
        tournamentDto.setNumberOfQuestions(1)
        tournamentDto.setTopics(topicDtoList)
    }


    def "sign-up in a tournament"() {
        given: "a tournament and a participant"
        prepareStatus(tournamentDto, Tournament.Status.AVAILABLE)

        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        tournamentService.signUpInTournament(tournamentDto.getId(), participant.getUsername())

        then:
        def tournamentCreated = tournamentRepository.findAll().get(0)
        def participants = tournamentCreated.getParticipants()
        participants.size() == 2
    }

    def "sign-up in a tournament although there aren't tournaments"() {
        given: "a bad tournamentId"
        int badId = 2

        when:
        tournamentService.signUpInTournament(badId, participant.getUsername())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND
    }

    @Unroll("invalid status: #status || #errorMessage")
    def "sign-up in a tournament with non-available status"() {
        given: "a tournament with non-available status and a participant"
        prepareStatus(tournamentDto, status)
        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        if (status == Tournament.Status.CANCELLED)
            tournamentRepository.findById(tournamentDto.getId()).get().cancel()

        when:
        tournamentService.signUpInTournament(tournamentDto.getId(), participant.getUsername())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        status                      || errorMessage
        Tournament.Status.CREATED   || ErrorMessage.TOURNAMENT_NOT_AVAILABLE
        Tournament.Status.RUNNING   || ErrorMessage.TOURNAMENT_NOT_AVAILABLE
        Tournament.Status.FINISHED  || ErrorMessage.TOURNAMENT_NOT_AVAILABLE
        Tournament.Status.CANCELLED || ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "sign-up in a tournament with a user that is already signed-up"(){
        given: "a tournament with a user already signed-up and a participant"
        prepareStatus(tournamentDto, Tournament.Status.AVAILABLE)
        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        tournamentService.signUpInTournament(tournamentDto.getId(), participant.getUsername())

        when:
        tournamentService.signUpInTournament(tournamentDto.getId(), participant.getUsername())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_ALREADY_SIGNED_UP_IN_TOURNAMENT
    }

    def prepareStatus(TournamentDto tournamentDto, Tournament.Status status) {
        def now = LocalDateTime.now()

        switch(status) {
            case Tournament.Status.CREATED:
                creationDate = now.minusDays(1)
                availableDate = now.plusDays(1)
                runningDate = now.plusDays(2)
                conclusionDate = now.plusDays(3)
                break;
            case Tournament.Status.AVAILABLE:
                creationDate = now.minusDays(2)
                availableDate = now.minusDays(1)
                runningDate = now.plusDays(1)
                conclusionDate = now.plusDays(2)
                break;
            case Tournament.Status.RUNNING:
                creationDate = now.minusDays(3)
                availableDate = now.minusDays(2)
                runningDate = now.minusDays(1)
                conclusionDate = now.plusDays(1)
                break;
            case Tournament.Status.FINISHED:
            case Tournament.Status.CANCELLED:
                creationDate = now.minusDays(4)
                availableDate = now.minusDays(3)
                runningDate = now.minusDays(2)
                conclusionDate = now.minusDays(1)
                break;
        }

        tournamentDto.setCreationDate(creationDate.format(formatter))
        tournamentDto.setAvailableDate(availableDate.format(formatter))
        tournamentDto.setRunningDate(runningDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
