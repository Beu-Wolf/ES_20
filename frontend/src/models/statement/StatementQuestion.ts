import StatementOption from '@/models/statement/StatementOption';
import Image from '@/models/management/Image';
import { _ } from 'vue-underscore';
import ClarificationRequest from '../clarification/ClarificationRequest';

export default class StatementQuestion {
  quizQuestionId!: number;
  content!: string;
  image: Image | null = null;
  questionId!: number;

  options: StatementOption[] = [];
  clarifications: ClarificationRequest[] = [];

  constructor(jsonObj?: StatementQuestion) {
    if (jsonObj) {
      this.quizQuestionId = jsonObj.quizQuestionId;
      this.content = jsonObj.content;
      this.image = jsonObj.image;
      this.questionId = jsonObj.questionId;

      if (jsonObj.options) {
        this.options = _.shuffle(
          jsonObj.options.map(
            (option: StatementOption) => new StatementOption(option)
          )
        );
      }
      if (jsonObj.clarifications) {
        this.clarifications = jsonObj.clarifications.map(
          (clarification: ClarificationRequest) =>
            new ClarificationRequest(clarification)
        );
      }
    }
  }

  getQuestionId(): number {
    return this.questionId;
  }

  addClarificationRequest(request: ClarificationRequest): void {
    this.clarifications.push(request);
  }

  getClarificationRequests(): ClarificationRequest[] {
    return this.clarifications;
  }
}
