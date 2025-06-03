// src/stores/user.js
import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
  state: () => ({
    username: '',
    uid: null,
    isAdmin: false,
  }),
  actions: {
    login(user) {
      this.username = user.username;
      this.uid = user.uid;
      this.isAdmin = user.isAdmin;
    },
    logout() {
      this.username = '';
      this.uid = null;
      this.isAdmin = false;
    }
  }
});