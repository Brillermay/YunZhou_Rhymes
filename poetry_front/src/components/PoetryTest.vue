<template>
  <div class="all">
    <div id="mtapp">
      <div class="container">
        <div class="bg">
          <header class="test-header">
            <h1>问韵寻章</h1>
            <p>一卷诗书藏古意，半帘花影读春秋</p>
            <!-- 新增排行榜按钮 -->
            <button class="rank-btn" @click="handleRankClick">排行榜</button>
          </header>


          <!-- 弹窗 -->
          <div v-if="isModalVisible" class="modal-overlay">
            <div class="modal">
              <img src="../assets/image/imgtest/WechatIMG538.png" alt="Image" class="left-image" />

              <template v-if="showResultsFlag">
                <h2 v-if="correctCount <= 0.5 * selectedQuestionCount">诗词萌新</h2>
                <h2 v-else-if="correctCount <= 0.8 * selectedQuestionCount">诗词小达人</h2>
                <h2 v-else>诗词通关王</h2>
                <p v-if="isNewRecord" style="color:#d4af37;font-size:22px;margin:10px 0;">🎉 恭喜！你打破了历史最高分！</p>
                <p>你答对了 {{ correctCount }} 道题！</p>
                <div class="result-btn-group">
                  <button @click="confirm(); handleProgressClick()">再来一遍</button>
                  <button @click="showDetailModal = true" style="margin-left: 20px;">答题详情</button>
                </div>
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
              <!-- 对句题 -->
              <template v-if="currentQuestion.type === 'duiju'">
                <p>{{ currentIndex + 1 }}. {{ currentQuestion.question }}</p>
                <div class="duiju-answer-box">
                  <span v-for="(char, idx) in duijuUserAnswer" :key="idx" class="duiju-answer-char">{{ char }}</span>
                  <span v-for="i in duijuAnswerLength - duijuUserAnswer.length" :key="'empty' + i"
                    class="duiju-answer-char empty"></span>
                </div>
                <div class="duiju-options">
                  <div class="duiju-row" v-for="row in 2" :key="row">
                    <button v-for="col in 8" :key="(row - 1) * 8 + (col - 1)"
                      :disabled="duijuSelectedIdx.includes((row - 1) * 8 + (col - 1))"
                      @click="selectDuijuChar((row - 1) * 8 + (col - 1))" class="duiju-option-btn">
                      {{ currentQuestion.duijuOptions[(row - 1) * 8 + (col - 1)] }}
                    </button>
                  </div>
                </div>
                <div class="duiju-actions">
                  <button @click="submitDuijuAnswer"
                    :disabled="duijuUserAnswer.length !== duijuAnswerLength">提交</button>
                  <button @click="resetDuijuAnswer">重选</button>
                </div>
              </template>
              <!-- 字词识诗题 -->
              <template v-else-if="currentQuestion.type === 'zici'">
                <p>{{ currentIndex + 1 }}. {{ currentQuestion.question }}</p>
                <div class="zici-answer-box">
                  <span v-for="(char, idx) in ziciUserAnswer" :key="idx" class="zici-answer-char">{{ char }}</span>
                  <span v-for="i in ziciAnswerLength - ziciUserAnswer.length" :key="'empty' + i"
                    class="zici-answer-char empty"></span>
                </div>
                <div class="zici-options">
                  <div class="zici-row" v-for="row in 2" :key="row">
                    <button v-for="col in 6" :key="(row - 1) * 6 + (col - 1)"
                      :disabled="ziciSelectedIdx.includes((row - 1) * 6 + (col - 1))"
                      @click="selectZiciChar((row - 1) * 6 + (col - 1))" class="zici-option-btn">
                      {{ currentQuestion.ziciOptions[(row - 1) * 6 + (col - 1)] }}
                    </button>
                  </div>
                </div>
                <div class="zici-actions">
                  <button @click="submitZiciAnswer" :disabled="ziciUserAnswer.length !== ziciAnswerLength">提交</button>
                  <button @click="resetZiciAnswer">重选</button>
                </div>
              </template>
              <!-- 普通题 -->
              <template v-else>
                <p>{{ currentIndex + 1 }}. {{ currentQuestion.question }}</p>
                <div class="option">
                  <button v-for="(text, key) in currentQuestion.options" :key="key"
                    :class="buttonState(optionIdPrefix(currentQuestion.id, key))"
                    @click="handleOptionClick(currentQuestion.id, key)" :disabled="showResultsFlag || isAnswered">
                    {{ optLabel(key) }}.{{ text }}
                  </button>
                </div>
              </template>
            </div>
          </transition>
        </div>

        <div class="progress-container" v-if="!showStartScreen">
          <div class="timer-box">
            ⏱️ 用时：{{ formatTime(elapsedSeconds) }}
          </div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fg" :style="{ width: progressPercentage + '%' }"></div>
            <img src="../assets/image/imgtest/brush.png" alt="毛笔" class="brush"
              :style="{ left: progressPercentage + '%' }" />
          </div>
          <div class="progress-text"> 第{{ currentIndex + 1 }} / {{ totalQuestions }} 题</div>
        </div>

      </div>
    </div>


    <!-- 答题详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay">
      <div class="modal detail-modal">
        <h2>答题详情</h2>
        <div class="detail-list">
          <div v-for="(record, idx) in answerRecords" :key="idx" class="detail-item">
            <div class="detail-q">
              {{ idx + 1 }}. {{ record.question }}
            </div>
            <div class="detail-options">
              <template v-if="record.type === 'duiju'">
                <span class="detail-opt" :class="record.isCorrect ? 'user-correct' : 'user-wrong'">
                  你的答案：{{ record.userAnswer }}
                </span>
                <span class="detail-opt correct">
                  正确答案：{{ record.correctAnswer }}
                </span>
              </template>
              <template v-else-if="record.type === 'zici'">
                <span class="detail-opt" :class="record.isCorrect ? 'user-correct' : 'user-wrong'">
                  你的答案：{{ record.userAnswer }}
                </span>
                <span class="detail-opt correct">
                  正确答案：{{ record.correctAnswer }}
                </span>
              </template>
              <template v-else>
                <span v-for="(text, key) in record.options" :key="key" :class="[
                  'detail-opt',
                  key === record.correctAnswer ? 'correct' : '',
                  key === record.userAnswer ? (record.isCorrect ? 'user-correct' : 'user-wrong') : ''
                ]">
                  {{ optLabel(key) }}.{{ text }}
                </span>
              </template>
            </div>
            <div class="detail-result">
              <span v-if="record.isCorrect" style="color: #2ecc40;">✔ 答对</span>
              <span v-else style="color: #e74c3c;">✘ 答错</span>
            </div>
          </div>
        </div>
        <button @click="showDetailModal = false" style="margin-top: 20px;">关闭</button>
      </div>
    </div>

    <!-- 新增排行榜弹窗 -->
    <div v-if="showRankModal" class="modal-overlay">
      <div class="modal rank-modal">
        <h2>🏆排行榜<br>（{{ difficultyLabel(selectedDifficulty) }}，{{ selectedQuestionCount }}题）</h2>
        <div v-if="rankLoading" style="margin: 20px;">加载中...</div>
        <div v-else>
          <div class="rank-table-scroll">
            <table class="rank-table">
              <thead>
                <tr>
                  <th>排名</th>
                  <th>用户名</th>
                  <th>答题时间</th>
                  <th>最高分</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, idx) in rankList" :key="item.UID"
                  :class="['rank-row', { 'first': idx === 0, 'second': idx === 1, 'third': idx === 2 }]">
                  <td>
                    <span v-if="idx === 0">🏅</span>
                    <span v-else-if="idx === 1">🥈</span>
                    <span v-else-if="idx === 2">🥉</span>
                    <span v-else>{{ idx + 1 }}</span>
                  </td>
                  <td>{{ item.UserName }}</td>
                  <td>{{ formatTime(item.Mintime) }}</td>
                  <td>{{ item.Max }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="rankList.length === 0" style="margin: 20px;">暂无数据</div>
          <!-- 底部显示当前用户排名 -->
          <div v-if="myRankInfo" class="my-rank-info">
            <span>我的排名：</span>
            <span class="my-rank">{{ myRankInfo.rank }}</span>
            <span class="my-name">{{ myRankInfo.UserName }}</span>
            <span class="my-time" v-if="myRankInfo.Mintime">{{ formatTime(myRankInfo.Mintime) }}</span>
            <span class="my-score">{{ myRankInfo.Max }}</span>
          </div>
        </div>
        <button @click="showRankModal = false" style="margin-top: 20px;">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import API_BASE_URL from '@/config/api';
export default {
  name: "mtApp",
  data() {
    return {
      showStartScreen: true,
      // 修改：默认难度为数字
      selectedDifficulty: 1,
      selectedQuestionCount: 10,
      // 修改：难度选项为数字
      difficultyOptions: [
        { label: '简单', value: 1 },
        { label: '普通', value: 2 },
        { label: '困难', value: 3 }
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
      },
      answerRecords: [], // 新增：记录每题作答情况
      showDetailModal: false,
      duijuUserAnswer: [],
      duijuSelectedIdx: [],
      ziciUserAnswer: [],
      ziciSelectedIdx: [],
      showRankModal: false,
      rankList: [],
      rankLoading: false,
      myRankInfo: null, // 新增：我的排名信息
      isNewRecord: false, // 新增：是否破纪录
      timer: null,         // 计时器对象
      startTime: null,     // 开始时间
      elapsedSeconds: 0,   // 已用秒数
    };
  },

  computed: {
    progressPercentage() {
      return ((this.currentIndex + (this.isAnswered ? 1 : 0)) / this.totalQuestions) * 100;
    },
    totalQuestions() {
      return this.questions.length;
    },
    currentQuestion() {
      return this.questions[this.currentIndex] || null;
    },
    duijuAnswerLength() {
      return this.currentQuestion && this.currentQuestion.type === 'duiju'
        ? this.currentQuestion.duijuAnswer.length
        : 0;
    },
    ziciAnswerLength() {
      return this.currentQuestion && this.currentQuestion.type === 'zici'
        ? this.currentQuestion.ziciAnswer.length
        : 0;
    },
  },
  mounted() {
    fetch('/questions.json')
      .then(res => res.json())
      .then(data => {
        this.allQuestions = data;
        this.questions = this.shuffleArray(data).slice(0, 10);

        for (let q of this.questions) {
          if (q.type !== 'duiju' && q.type !== 'zici') {
            const correctId = this.optionIdPrefix(q.id, q.answer[0]);
            this.correctAnswers[correctId] = true;
          }
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
      const isCorrect = btnId === correctId;

      // 记录作答
      this.answerRecords.push({
        question: this.currentQuestion.question,
        options: this.currentQuestion.options,
        userAnswer: optKey,
        correctAnswer: this.currentQuestion.answer[0],
        isCorrect,
      });

      if (isCorrect) {
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
      this.resetDuijuAnswer && this.resetDuijuAnswer();
      this.resetZiciAnswer && this.resetZiciAnswer();
      if (this.currentIndex < this.questions.length - 1) {
        this.currentIndex++;
        this.isAnswered = false;
      } else {
        this.showResultsFlag = true;
        this.isResultsVisible = true;
        this.isModalVisible = true;
        if (this.timer) clearInterval(this.timer);
        this.submitRecordToBackend();
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
      this.showStartScreen = true; // 新增：返回选择界面
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
      this.answerRecords = []; // 新增

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

      this.elapsedSeconds = 0;
      this.startTime = Date.now();
      if (this.timer) clearInterval(this.timer);
      this.timer = setInterval(() => {
        this.elapsedSeconds = Math.floor((Date.now() - this.startTime) / 1000);
      }, 1000);

      // 单独筛选对句题和字词题
      const duijuQuestions = this.allQuestions.filter(q => q.type === 'duiju');
      const ziciQuestions = this.allQuestions.filter(q => q.type === 'zici');
      const nonDuijuZiciQuestions = this.allQuestions.filter(q => !q.type || (q.type !== 'duiju' && q.type !== 'zici'));

      // 随机抽取1~3个对句题
      let duijuCount = Math.min(duijuQuestions.length, Math.max(1, Math.floor(Math.random() * 3) + 1));
      duijuCount = Math.min(duijuCount, count);

      // 字词题数量
      let ziciCount = 0;
      // 修改：难度判断为数字
      if (this.selectedDifficulty === 1) {
        ziciCount = 0;
      } else if (this.selectedDifficulty === 2) {
        ziciCount = Math.min(ziciQuestions.length, Math.floor(Math.random() * 2) + 1); // 1~2题
      } else if (this.selectedDifficulty === 3) {
        ziciCount = Math.min(ziciQuestions.length, Math.floor(Math.random() * 2) + 1); // 1~2题
      }
      ziciCount = Math.min(ziciCount, count - duijuCount);

      const selectedDuiju = this.shuffleArray(duijuQuestions).slice(0, duijuCount);
      const selectedZici = this.shuffleArray(ziciQuestions).slice(0, ziciCount);

      // 其余题目按难度筛选
      let selectedOthers = [];
      const remainCount = count - duijuCount - ziciCount;
      // 只选没有 type 字段的选择题
      const choiceQuestions = nonDuijuZiciQuestions.filter(q => !q.type);

      // 这里如果你想用 id 判断难度，可以这样（假设 0001~0049 为简单，0050 及以上为困难）
      const easyQuestions = choiceQuestions.filter(q => q.id < "0050");
      const hardQuestions = choiceQuestions.filter(q => q.id >= "0050");

      // 修改：难度判断为数字
      if (this.selectedDifficulty === 1) {
        selectedOthers = this.shuffleArray(easyQuestions).slice(0, remainCount);
      } else if (this.selectedDifficulty === 2) {
        const half = Math.floor(remainCount / 2);
        const normalEasyPart = this.shuffleArray(easyQuestions).slice(0, half);
        const normalHardPart = this.shuffleArray(hardQuestions).slice(0, remainCount - half);
        selectedOthers = [...normalEasyPart, ...normalHardPart];
        selectedOthers = this.shuffleArray(selectedOthers);
      } else if (this.selectedDifficulty === 3) {
        selectedOthers = this.shuffleArray(hardQuestions).slice(0, remainCount);
      }

      // 合并并打乱
      let selectedQuestions = this.shuffleArray([...selectedDuiju, ...selectedZici, ...selectedOthers]);

      this.questions = selectedQuestions;

      // 初始化正确答案映射
      this.correctAnswers = {};
      for (let q of this.questions) {
        if (q.type !== 'duiju' && q.type !== 'zici') {
          const correctId = this.optionIdPrefix(q.id, q.answer[0]);
          this.correctAnswers[correctId] = true;
        }
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
    // 对句题相关
    selectDuijuChar(idx) {
      if (
        this.currentQuestion &&
        this.currentQuestion.type === 'duiju' &&
        this.duijuUserAnswer.length < this.duijuAnswerLength
      ) {
        this.duijuUserAnswer.push(this.currentQuestion.duijuOptions[idx]);
        this.duijuSelectedIdx.push(idx);
      }
    },
    resetDuijuAnswer() {
      this.duijuUserAnswer = [];
      this.duijuSelectedIdx = [];
    },
    submitDuijuAnswer() {
      const userStr = this.duijuUserAnswer.join('');
      const correctStr = this.currentQuestion.duijuAnswer;
      const isCorrect = userStr === correctStr;

      // 记录作答
      this.answerRecords.push({
        question: this.currentQuestion.question,
        options: {}, // 对句题无选项
        userAnswer: userStr,
        correctAnswer: correctStr,
        isCorrect,
        type: 'duiju'
      });


      if (isCorrect) {
        this.correctCount++;
        this.isAnswered = true;
        setTimeout(() => {
          this.nextQuestion();
          this.resetDuijuAnswer();
        }, 800);
      } else {
        this.modalContent = {
          title: "答错啦",
          message: `正确对句是：${correctStr}`,
          explanation: ''
        };
        this.isModalVisible = true;
      }
    },
    // 字词识诗题相关
    selectZiciChar(idx) {
      if (
        this.currentQuestion &&
        this.currentQuestion.type === 'zici' &&
        this.ziciUserAnswer.length < this.ziciAnswerLength
      ) {
        this.ziciUserAnswer.push(this.currentQuestion.ziciOptions[idx]);
        this.ziciSelectedIdx.push(idx);
      }
    },
    resetZiciAnswer() {
      this.ziciUserAnswer = [];
      this.ziciSelectedIdx = [];
    },
    submitZiciAnswer() {
      const userStr = this.ziciUserAnswer.join('');
      const correctStr = this.currentQuestion.ziciAnswer;
      const isCorrect = userStr === correctStr;

      // 记录作答
      this.answerRecords.push({
        question: this.currentQuestion.question,
        options: {}, // 字词题无选项
        userAnswer: userStr,
        correctAnswer: correctStr,
        isCorrect,
        type: 'zici'
      });

      if (isCorrect) {
        this.correctCount++;
        this.isAnswered = true;
        setTimeout(() => {
          this.nextQuestion();
          this.resetZiciAnswer();
        }, 800);
      } else {
        this.modalContent = {
          title: "答错啦",
          message: `正确诗句是：${correctStr}`,
          explanation: ''
        };
        this.isModalVisible = true;
      }
    },
    async getUserName(uid) {
      try {
        const response = await fetch(`${API_BASE_URL}/user/loginName/${uid}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        });
        if (response.ok) {
          const name = await response.text();
          return name;
        }
        return null;
      } catch (error) {
        console.error('获取用户名失败:', error);
        return null;
      }
    },
    async handleRankClick() {
      const username = localStorage.getItem('username');
      if (!username) {
        alert('请先登录才能查看排行榜！');
        return;
      }
      this.showRankModal = true;
      this.rankLoading = true;
      this.rankList = [];
      this.myRankInfo = null; // 重置
      try {
        // 获取当前难度和题数
        const difficulty = this.selectedDifficulty;
        const sum = this.selectedQuestionCount;
        // 获取当前用户UID
        let uid = null;
        try {
          const res = await fetch(`${API_BASE_URL}/user/loginID/${username}`);
          uid = await res.text();
        } catch (e) {
          uid = null;
        }
        // 请求排行榜
        const resp = await fetch('${API_BASE_URL}/compRec/rank', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            UID: uid || 0,
            Difficulty: difficulty,
            Sum: sum,
          }),
        });
        const data = await resp.json();

        // 批量获取用户名
        const uidList = data.map(item => item.UID);
        const namePromises = uidList.map(uid => this.getUserName(uid));
        const nameList = await Promise.all(namePromises);

        // 合并用户名到排行榜数据
        this.rankList = data.map((item, idx) => ({
          ...item,
          UserName: nameList[idx] || `UID:${item.UID}`
        }));

        // 查找我的排名
        if (uid) {
          let myRank = null;
          for (let i = 0; i < this.rankList.length; i++) {
            if (String(this.rankList[i].UID) === String(uid)) {
              myRank = {
                rank: i + 1,
                ...this.rankList[i]
              };
              break;
            }
          }
          // 如果没在榜单，显示未上榜
          if (!myRank) {
            // 也查一下自己的用户名
            const myName = await this.getUserName(uid);
            this.myRankInfo = {
              rank: '未上榜',
              UserName: myName || username,
              Max: '-'
            };
          } else {
            this.myRankInfo = myRank;
          }
        }
      } catch (e) {
        alert('排行榜加载失败，请稍后重试');
      } finally {
        this.rankLoading = false;
      }
    },
    // 修改：难度数字转中文
    difficultyLabel(val) {
      if (val === 1) return '简单';
      if (val === 2) return '普通';
      if (val === 3) return '困难';
      return val;
    },
    async submitRecordToBackend() {
      const username = localStorage.getItem('username');
      if (!username) return;
      let uid = null;
      try {
        const res = await fetch(`${API_BASE_URL}/user/loginID/${username}`);
        uid = await res.text();
      } catch (e) {
        uid = null;
      }
      // 查询历史最高分
      let oldMax = 0;
      try {
        const resp = await fetch('${API_BASE_URL}/compRec/rank', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            UID: uid || 0,
            Difficulty: this.selectedDifficulty,
            Sum: this.selectedQuestionCount,
          }),
        });
        const data = await resp.json();
        // 找到自己
        const my = data.find(item => String(item.UID) === String(uid));
        if (my) oldMax = Number(my.Max);
      } catch (e) {
        oldMax = 0;
      }
      // 判断是否破纪录
      this.isNewRecord = this.correctCount > oldMax;


      // 提交成绩
      const record = {
        UID: uid || 0,
        Difficulty: this.selectedDifficulty,
        Sum: this.selectedQuestionCount,
        Max: this.correctCount,
        Mintime: this.elapsedSeconds
      };
      console.log(record);
      try {
        await fetch('${API_BASE_URL}/compRec/submit', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(record)
        });
      } catch (e) {
        // 可选：错误处理
        console.error('成绩提交失败', e);
      }
    },
    formatTime(seconds) {
      if (!seconds || isNaN(seconds)) return '--';
      const min = Math.floor(seconds / 60);
      const sec = seconds % 60;
      return `${min}′${sec < 10 ? '0' : ''}${sec}″`;
    },
  }
}
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
  width: 450px;
  height: 300px;
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
  bottom: 80px;
  animation: fadeIn 1s ease-out;
}

/* 右侧图片 */
.right-image {
  width: 120px;
  height: auto;
  position: absolute;
  right: 5px;
  bottom: 85px;
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
    margin-top: 30px;
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
  margin: 0px auto;
  width: 80%;
  position: relative;
}

.progress-bar-bg {
  height: 20px;
  background: url('./src/assets/image/imgtest/scroll-bg.png') repeat-x;
  border-radius: 15px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.3);
  position: relative;
  margin: 17px 0px;
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

.detail-modal {
  width: 90vw;
  max-width: 900px;
  height: 80vh;
  max-height: 90vh;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f4e9c1, #f5ecc7);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
  animation: slideIn 0.4s ease-out forwards;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}

.detail-modal h2 {
  font-size: 24px;
  font-weight: 500;
  color: #333;
  margin-bottom: 1.5rem;
  position: relative;
  display: inline-block;
}

.detail-modal h2::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 2px;
  bottom: -5px;
  left: 0;
  background: linear-gradient(to right, transparent, #8c7853, transparent);
}



.detail-item {
  margin-bottom: 1.2rem;
  padding: 10px;
  border-radius: 8px;
  background: rgba(245, 245, 245, 0.8);
  position: relative;
  overflow: hidden;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-q {
  font-size: 18px;
  color: #5a4634;
  margin-bottom: 0.5rem;
}

.detail-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 4px;
  justify-content: center;
  /* 新增：让选项居中 */
  width: 100%;
  /* 新增：让选项区域占满父容器宽度 */
}

.detail-opt {
  padding: 8px 12px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.7);
  color: #5a4634;
  font-weight: 500;
  transition: background 0.3s, transform 0.3s;
}

.detail-opt.correct {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.detail-opt.user-correct {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
  transform: scale(1.05);
}

.detail-opt.user-wrong {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
  transform: scale(1.05);
}

.detail-result {
  font-size: 16px;
  font-weight: 500;
  position: absolute;
  top: 10px;
  right: 10px;
}

.detail-result span {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.detail-result span:first-child {
  margin-right: 5px;
}

.detail-result span.correct {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.detail-result span.wrong {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
}

/* 滑入动画 */
@keyframes slideIn {
  0% {
    opacity: 0;
    transform: translateY(-10px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}


.detail-list {
  max-height: 60vh;
  overflow-y: auto;
  width: 100%;
  margin-bottom: 1.5rem;
}

.detail-item {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  margin-bottom: 18px;
  padding: 12px 10px;
  box-shadow: 0 2px 8px rgba(31, 38, 135, 0.08);
}

.detail-q {
  font-size: 18px;
  color: #5a4634;
  margin-bottom: 6px;
  font-weight: 600;
}

.detail-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 4px;
}

.detail-opt {
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 15px;
  border: 1px solid #e5d8b8;
  background: #f8f4ed;
  color: #5a4634;
}

.detail-opt.correct {
  border: 2px solid #2ecc40;
  background: #eafbe7;
  font-weight: bold;
}

.detail-opt.user-correct {
  border: 2px solid #2ecc40;
  background: #eafbe7;
  color: #2ecc40;
}

.detail-opt.user-wrong {
  border: 2px solid #e74c3c;
  background: #fbeaea;
  color: #e74c3c;
}

.detail-result {
  margin-top: 2px;
  font-size: 14px;
  font-weight: 600;
}

.detail-modal button {
  margin-top: 20px;
  font-size: 18px;
  padding: 12px 40px;
}

.result-btn-group {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 10px;
}

.duiju-answer-box {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
  min-height: 40px;
}

.duiju-answer-char {
  display: inline-block;
  width: 36px;
  height: 36px;
  border-bottom: 2px solid #a17f61;
  font-size: 22px;
  margin: 0 2px;
  text-align: center;
  line-height: 36px;
  background: #fffbe8;
  border-radius: 6px;
}

.duiju-answer-char.empty {
  background: transparent;
  border-bottom: 2px dashed #c9c19e;
}

.duiju-options {
  margin: 10px 0;
}

.duiju-row {
  display: flex;
  justify-content: flex-start;
  /* 或 center */
  align-items: center;
  gap: 10px !important;
  /* 禁用gap */
  padding: 10px;
}

.duiju-option-btn {
  min-width: 28px;
  height: 36px;
  margin: 15px 20px !important;
  /* 强制缩小间距 */
  font-size: 22px !important;
  background: #f6df8e;
  border: 1.5px solid #a17f61;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  padding: 0 2px !important;
  /* 缩小内边距 */
  box-sizing: border-box;
  line-height: 36px;
  display: inline-block;
  vertical-align: middle;
}

.duiju-option-btn:disabled {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
}

.duiju-actions {
  margin-top: 16px;
}

.duiju-actions button {
  margin: 10px 100px;
  font-size: 16px;
  padding: 8px 0px;
  width: 200px;
  transition: transform 0.1s ease, box-shadow 0.1s ease;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

/* 按下时缩小 + 变色 + 收起阴影 */
.duiju-actions button:active {
  transform: scale(0.96);
  box-shadow: 0 2px 3px rgba(0, 0, 0, 0.08);
  background-color: #e0e0e0;
}

/* 字词识诗题样式 */
.zici-answer-box {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
  min-height: 40px;
}

.zici-answer-char {
  display: inline-block;
  width: 36px;
  height: 36px;
  border-bottom: 2px solid #a17f61;
  font-size: 22px;
  margin: 0 2px;
  text-align: center;
  line-height: 36px;
  background: #fffbe8;
  border-radius: 6px;
}

.zici-answer-char.empty {
  background: transparent;
  border-bottom: 2px dashed #c9c19e;
}

.zici-options {
  margin: 10px 0;
}

.zici-row {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 10px !important;
  padding: 10px;
}

.zici-option-btn {
  min-width: 28px;
  height: 36px;
  margin: 15px 20px !important;
  font-size: 22px !important;
  background: #f6df8e;
  border: 1.5px solid #a17f61;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  padding: 0 2px !important;
  box-sizing: border-box;
  line-height: 36px;
  display: inline-block;
  vertical-align: middle;
}

.zici-option-btn:disabled {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
}

.zici-actions {
  margin-top: 16px;
}

.zici-actions button {
  margin: 10px 100px;
  font-size: 16px;
  padding: 8px 0px;
  width: 200px;
  transition: transform 0.1s ease, box-shadow 0.1s ease;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.zici-actions button:active {
  transform: scale(0.96);
  box-shadow: 0 2px 3px rgba(0, 0, 0, 0.08);
  background-color: #e0e0e0;
}

.rank-btn {
  position: absolute;
  top: 170px;
  right: 30px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 24px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: background 0.3s;
  z-index: 10;
}

.rank-btn:hover {
  background: linear-gradient(to right, #a3916a, #7c6488);
}

.rank-modal {
  width: 50vw;
  height: 85vh;
  padding: 40px 50px 30px 50px;
  background: linear-gradient(60deg, #ead177 60%, #c9c19e 100%);
  border-radius: 22px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.22);
  text-align: center;
  position: relative;
  margin: 0 auto;
  /* 新增大气感 */
  display: flex;
  flex-direction: column;
  align-items: center;
}

.rank-table {
  width: 40vw;
  border-collapse: separate;
  border-spacing: 0 10px;
  margin: 0px 0 10px 0;
  background: #fffbe8;
  border-radius: 12px;
  overflow: hidden;
  font-size: 20px;
  box-shadow: 0 2px 12px rgba(31, 38, 135, 0.08);
}

.rank-table th,
.rank-table td {
  padding: 14px 0;
  font-size: 20px;
  border-bottom: 1.5px solid #e0d9c7;
  text-align: center;
  font-weight: 600;
}

.rank-table th {
  background: #f6df8e;
  color: #6e5773;
  font-weight: bold;
  font-size: 22px;
  border-bottom: 2.5px solid #cfae74;
}

.rank-table tr:last-child td {
  border-bottom: none;
}

.rank-row.first {
  background: linear-gradient(90deg, #fffbe8 60%, #ffe9b3 100%);
  color: #d4af37;
  font-weight: bold;
  font-size: 22px;
  box-shadow: 0 2px 12px rgba(212, 175, 55, 0.08);
}

.rank-row.second {
  background: linear-gradient(90deg, #fffbe8 60%, #e6e6e6 100%);
  color: #b4b4b4;
  font-weight: bold;
  font-size: 21px;
}

.rank-row.third {
  background: linear-gradient(90deg, #fffbe8 60%, #ffd6b3 100%);
  color: #c97a3a;
  font-weight: bold;
  font-size: 20px;
}

.my-rank-info {
  margin-top: 24px;
  padding: 12px 32px;
  border-radius: 18px;
  background: linear-gradient(90deg, #fffbe8 60%, #f6df8e 100%);
  box-shadow: 0 4px 18px rgba(212, 175, 55, 0.10);
  font-size: 22px;
  color: #6e5773;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 28px;
  font-weight: 700;
  border: 2px solid #e7d7a7;
  position: relative;
  min-width: 340px;
  max-width: 480px;
  margin-left: auto;
  margin-right: auto;
}

.my-rank-info::before {
  font-size: 28px;
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.85;
}

.my-rank {
  color: #d4af37;
  font-size: 26px;
  margin: 0 8px;
  font-weight: 900;
  letter-spacing: 2px;
}

.my-name {
  color: #6e5773;
  font-size: 22px;
  margin: 0 8px;
  font-weight: 700;
}

.my-time {
  color: #4caf50;
  font-size: 20px;
  margin: 0 8px;
  font-weight: 600;
  background: #eafbe7;
  border-radius: 8px;
  padding: 2px 10px;
}

.my-score {
  color: #c97a3a;
  font-size: 22px;
  margin: 0 8px;
  font-weight: 700;
  background: #fff2e0;
  border-radius: 8px;
  padding: 2px 10px;
  border: 1.5px solid #f6df8e;
}

.timer-box {
  margin-top: 10px;
  display: inline-block;
  background: rgba(255, 255, 255, 0.7);
  color: #6e5773;
  font-size: 18px;
  font-weight: bold;
  border-radius: 16px;
  padding: 6px 22px;
  box-shadow: 0 2px 8px rgba(31, 38, 135, 0.08);
  letter-spacing: 1px;
}
.rank-table-scroll {
  max-height: 560px; /* 7行，每行约60px，可根据实际行高微调 */
  overflow-y: auto;
  width: 100%;
  margin-bottom: 10px;
}
</style>