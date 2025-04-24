<template>
  <div class="all">
    <div id="mtapp">
      <div class="container">
        <div class="bg">
          <header class="test-header">
            <h1>问韵寻章</h1>
            <p>一卷诗书藏古意，半帘花影读春秋</p>
          </header>




          <!-- 弹窗 -->
          <div v-if="isModalVisible" class="modal-overlay">
            <div class="modal">
              <img src="../assets/image/imgtest/WechatIMG538.png" alt="Image" class="left-image" />

              <template v-if="showResultsFlag">
                <h2 v-if="correctCount <=0.5*selectedQuestionCount">诗词萌新</h2>
                <h2 v-else-if="correctCount<=0.8*selectedQuestionCount">诗词小达人</h2>
                <h2 v-else>诗词通关王</h2>
                <p>你答对了 {{ correctCount }} 道题！</p>
                <p>{{ modalContent.explanation }}</p>
                <button @click="confirm(); handleProgressClick()">再来一遍</button>
              </template>

              <template v-else>
                <h2>{{ modalContent.title }}</h2>
                <p>{{ modalContent.message }}</p>
                <p>{{ modalContent.explanation }}</p>
                <button @click="handleExplanationConfirm()">继续下一题</button>
              </template>

              <img src="../assets/image/imgtest/WechatIMG534.png" alt="Image" class="right-image" />
            </div>
          </div>
        </div>




        <!-- 当前题目显示 -->
        <div class="test">
          <!-- 开始界面 -->
          <div v-if="showStartScreen" class="start-screen">

            <div class="difficulty-selector">
              <h3>选择难度</h3>
              <div class="difficulty-options">
                <button v-for="option in difficultyOptions" :key="option.value"
                  @click="selectedDifficulty = option.value"
                  :class="['start-btn', { active: selectedDifficulty === option.value }]">
                  {{ option.label }}
                </button>
              </div>
            </div>

            <div class="count-selector">
              <h3>选择题目数量</h3>
              <div class="count-options">
                <button v-for="num in questionCountOptions" :key="num" @click="selectedQuestionCount = num"
                  :class="['start-btn', { active: selectedQuestionCount === num }]">
                  {{ num }}
                </button>
              </div>
            </div>

            <button @click="startQuiz" class="beging-btn"
              :disabled="!selectedDifficulty || !selectedQuestionCount">开始答题</button>
          </div>

          <transition name="fade" mode="out-in">
            <div class="a" v-if="currentQuestion && !showStartScreen" :key="currentQuestion.id">
              <p>{{ currentIndex + 1 }}. {{ currentQuestion.question }}</p>
              <div class="option">
                <button v-for="(text, key) in currentQuestion.options" :key="key"
                  :class="buttonState(optionIdPrefix(currentQuestion.id, key))"
                  @click="handleOptionClick(currentQuestion.id, key)" :disabled="showResultsFlag || isAnswered">
                  {{ optLabel(key) }}.{{ text }}
                </button>
              </div>
            </div>
          </transition>
        </div>


        <div class="progress-container" v-if="!showStartScreen">
          <div class="progress-bar-bg">
            <div class="progress-bar-fg" :style="{ width: progressPercentage + '%' }"></div>
            <img src="../assets/image/imgtest/brush.png" alt="毛笔" class="brush"
              :style="{ left: progressPercentage + '%' }" />
          </div>
          <div class="progress-text"> 第{{ currentIndex + 1 }} / {{ totalQuestions }} 题</div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "mtApp",
  data() {
    return {
      showStartScreen: true,
      selectedDifficulty: 'easy',
      selectedQuestionCount: 10,
      difficultyOptions: [
        { label: '简单', value: 'easy' },
        { label: '普通', value: 'normal' },
        { label: '困难', value: 'hard' }
      ],
      questionCountOptions: [10, 15, 20],
      buttonStates: {},
      correctAnswers: {},
      allQuestions: [],
      questions: [],
      showResultsFlag: false,
      isResultsVisible: false,
      isRestarting: false,
      isModalVisible: false,
      correctCount: 0,
      currentIndex: 0,
      isAnswered: false,
      modalContent: {
        title: '',
        message: '',
        explanation: ''
      }
    };
  },

  computed: {
    progressPercentage() {
      return ((this.currentIndex + (this.isAnswered ? 1 : 0)) / this.totalQuestions) * 100;
    },
    totalQuestions() {
      return this.questions.length;
    },
    answeredQuestionsCount() {
      return Object.values(this.buttonStates).filter(
        (state) => state === "color-change clicked"
      ).length;
    },
    progressPercentage() {
      return (this.answeredQuestionsCount / this.totalQuestions) * 100;
    },
    currentQuestion() {
      return this.questions[this.currentIndex] || null;
    },
  },
  mounted() {
    fetch('/questions.json')
      .then(res => res.json())
      .then(data => {
        this.allQuestions = data;
        this.questions = this.shuffleArray(data).slice(0, 10);

        for (let q of this.questions) {
          const correctId = this.optionIdPrefix(q.id, q.answer[0]);
          this.correctAnswers[correctId] = true;
        }
      });
  },
  methods: {
    shuffleArray(array) {
      let shuffled = array.slice();
      for (let i = shuffled.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
      }
      return shuffled;
    },
    optionIdPrefix(qid, optKey) {
      return `${qid}_${optKey}`;
    },
    optLabel(key) {
      return key.toUpperCase();
    },
    handleOptionClick(qid, optKey) {
      const btnId = this.optionIdPrefix(qid, optKey);
      const otherBtnIds = ['a', 'b', 'c', 'd']
        .filter((k) => k !== optKey)
        .map((k) => this.optionIdPrefix(qid, k));

      this.changeScore(btnId, ...otherBtnIds);
      this.isAnswered = true;

      const correctId = Object.keys(this.correctAnswers).find((id) => id.startsWith(qid + '_'));

      if (btnId === correctId) {
        this.correctCount++;
        setTimeout(() => {
          this.nextQuestion();
        }, 1000);
      } else {
        this.showExplanationModal(qid);
      }
    },
    showExplanationModal(qid) {
      const question = this.questions.find(q => q.id === qid);
      this.modalContent = {
        title: "答错啦",
        message: `正确答案是：${this.optLabel(question.answer[0])}. ${question.options[question.answer[0]]}`,
      };
      this.isModalVisible = true;
    },
    handleExplanationConfirm() {
      this.isModalVisible = false;
      this.nextQuestion();
    },
    nextQuestion() {
      if (this.currentIndex < this.questions.length - 1) {
        this.currentIndex++;
        this.isAnswered = false;
      } else {
        this.showResultsFlag = true;
        this.isResultsVisible = true;
        this.isModalVisible = true;
      }
    },
    changeScore(btnId1, btnId2, btnId3, btnId4) {
      if (!this.showResultsFlag) {
        this.buttonStates[btnId1] = "color-change clicked";
        this.buttonStates[btnId2] = "color-change";
        this.buttonStates[btnId3] = "color-change";
        this.buttonStates[btnId4] = "color-change";
      }
    },
    buttonState(btnId) {
      return this.buttonStates[btnId] || "color-change";
    },
    confirm() {
      this.isModalVisible = false;
      window.scrollTo({ top: 0, behavior: "smooth" });
    },
    handleProgressClick() {

      if (this.isResultsVisible) {
        this.resetQuiz();
        this.showStartScreen = true;
        window.scrollTo({ top: 0, behavior: "smooth" });
      } else {
        this.showResults();
      }
    },
    showResults() {
      if (this.answeredQuestionsCount === this.totalQuestions) {
        this.isResultsVisible = true;
        this.showResultsFlag = true;

        let correctCount = 0;
        for (let btnId in this.buttonStates) {
          if (this.buttonStates[btnId] === "color-change clicked") {
            if (this.correctAnswers[btnId]) {
              this.buttonStates[btnId] = "correct-selected";
              correctCount++;
            } else {
              this.buttonStates[btnId] = "wrong-selected";
            }
          } else if (this.correctAnswers[btnId]) {
            this.buttonStates[btnId] = "correct-answer";
          }
        }

        this.correctCount = correctCount;
        this.isModalVisible = true;
      }
    },
    resetQuiz() {
      this.isResultsVisible = false;
      this.isRestarting = false;
      this.showResultsFlag = false;
      this.correctCount = 0;
      this.buttonStates = {};
      this.currentIndex = 0;
      this.isAnswered = false;

      this.questions = this.shuffleArray(this.allQuestions).slice(0, 10);

      this.correctAnswers = {};
      for (let q of this.questions) {
        const correctId = this.optionIdPrefix(q.id, q.answer[0]);
        this.correctAnswers[correctId] = true;
      }

      window.scrollTo({ top: 0, behavior: "smooth" });
    },
    startQuiz() {
      this.showStartScreen = false;
      const count = this.selectedQuestionCount;

      // 筛选简单、中等、困难题目
      const easyQuestions = this.allQuestions.filter(q => q.id < 50);
      const hardQuestions = this.allQuestions.filter(q => q.id >= 50);

      let selectedQuestions = [];

      if (this.selectedDifficulty === 'easy') {
        selectedQuestions = this.shuffleArray(easyQuestions).slice(0, count);
      } else if (this.selectedDifficulty === 'normal') {
        const half = Math.floor(count / 2);
        const normalEasyPart = this.shuffleArray(easyQuestions).slice(0, half);
        const normalHardPart = this.shuffleArray(hardQuestions).slice(0, count - half);
        selectedQuestions = [...normalEasyPart, ...normalHardPart];
        selectedQuestions = this.shuffleArray(selectedQuestions); // 再打乱一次
      } else if (this.selectedDifficulty === 'hard') {
        selectedQuestions = this.shuffleArray(hardQuestions).slice(0, count);
      }

      this.questions = selectedQuestions;

      // 初始化正确答案映射
      this.correctAnswers = {};
      for (let q of this.questions) {
        const correctId = this.optionIdPrefix(q.id, q.answer[0]);
        this.correctAnswers[correctId] = true;
      }

      // 初始化状态
      this.currentIndex = 0;
      this.correctCount = 0;
      this.buttonStates = {};
      this.isAnswered = false;

      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: "smooth" });
      this.showStartScreen = false;
    },
  },
};
</script>


<style scoped>
.test-header {
  text-align: center;
  padding: 0.05rem;
  margin-left: 20px;
  margin-right: 20px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  margin-bottom: 2rem;
}

.test-header h1 {
  display: flex;
  justify-content: center;
  color: #e5e5e5;
  font-family: eva;
  font-size: 40px;
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 3px 3px 10px rgba(0, 0, 0, 0.5);
  animation: float 3s ease-in-out infinite;
}

.test-header p {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-4px);
  }

  100% {
    transform: translateY(0);
  }
}

/* 弹窗的背景遮罩 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 弹窗样式 */
/* 背景覆盖层 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  /* 半透明背景 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  /* 确保弹窗在最上层 */
}

/* 弹窗容器 */
.modal {
  background: linear-gradient(135deg, #f6df8e, #c9c19e);
  /* 古风米白+淡灰 */
  padding: 30px 40px;
  border-radius: 15px;
  text-align: center;
  width: 350px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  transform: scale(0);
  animation: modalScale 0.3s ease-out forwards;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.modal button {
  background: #a17f61;
  /* 墨绿到木棕 */
  color: #f9f5ec;
  /* 柔和米白字色 */
  border: none;
  padding: 12px 25px;
  border-radius: 50px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.modal button:hover {
  transform: scale(1.05);
}

/* 弹窗标题 */
.modal h2 {
  font-size: 27px;
  font-weight: 600;
  color: #5a4634;
  text-transform: uppercase;
  letter-spacing: 1px;
  animation: fadeIn 1s ease-out;
}

.modal p {
  font-size: 17px;
  color: #5a4634;
  font-family: 'Arial', sans-serif;
  text-align: center;
  letter-spacing: 1px;
  opacity: 0;
  animation: slideUp 1s ease-out forwards;
  color: #555;
  font-weight: 600;

  max-width: 220px;
  /* 限制最大宽度 */
  word-break: break-word;
  /* 强制长单词或 URL 换行 */
  white-space: normal;
  /* 正常换行模式 */

}


/* 滑动动画 */
@keyframes slideUp {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}



/* 按钮悬浮效果 */
.modal button:hover {
  transform: translateY(-3px);
  /* 上移动画 */
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

/* 按钮点击效果 */
.modal button:active {
  transform: translateY(1px);
  /* 点击时向下 */
  box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);
}


/* 左侧图片 */
.left-image {
  width: 120px;
  height: auto;
  position: absolute;
  left: 5px;
  bottom: 60px;
  animation: fadeIn 1s ease-out;
}

/* 右侧图片 */
.right-image {
  width: 120px;
  height: auto;
  position: absolute;
  right: 5px;
  bottom: 65px;
  animation: fadeIn 1s ease-out;
}

/* 渐变背景色动画 */
@keyframes fadeIn {
  0% {
    opacity: 0;
    transform: translateY(-10px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 弹窗缩放动画 */
@keyframes modalScale {
  0% {
    transform: scale(0);
  }

  100% {
    transform: scale(1);
  }
}



/* 左右图片的视觉效果 */
.modal img {
  max-width: 80px;
  margin: 20px;
  transition: transform 0.3s ease;
}

.modal img:hover {
  transform: scale(1.1);
}


.correct-answer {
  background-color: #daa6a9;
  transform: scale(1.2);
  border: 3.5px solid rgb(0, 253, 72) !important;
  background: transparent;
  text-transform: uppercase;
  padding: 15px 50px;
  transition: transform 0.4s ease;
}

.correct-selected {
  background-color: #daa6a9;
  transform: scale(1.2);
  border: 3.5px solid rgb(0, 253, 72) !important;
  background: transparent;
  text-transform: uppercase;
  padding: 15px 50px;
  transition: transform 0.4s ease;
}

.wrong-selected {
  background-color: #daa6a9;
  transform: scale(1.2);
  border: 3.5px solid rgb(250, 2, 2) !important;
  background: transparent;
  text-transform: uppercase;
  padding: 15px 50px;
  transition: transform 0.4s ease;
}

.containerww {

  height: 10px;
  /* 设置容器高度 */
}


#shiny-shadow {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #1c2541;
}

.color-change {
  &.clicked {
    background-color: #daa6a9;
    transform: scale(1.2);
    border: 3.5px solid rgb(249, 240, 204);
    background: transparent;
    text-transform: uppercase;
    padding: 15px 50px;
    transition: transform 0.4s ease;
  }
}

.test {
  font-family: maiyuan;
  display: flex;
  margin: 0 auto;
  flex-wrap: wrap;
  flex-direction: column;
  max-width: 70%;
  position: relative;
  font-size: 27px;
  font-weight: 600;
  text-align: center;
}

.a {
  padding: 50px;
  margin: 40px;
  width: 60vw;
  height: 45vh;
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(4px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  align-items: center;
  justify-content: center;
  font-size: 33px;
  color: #5a4634;

  button {
    margin: 20px 70px 10px;
    border: 3.5px solid black;
    width: 360px;
    background: transparent;
    text-transform: uppercase;
    color: #5a4634;
    padding: 15px 50px;
    outline: none;
    overflow: hidden;
    position: relative;
    font-family: maiyuan;
    font-weight: 500;
    font-size: 25px;
    background: rgba(255, 255, 255, 0.25);
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    border-radius: 10px;
    border: 1px solid rgba(255, 255, 255, 0.18);

    &:hover:after {
      left: 120%;
      transition: all 600ms cubic-bezier(0.3, 1, 0.2, 1);
    }

    &:after {
      content: "";
      display: block;
      position: absolute;
      top: -36px;
      left: -100px;
      background: rgb(255, 255, 255);
      width: 70px;
      height: 125px;
      opacity: 20%;
      transform: rotate(-45deg);
    }
  }

}





/* 新增的 option 样式 */
.option {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  /* 每行两个 */
  gap: 20px 40px;
  /* 行距和列距 */
  justify-items: center;
  /* 选项按钮水平居中 */
  align-items: center;
  padding-top: 30px;
}


.progress-container {
  position: fixed;
  width: 10em;
  height: 10em;
  margin: 20px auto;
  z-index: 99;
  left: 1em;

  .progress-circle {
    transform: rotate(-90deg);
    width: 100%;
    height: 100%;

    .circle-bg {
      fill: none;
      stroke: #eee;
      stroke-width: 3.8;
    }

    .circle {
      fill: none;
      stroke-width: 2.8;
      stroke-linecap: round;
      stroke: #03e9f4;
      transform: rotate(-90deg);
      transform-origin: 50% 50%;
    }
  }

  .progress-text {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    font-size: 20px;
    font-weight: bold;
    white-space: nowrap;
  }
}



.all {
  position: relative;
  width: 100vw;
  height: 100vh;
  font-family: 'Montserrat', sans-serif, Arial, 'Microsoft Yahei';
  background-color: #f9ead4;
  overflow: hidden;
  /* 避免伪元素溢出 */
}

.all::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('/src/assets/1.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  opacity: 0.4;
  /* 调整这里的透明度，0.0 ~ 1.0 */
  z-index: 0;
  /* 放在内容背后 */
}

.container {
  position: relative;
  width: 100vw;

  overflow: hidden;

}


.progress-container {
  text-align: center;
  margin: 50px auto;
  width: 80%;
  position: relative;
}

.progress-bar-bg {
  height: 20px;
  background: url('./src/assets/image/imgtest/scroll-bg.png') repeat-x;
  border-radius: 15px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.3);
  position: relative;
}

.progress-bar-fg {
  height: 100%;
  background: linear-gradient(to right, #cfae74, #f7e4b3);
  transition: width 0.5s ease;
  border-radius: 15px 0 0 15px;
  position: relative;
}

.brush {
  position: absolute;
  top: 1%;
  transform: translate(-50%, -50%);
  height: 70px;
  transition: left 0.5s ease;
  pointer-events: none;
  z-index: 2;
}

.progress-text {
  font-family: "KaiTi", serif;
  color: #5a3e1b;
  font-size: 16px;
}

.start-screen {
  padding: 50px;
  margin: 40px;
  width: 60vw;
  height: 45vh;
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(4px);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  align-items: center;
  justify-content: center;
  color: #5a4634;
  flex: 1;
  display: flex;
  flex-direction: column;

  .beging-btn {
    margin: 20px 70px 10px;
    border: 3.5px solid black;
    width: 360px;
    background: transparent;
    text-transform: uppercase;
    color: #5a4634;
    padding: 15px 50px;
    outline: none;
    overflow: hidden;
    position: relative;
    font-family: maiyuan;
    font-weight: 500;
    font-size: 25px;
    background: rgba(255, 255, 255, 0.25);
    box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    border-radius: 10px;
    border: 1px solid rgba(255, 255, 255, 0.18);

    /* 按钮悬浮效果 */
    &:hover {
      transform: translateY(-5px);
      /* 上移动画 */
      box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
    }

    /* 按钮点击效果 */
    &:active {
      transform: translateY(1px);
      /* 点击时向下 */
      box-shadow: 0 5px 10px rgba(0, 0, 0, 0.1);
    }

  }
}


.difficulty-selector,
.count-selector {
  padding: 0.8rem;
  border-radius: 16px;
  width: 100%;
  max-width: 450px;
  display: flex;
  flex-direction: column;
  margin-bottom: 1.8rem;
  text-align: center;
}

.difficulty-selector h3,
.count-selector h3 {
  color: #5a4634;
  margin-bottom: 2rem;
  font-size: 26px;
  white-space: nowrap;
}

.difficulty-options,
.count-options {
  display: flex;
  justify-content: center;
  gap: 3rem;
}

.difficulty-options button,
.count-options button {
  border: 1px solid #d6cab4;
  border-radius: 20px;
  background: #f8f4ed;
  color: #5a4634;
  cursor: pointer;
  transition: all 0.3s;
  width: 90px;
  height: 42px;
  font-size: 15px;
}

.difficulty-options button.active,
.count-options button.active {
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  border-color: transparent;
}

.difficulty-options button:hover,
.count-options button:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>