package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class CreateTournamentTest extends Specification {

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "create a tournament"() {
        expect: false
    }

    def "create a tournament with invalid number of questions"() {
        // exception is thrown
        expect: false
    }

    def "create a tournament with a non-existing topic"(){
        // exception is thrown
        expect: false
    }

    def "create a tournament with a non-existing student"() {
        // exception is thrown
        expect: false
    }

    def "create a tournament with a non-existing course"() {
        // exception is thrown
        expect: false
    }

    def "create a tournament with available date after conclusion"() {
        // exception is thrown
        expect: false
    }

    def "create a tournament with available date before the current date"() {
        // exception is thrown
        expect: false
    }

    def "create a tournament with student not enrolled in course"() {
        // exception is thrown
        expect: false
    }
}

