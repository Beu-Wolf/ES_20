<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="95%"
    scrollable
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          Promote Student Question
        </span>
      </v-card-title>

      <v-card-text
        class="text-left"
        v-if="editStudentQuestion"
        data-cy="Topics"
      >
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-form>
              <v-autocomplete
                v-model="studentQuestionTopics"
                label="Topics"
                :items="topics"
                multiple
                return-object
                item-text="name"
                item-value="name"
              >
                <template v-slot:selection="data">
                  <v-chip
                    v-bind="data.attrs"
                    :input-value="data.selected"
                    close
                    @click="data.select"
                    @click:close="removeTopic(data.item)"
                    :data-cy="data.item.name"
                  >
                    {{ data.item.name }}
                  </v-chip>
                </template>
                <template v-slot:item="data">
                  <v-list-item-content data-cy="topicList">
                    <v-list-item-title v-html="data.item.name" />
                  </v-list-item-content>
                </template>
              </v-autocomplete>
            </v-form>
            <v-flex xs24 sm12 md8>
              <v-text-field
                v-model="editStudentQuestion.title"
                label="Question Title"
                data-cy="StudentQuestionTitle"
              />
            </v-flex>
            <v-flex xs24 sm12 md8>
              <v-textarea
                outline
                auto-grow
                rows="1"
                v-model="editStudentQuestion.content"
                label="Question"
                data-cy="StudentQuestionContent"
              ></v-textarea>
            </v-flex>
            <v-flex
              xs24
              sm12
              md8
              v-for="index in editStudentQuestion.options.length"
              :key="index"
            >
              <v-switch
                v-model="editStudentQuestion.options[index - 1].correct"
                class="ma-4"
                label="Correct"
                :data-cy="`CorrectOption${index}`"
              />
              <v-textarea
                outline
                auto-grow
                rows="1"
                v-model="editStudentQuestion.options[index - 1].content"
                :label="`Option ${index}`"
                :data-cy="`Option${index}`"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="error"
          @click="$emit('cancel-evaluate', false)"
          data-cy="CancelStudentQuestion"
          >Cancel</v-btn
        >
        <v-btn
          color="primary"
          dark
          @click="promoteStudentQuestion"
          data-cy="SaveStudentQuestion"
        >
          Promote
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import Topic from '@/models/management/Topic';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class EditAndPromoteStudentQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  studentQuestion!: StudentQuestion;
  @Prop({ type: Array, required: true }) readonly topics!: Topic[];

  studentQuestionTopics: Topic[] = JSON.parse(
    JSON.stringify(this.studentQuestion.topics)
  );

  editStudentQuestion!: StudentQuestion;

  created() {
    this.editStudentQuestion = new StudentQuestion(this.studentQuestion);
  }

  async promoteStudentQuestion() {
    // this should not happen.
    // Specifying this behaviour anyways
    if (!this.editStudentQuestion) {
      await this.$store.dispatch(
        'error',
        'Lost track of student question. Aborting...'
      );
      this.$emit('dialog', false);
    }

    if (
      !this.editStudentQuestion.title ||
      !this.editStudentQuestion.content ||
      !this.studentQuestionTopics
    ) {
      await this.$store.dispatch(
        'error',
        'Question must have title, content, and at least one topic'
      );
      return;
    }

    // *sigh*... prettier...
    if (
      !confirm(
        'Are you sure you want to promote this question? This action cannot be undone'
      )
    )
      return;

    this.editStudentQuestion.topics = this.studentQuestionTopics;
    this.editStudentQuestion.justification = 'Question edited by teacher';

    await this.$store.dispatch('loading');
    try {
      const result = await RemoteServices.editAndPromoteStudentQuestion(
        this.editStudentQuestion
      );
      this.$emit('save-student-question', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  removeTopic(topic: Topic) {
    this.studentQuestionTopics = this.studentQuestionTopics.filter(
      element => element.id != topic.id
    );
  }
}
</script>
