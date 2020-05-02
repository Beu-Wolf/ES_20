package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USERNAME_NOT_FOUND;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND;

@Service
public class MyStatsService {
    @Autowired
    private UserRepository userRepository;


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MyStatsDto getMyStats(String username, int executionId) {
        User user = userRepository.findByUsername(username);
        if (user == null ) {
            throw new TutorException(USERNAME_NOT_FOUND, username);
        }


        MyStatsDto statsDto = new MyStatsDto(user.getMyStats());


        //Calculate only if public, lets assume it's the users num
        if(statsDto.getTestStat() == MyStats.StatsVisibility.PUBLIC) {
            statsDto.setTestStat(user.getNumberOfCorrectInClassAnswers());
        }

        return statsDto;
    }


}
