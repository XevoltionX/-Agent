<template>
  <div class="lead-item" @click="$emit('click')">
    <div class="lead-left">
      <span v-if="showTimeIcon" class="lead-time-icon">🕐</span>
      <span class="lead-time" :class="{ bigger: !showTimeIcon }">{{ formatTime(lead.created_at) }}</span>
    </div>
    <div class="lead-mid">
      <div class="lead-message">{{ lead.buyer_message }}</div>
      <div class="lead-product-title">{{ lead.product_title }}</div>
    </div>
    <div class="lead-right">
      <span v-if="showStatus" class="lead-status-txt" :class="lead.status">{{ lead.status === 'CONTACTED' ? '已联系' : '待联系' }}</span>
      <span class="lead-email"><i class="email-dot"></i>{{ showFullEmail ? lead.buyer_email : maskEmail(lead.buyer_email) }}</span>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  lead: { type: Object, required: true },
  showFullEmail: { type: Boolean, default: false },
  showStatus: { type: Boolean, default: false },
  showTimeIcon: { type: Boolean, default: false }
})

defineEmits(['click'])

const formatTime = (t) => {
  if (!t) return ''
  // "2026-06-17 10:30" → "06-17 10:30"
  const parts = t.split(' ')
  return (parts[0]?.slice(5) || '') + ' ' + (parts[1] || '')
}

const maskEmail = (email) => {
  if (!email) return '****@***.com'
  const atIndex = email.indexOf('@')
  if (atIndex <= 0) return '****@***.com'
  return email.charAt(0) + '****@' + email.slice(atIndex + 1)
}
</script>

<style scoped>
.lead-item {
  display: flex;
  gap: 30px;
  padding: var(--space-3) 0;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  font-size: var(--font-xs);
}
.lead-item:active {
  background: var(--bg-gray-light);
}
.lead-left {
  width: 62px;
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
  gap: 2px;
  padding-top: 1px;
}
.lead-time-icon {
  font-size: 10px;
  flex-shrink: 0;
  opacity: 0.4;
  line-height: 1.3;
}
.lead-time {
  white-space: nowrap;
  font-size: 10px;
  color: var(--text-hint);
  line-height: 1.3;
}
.lead-time.bigger {
  font-size: var(--font-sm);
}
.lead-mid {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}
.lead-message {
  font-size: 12px;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.lead-product-title {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.lead-right {
  width: auto;
  flex-shrink: 0;
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}
.lead-status-txt {
  font-size: 10px;
}
.lead-status-txt.PENDING {
  color: var(--text-hint);
}
.lead-status-txt.CONTACTED {
  color: #22c55e;
}
.lead-email {
  color: var(--text-primary);
  font-size: 10px;
  display: flex;
  align-items: center;
  gap: 3px;
}
.email-dot {
  display: inline-block;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--gradient-primary);
  flex-shrink: 0;
}
</style>
