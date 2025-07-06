<template>
  <Teleport to="body">
    <div v-if="show" class="result-modal-overlay" @click="closeModal">
      <div class="result-modal" @click.stop>
        <!-- 🔧 模态框头部 -->
        <div class="modal-header">
          <h3 class="modal-title">
            <i class="icon-trophy"></i>
            测试结果
          </h3>
          <button class="close-btn" @click="closeModal">
            <i class="icon-times"></i>
          </button>
        </div>
        
        <!-- 🔧 结果内容 -->
        <div class="modal-body">
          <!-- 总体成绩 -->
          <div class="result-overview">
            <div class="score-circle">
              <div class="score-value">{{ correctCount }}/{{ totalQuestions }}</div>
              <div class="score-label">答对题数</div>
            </div>
            
            <div class="result-stats">
              <div class="stat-item">
                <div class="stat-label">正确率</div>
                <div class="stat-value">{{ accuracyRate }}%</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">用时</div>
                <div class="stat-value">{{ formatTime(elapsedSeconds) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">评级</div>
                <div class="stat-value" :class="gradeClass">{{ grade }}</div>
              </div>
            </div>
          </div>
          
          <!-- 🔧 破纪录提示 -->
          <div v-if="isNewRecord" class="new-record-badge">
            <i class="icon-star"></i>
            <span>恭喜！创造新纪录！</span>
          </div>
          
          <!-- 🔧 详细答题记录 -->
          <div class="answer-details">
            <h4 class="section-title">答题详情</h4>
            <div class="answer-list">
              <div 
                v-for="(record, index) in answerRecords" 
                :key="index"
                class="answer-item"
                :class="{ correct: record.isCorrect, wrong: !record.isCorrect }"
              >
                <div class="question-number">{{ index + 1 }}</div>
                <div class="question-content">
                  <div class="question-text">{{ record.question }}</div>
                  <div class="answer-info">
                    <div class="user-answer">
                      <span class="label">你的答案:</span>
                      <span class="answer">{{ getUserAnswerText(record) }}</span>
                    </div>
                    <div class="correct-answer">
                      <span class="label">正确答案:</span>
                      <span class="answer">{{ getCorrectAnswerText(record) }}</span>
                    </div>
                  </div>
                </div>
                <div class="result-icon">
                  <i :class="record.isCorrect ? 'icon-check' : 'icon-times'"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 🔧 模态框底部 -->
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="backToSelector">
            <i class="icon-arrow-left"></i>
            返回选择
          </button>
          <button class="btn btn-primary" @click="restartTest">
            <i class="icon-refresh"></i>
            重新测试
          </button>
          <button class="btn btn-info" @click="closeModal">
            <i class="icon-chart-bar"></i>
            查看统计
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
export default {
  name: 'ResultModal',
  
  // 🔧 明确定义 props
  props: {
    show: {
      type: Boolean,
      default: false
    },
    correctCount: {
      type: Number,
      default: 0
    },
    totalQuestions: {
      type: Number,
      default: 0
    },
    elapsedSeconds: {
      type: Number,
      default: 0
    },
    isNewRecord: {
      type: Boolean,
      default: false
    },
    answerRecords: {
      type: Array,
      default: () => []
    }
  },
  
  // 🔧 明确定义 emits
  emits: ['close', 'restart', 'back-to-selector'],
  
  computed: {
    accuracyRate() {
      if (this.totalQuestions === 0) return 0
      return Math.round((this.correctCount / this.totalQuestions) * 100)
    },
    
    grade() {
      const rate = this.accuracyRate
      if (rate >= 90) return '优秀'
      if (rate >= 80) return '良好'
      if (rate >= 70) return '及格'
      if (rate >= 60) return '一般'
      return '需努力'
    },
    
    gradeClass() {
      const rate = this.accuracyRate
      if (rate >= 90) return 'grade-excellent'
      if (rate >= 80) return 'grade-good'
      if (rate >= 70) return 'grade-pass'
      if (rate >= 60) return 'grade-fair'
      return 'grade-poor'
    }
  },
  
  watch: {
    show(newVal) {
      if (newVal) {
        document.body.style.overflow = 'hidden'
      } else {
        document.body.style.overflow = 'auto'
      }
    }
  },
  
  methods: {
    closeModal() {
      this.$emit('close')
    },
    
    restartTest() {
      this.$emit('restart')
    },
    
    backToSelector() {
      this.$emit('back-to-selector')
    },
    
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    
    getUserAnswerText(record) {
      if (!record.options || !record.userAnswer) return record.userAnswer || '未答'
      
      const optionText = record.options[record.userAnswer]
      return optionText ? `${record.userAnswer.toUpperCase()}. ${optionText}` : record.userAnswer
    },
    
    getCorrectAnswerText(record) {
      if (!record.options || !record.correctAnswer) return record.correctAnswer || '未知'
      
      const optionText = record.options[record.correctAnswer]
      return optionText ? `${record.correctAnswer.toUpperCase()}. ${optionText}` : record.correctAnswer
    }
  },
  
  beforeUnmount() {
    document.body.style.overflow = 'auto'
  }
}
</script>

<style lang="scss">
// 🔧 不使用 scoped，确保弹窗样式生效
.result-modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: rgba(0, 0, 0, 0.8) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 99999 !important;
  padding: 1rem;
  backdrop-filter: blur(5px);
  animation: fadeIn 0.3s ease-out;
}

.result-modal {
  width: 90vw;
  max-width: 1000px;
  max-height: 90vh;
  background: white !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3) !important;
  overflow: hidden;
  animation: slideIn 0.3s ease-out;
  position: relative !important;
  z-index: 100000 !important;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, 
    rgba(140, 120, 83, 0.1) 0%, 
    rgba(140, 120, 83, 0.05) 100%
  );
  
  .modal-title {
    font-family: 'KaiTi', 'STKaiti', serif;
    font-weight: 700;
    letter-spacing: 2px;
    font-size: 1.5rem;
    color: #ffffff;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    color: #666;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    
    &:hover {
      color: #8c7853;
      background: rgba(140, 120, 83, 0.1);
    }
  }
}

.modal-body {
  padding: 2rem;
  max-height: 60vh;
  overflow-y: auto;
}

.result-overview {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  align-items: center;
  
  .score-circle {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: linear-gradient(135deg, #8c7853, #6e5773);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
    
    .score-value {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 0.25rem;
    }
    
    .score-label {
      font-size: 0.9rem;
      opacity: 0.9;
    }
  }
  
  .result-stats {
    flex: 1;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 1rem;
    
    .stat-item {
      text-align: center;
      padding: 1rem;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 8px;
      border: 1px solid rgba(140, 120, 83, 0.2);
      
      .stat-label {
        font-size: 0.9rem;
        color: #666;
        margin-bottom: 0.5rem;
      }
      
      .stat-value {
        font-size: 1.2rem;
        font-weight: 600;
        color: #333;
        
        &.grade-excellent { color: #27ae60; }
        &.grade-good { color: #f39c12; }
        &.grade-pass { color: #3498db; }
        &.grade-fair { color: #e67e22; }
        &.grade-poor { color: #e74c3c; }
      }
    }
  }
}

.new-record-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem;
  background: linear-gradient(135deg, #f39c12, #e67e22);
  color: white;
  border-radius: 8px;
  margin-bottom: 2rem;
  font-weight: 600;
  
  .icon-star {
    font-size: 1.2rem;
  }
}

.answer-details {
  .section-title {
    font-family: 'KaiTi', 'STKaiti', serif;
    font-weight: 700;
    letter-spacing: 2px;
    font-size: 1.2rem;
    color: #8c7853;
    margin-bottom: 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    
    &::before {
      content: '';
      width: 4px;
      height: 20px;
      background: #8c7853;
      border-radius: 2px;
    }
  }
  
  .answer-list {
    display: grid;
    gap: 1rem;
    
    .answer-item {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      border-radius: 8px;
      border: 2px solid transparent;
      transition: all 0.3s ease;
      
      &.correct {
        background: rgba(39, 174, 96, 0.1);
        border-color: rgba(39, 174, 96, 0.3);
      }
      
      &.wrong {
        background: rgba(231, 76, 60, 0.1);
        border-color: rgba(231, 76, 60, 0.3);
      }
      
      .question-number {
        width: 30px;
        height: 30px;
        background: #8c7853;
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 0.9rem;
        flex-shrink: 0;
      }
      
      .question-content {
        flex: 1;
        
        .question-text {
          font-weight: 500;
          color: #333;
          margin-bottom: 0.5rem;
          line-height: 1.5;
        }
        
        .answer-info {
          display: grid;
          gap: 0.3rem;
          
          .user-answer,
          .correct-answer {
            display: flex;
            gap: 0.5rem;
            font-size: 0.9rem;
            
            .label {
              color: #666;
              min-width: 70px;
            }
            
            .answer {
              color: #333;
              font-weight: 500;
            }
          }
        }
      }
      
      .result-icon {
        display: flex;
        align-items: center;
        font-size: 1.5rem;
        
        .icon-check {
          color: #27ae60;
        }
        
        .icon-times {
          color: #e74c3c;
        }
      }
    }
  }
}

.modal-footer {
  display: flex;
  justify-content: center;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  background: rgba(248, 246, 240, 0.5);
  
  .btn {
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    text-decoration: none;
    min-width: 120px;
    justify-content: center;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }
    
    &.btn-primary {
      background: #8c7853;
      color: white;
      
      &:hover {
        background: #6e5773;
      }
    }
    
    &.btn-secondary {
      background: #6c757d;
      color: white;
      
      &:hover {
        background: #5a6268;
      }
    }
    
    &.btn-info {
      background: #17a2b8;
      color: white;
      
      &:hover {
        background: #138496;
      }
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@media (max-width: 768px) {
  .result-modal {
    width: 95vw;
    
    .modal-body {
      padding: 1rem;
    }
  }
  
  .result-overview {
    flex-direction: column;
    gap: 1rem;
    
    .result-stats {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  .modal-footer {
    flex-direction: column;
    
    .btn {
      width: 100%;
    }
  }
  
  .answer-item {
    flex-direction: column;
    gap: 0.5rem;
    
    .question-content {
      .answer-info {
        .user-answer,
        .correct-answer {
          flex-direction: column;
          gap: 0.2rem;
        }
      }
    }
  }
}
</style>