import { createStore } from 'vuex';

const store = createStore({
  state: {
    players: [],
    gameStatus: 'waiting', // 'waiting', 'playing', 'finished'
    currentPlayer: null,
    gameBoard: Array(9).fill(null), // Example for a 3x3 game board
  },
  mutations: {
    setPlayers(state, players) {
      state.players = players;
    },
    setCurrentPlayer(state, player) {
      state.currentPlayer = player;
    },
    updateGameBoard(state, { index, player }) {
      if (!state.gameBoard[index]) {
        state.gameBoard.splice(index, 1, player);
      }
    },
    setGameStatus(state, status) {
      state.gameStatus = status;
    },
    resetGame(state) {
      state.gameBoard = Array(9).fill(null);
      state.gameStatus = 'waiting';
      state.currentPlayer = null;
    },
  },
  actions: {
    joinGame({ commit }, player) {
      // Logic to join the game
      commit('setCurrentPlayer', player);
    },
    makeMove({ commit }, { index, player }) {
      commit('updateGameBoard', { index, player });
      // Additional logic to check for win or draw
    },
    startGame({ commit }) {
      commit('setGameStatus', 'playing');
    },
    endGame({ commit }) {
      commit('setGameStatus', 'finished');
    },
  },
  getters: {
    getPlayers(state) {
      return state.players;
    },
    getGameStatus(state) {
      return state.gameStatus;
    },
    getCurrentPlayer(state) {
      return state.currentPlayer;
    },
    getGameBoard(state) {
      return state.gameBoard;
    },
  },
});

export default store;