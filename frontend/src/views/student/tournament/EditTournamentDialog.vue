<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">New Tournament</span>
      </v-card-title>
      <v-card-text>
        <v-row>
          <v-col cols="12" sm="8">
            <v-text-field
              v-model="editTournament.title"
              label="Title"
              data-cy="title"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="4">
            <v-text-field
              v-model="editTournament.numberOfQuestions"
              label="Number of questions"
              type="number"
              data-cy="numberOfQuestions"
            ></v-text-field>
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="12" sm="4" data-cy="availableDate">
            <v-datetime-picker
              label="Available Date (default: now)"
              format="YYYY-MM-DDTHH:mm:ssZ"
              v-model="editTournament.availableDate"
              date-format="yyyy-MM-dd"
              time-format="HH:mm"
            >
            </v-datetime-picker>
          </v-col>
          <v-col cols="12" sm="4">
            <v-datetime-picker
              label="*Running Date"
              format="YYYY-MM-DDTHH:mm:ssZ"
              v-model="editTournament.runningDate"
              date-format="yyyy-MM-dd"
              time-format="HH:mm"
              data-cy="runningDate"
            >
            </v-datetime-picker>
          </v-col>
          <v-spacer></v-spacer>
          <v-col cols="12" sm="4">
            <v-datetime-picker
              label="*Conclusion Date"
              format="YYYY-MM-DDTHH:mm:ssZ"
              v-model="editTournament.conclusionDate"
              date-format="yyyy-MM-dd"
              time-format="HH:mm"
              data-cy="conclusionDate"
            >
            </v-datetime-picker>
          </v-col>
        </v-row>
        <v-card-subtitle>Topics</v-card-subtitle>
        <v-autocomplete
          v-model="tournamentTopics"
          :items="topics"
          multiple
          return-object
          item-text="name"
          item-value="name"
          @change="saveTopics"
          data-cy="topics"
        >
          <template v-slot:selection="data">
            <v-chip
              v-bind="data.attrs"
              :input-value="data.selected"
              close
              @click="data.select"
              @click:close="removeTopic(data.item)"
            >
              {{ data.item.name }}
            </v-chip>
          </template>
          <template v-slot:item="data">
            <v-list-item-content>
              <v-list-item-title v-html="data.item.name" />
            </v-list-item-content>
          </template>
        </v-autocomplete>
        <v-card-actions>
          <v-spacer />
          <v-btn color="error" @click="$emit('dialog', false)">Cancel</v-btn>
          <v-btn
            color="primary"
            dark
            @click="saveTournament"
            data-cy="saveTournament"
            >Save</v-btn
          >
        </v-card-actions>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Model, Prop } from 'vue-property-decorator';
import Store from '@/store';
import RemoteServices from '@/services/RemoteServices';
import Tournament from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';
import DatetimePicker from 'vuetify-datetime-picker';
Vue.use(DatetimePicker);
@Component
export default class EditTournamentDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Tournament, required: true }) readonly tournament!: Tournament;
  @Prop({ type: Array, required: true }) readonly topics!: Topic[];

  editTournament!: Tournament;

  // Reactive "version" of editTournament.topics
  tournamentTopics: Topic[] = JSON.parse(
    JSON.stringify(this.tournament.topics)
  );

  removeTopic(topic: Topic) {
    this.tournamentTopics = this.tournamentTopics.filter(
      element => element.id != topic.id
    );
    this.saveTopics();
  }

  saveTopics() {
    this.editTournament.topics = this.tournamentTopics;
  }

  async saveTournament() {
    if (
      this.editTournament &&
      (!this.editTournament.title ||
        !this.editTournament.runningDate ||
        !this.editTournament.conclusionDate ||
        !this.editTournament.numberOfQuestions)
    ) {
      await this.$store.dispatch('error', 'Missing fields in tournament!');
      return;
    }

    if (this.editTournament.numberOfQuestions < 1) {
      await this.$store.dispatch('error', 'Invalid number of questions!');
      return;
    }

    if (this.editTournament.topics.length == 0) {
      await this.$store.dispatch('error', 'Missing topics!');
      return;
    }

    if (!this.editTournament.id) {
      this.editTournament.creator = Store.getters.getUser;
      try {
        const result = await RemoteServices.createTournament(
          this.editTournament
        );
        this.$emit('saveTournament', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  created() {
    this.editTournament = new Tournament(this.tournament);
  }
}
</script>
