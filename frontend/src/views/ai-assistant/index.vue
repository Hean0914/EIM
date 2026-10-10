<template>
  <section class="assistant-page">
    <header class="assistant-hero">
      <div>
        <span class="assistant-kicker">LOCAL INTELLIGENCE</span>
        <h1>企业办公助手</h1>
        <p>基于你的当前权限，快速整理待办、计划、会议和审批信息。</p>
      </div>
      <el-button :icon="Delete" plain @click="clearConversation">清空对话</el-button>
    </header>

    <section class="assistant-shell">
      <div class="assistant-messages" aria-live="polite">
        <article
          v-for="(message, index) in messages"
          :key="`${message.kind || message.role}-${index}`"
          :class="['assistant-message', `assistant-message--${message.role}`, { 'is-error': message.kind === 'error' }]"
        >
          <div class="assistant-message__avatar">{{ message.role === 'assistant' ? 'AI' : userInitial }}</div>
          <div class="assistant-message__body">
            <span class="assistant-message__label">{{ message.role === 'assistant' ? '办公助手' : '你' }}</span>
            <p>{{ message.content }}</p>
            <div v-if="message.webSearchUsed && message.sources?.length" class="assistant-sources">
              <span class="assistant-sources__title">参考来源</span>
              <a
                v-for="source in message.sources"
                :key="source.url"
                class="assistant-source"
                :href="source.url"
                target="_blank"
                rel="noopener noreferrer"
              >
                <strong>{{ source.title }}</strong>
                <small>{{ source.snippet }}</small>
              </a>
            </div>
          </div>
        </article>
        <div v-if="loading" class="assistant-typing" aria-label="正在生成回答">
          <span></span><span></span><span></span>
          <em>正在整理工作信息</em>
        </div>
      </div>

      <div class="assistant-prompts">
        <button v-for="prompt in quickPrompts" :key="prompt" type="button" @click="sendPrompt(prompt)">
          {{ prompt }}
        </button>
      </div>

      <form class="assistant-composer" @submit.prevent="sendMessage">
        <el-input
          v-model="draft"
          type="textarea"
          :rows="3"
          maxlength="2000"
          show-word-limit
          resize="none"
          placeholder="输入你的问题，例如：我今天有哪些待办？"
          @keydown.enter.exact.prevent="sendMessage"
        />
        <div class="assistant-composer__footer">
          <span>回答基于当前权限内的业务数据，不会直接修改系统数据。</span>
          <el-button type="primary" :icon="Promotion" :loading="loading" :disabled="!canSend" native-type="submit">
            发送
          </el-button>
        </div>
      </form>
    </section>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Delete, Promotion } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chatWithAssistant } from '@/api/aiAssistant'
import { useUserStore } from '@/store/user'
import {
  canSendAssistantMessage,
  createAssistantErrorMessage,
  createWelcomeMessage
} from '@/utils/ai-assistant-view'

const userStore = useUserStore()
const userName = computed(() => userStore.user?.realName || userStore.user?.username || '')
const userInitial = computed(() => userName.value.slice(0, 1) || '你')
const draft = ref('')
const loading = ref(false)
const messages = ref([createWelcomeMessage(userName.value)])
const quickPrompts = [
  '我今天有哪些待办？',
  '总结我的近期计划。',
  '本周有哪些会议？',
  '我的审批申请有哪些？',
  '联网搜索 Spring AI 最新资料'
]

const canSend = computed(() => canSendAssistantMessage(draft.value, loading.value))

function sendPrompt(prompt) {
  draft.value = prompt
  void sendMessage()
}

async function sendMessage() {
  const message = draft.value.trim()
  if (!canSendAssistantMessage(message, loading.value)) {
    return
  }
  messages.value.push({ role: 'user', content: message })
  draft.value = ''
  loading.value = true
  try {
    const result = await chatWithAssistant(message)
    messages.value.push({
      role: 'assistant',
      content: result.data?.answer || '本地 AI 没有返回回答。',
      kind: 'answer',
      webSearchUsed: result.data?.webSearchUsed === true,
      sources: result.data?.sources || []
    })
  } catch (error) {
    messages.value.push(createAssistantErrorMessage(error))
    ElMessage.warning(error?.message || '本地 AI 暂时不可用')
  } finally {
    loading.value = false
  }
}

function clearConversation() {
  messages.value = [createWelcomeMessage(userName.value)]
  draft.value = ''
}
</script>

<style scoped>
.assistant-page {
  max-width: 1080px;
  margin: 0 auto;
}

.assistant-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 24px 4px 20px;
}

.assistant-kicker {
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.assistant-hero h1 {
  margin: 8px 0 0;
  color: #0f172a;
  font-size: 28px;
  letter-spacing: 0;
}

.assistant-hero p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.assistant-shell {
  overflow: hidden;
  background: #fff;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.assistant-messages {
  min-height: 440px;
  max-height: calc(100vh - 360px);
  padding: 28px;
  overflow-y: auto;
  background:
    linear-gradient(90deg, rgba(37, 99, 235, 0.04) 1px, transparent 1px) 0 0 / 28px 28px,
    #f8fafc;
}

.assistant-message {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 82%;
  margin-bottom: 22px;
}

.assistant-message--user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.assistant-message__avatar {
  display: grid;
  flex: 0 0 32px;
  place-items: center;
  width: 32px;
  height: 32px;
  color: #fff;
  background: #1d4ed8;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 700;
}

.assistant-message--user .assistant-message__avatar {
  color: #1e3a8a;
  background: #dbeafe;
}

.assistant-message__body {
  min-width: 0;
}

.assistant-message__label {
  display: block;
  margin: 1px 0 6px;
  color: #64748b;
  font-size: 12px;
}

.assistant-message--user .assistant-message__label {
  text-align: right;
}

.assistant-message__body p {
  margin: 0;
  padding: 12px 14px;
  color: #1e293b;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 4px 10px 10px;
  line-height: 1.7;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.assistant-message--user .assistant-message__body p {
  color: #fff;
  background: #1d4ed8;
  border-color: #1d4ed8;
  border-radius: 10px 4px 10px 10px;
}

.assistant-sources {
  display: grid;
  gap: 7px;
  margin-top: 10px;
  padding: 10px 12px;
  background: #f8fafc;
  border: 1px solid #dbe3ef;
  border-radius: 6px;
}

.assistant-sources__title {
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.assistant-source {
  display: grid;
  gap: 2px;
  color: #1d4ed8;
  text-decoration: none;
}

.assistant-source:hover strong {
  text-decoration: underline;
}

.assistant-source small {
  color: #64748b;
  line-height: 1.5;
}

.assistant-message.is-error .assistant-message__body p {
  color: #9a3412;
  background: #fff7ed;
  border-color: #fed7aa;
}

.assistant-typing {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #64748b;
  font-size: 12px;
}

.assistant-typing span {
  width: 6px;
  height: 6px;
  background: #60a5fa;
  border-radius: 50%;
  animation: assistant-pulse 1s infinite ease-in-out;
}

.assistant-typing span:nth-child(2) { animation-delay: 0.15s; }
.assistant-typing span:nth-child(3) { animation-delay: 0.3s; }
.assistant-typing em {
  margin-left: 5px;
  font-style: normal;
}

.assistant-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px 28px 0;
}

.assistant-prompts button {
  padding: 7px 11px;
  color: #1d4ed8;
  cursor: pointer;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
}

.assistant-prompts button:hover {
  background: #dbeafe;
}

.assistant-composer {
  padding: 16px 28px 24px;
}

.assistant-composer__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
}

.assistant-composer__footer span {
  color: #94a3b8;
  font-size: 12px;
}

@keyframes assistant-pulse {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

@media (max-width: 680px) {
  .assistant-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .assistant-messages {
    padding: 20px 16px;
  }

  .assistant-message {
    max-width: 94%;
  }

  .assistant-prompts,
  .assistant-composer {
    padding-left: 16px;
    padding-right: 16px;
  }

  .assistant-composer__footer {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
