<template>
  <div class="tag-editor">
    <template v-for="(tag, i) in tags" :key="i">
      <div class="tag-chip" :class="{ editing: editingIndex === i }" @click="startEdit(i)">
        <template v-if="editingIndex === i">
          <input
            ref="editInput"
            v-model="editValue"
            class="tag-input"
            maxlength="20"
            @blur="finishEdit(i)"
            @keydown.enter="finishEdit(i)"
          />
        </template>
        <template v-else>
          <span>{{ tag }}</span>
        </template>
        <span class="tag-x" @click.stop="removeTag(i)">×</span>
      </div>
    </template>

    <!-- 添加标签 -->
    <div v-if="showAdd" class="tag-add" :class="{ editing: isAdding }" @click="startAdd">
      <template v-if="isAdding">
        <input
          ref="addInput"
          v-model="addValue"
          class="tag-input"
          placeholder="新标签"
          maxlength="20"
          @blur="finishAdd"
          @keydown.enter="finishAdd"
        />
      </template>
      <template v-else>
        <span class="add-placeholder">+ 添加标签</span>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])

const editingIndex = ref(-1)
const editValue = ref('')
const isAdding = ref(false)
const addValue = ref('')
const showAdd = ref(true)

const tags = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const startEdit = (i) => {
  editingIndex.value = i
  editValue.value = tags.value[i]
  nextTick(() => {
    const inputs = document.querySelectorAll('.tag-input')
    inputs[inputs.length - 1]?.focus()
  })
}

const finishEdit = (i) => {
  const val = editValue.value.trim()
  if (val) {
    const arr = [...tags.value]
    arr[i] = val
    tags.value = arr
    editingIndex.value = -1
  } else {
    removeTag(i)
    editingIndex.value = -1
  }
}

const removeTag = (i) => {
  const arr = [...tags.value]
  arr.splice(i, 1)
  tags.value = arr
}

const startAdd = () => {
  isAdding.value = true
  addValue.value = ''
  nextTick(() => {
    const inputs = document.querySelectorAll('.tag-input')
    inputs[inputs.length - 1]?.focus()
  })
}

const finishAdd = () => {
  const val = addValue.value.trim()
  if (val) {
    tags.value = [...tags.value, val]
  }
  isAdding.value = false
  addValue.value = ''
}
</script>

<script>
import { computed } from 'vue'
export default { name: 'TagEditor' }
</script>

<style scoped>
.tag-editor {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: var(--font-sm);
  color: var(--color-primary);
  background: var(--color-primary-light);
  padding: 3px 8px;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all 0.15s;
  border: 1.5px solid transparent;
}
.tag-chip.editing {
  background: var(--bg-white);
  border-color: var(--color-primary);
  padding: 2px 4px 2px 8px;
}
.tag-input {
  width: 60px;
  border: none;
  outline: none;
  font-size: var(--font-sm);
  color: var(--text-primary);
  background: transparent;
  font-family: inherit;
}
.tag-x {
  font-size: 14px;
  color: var(--color-primary);
  opacity: 0.6;
  padding: 0 2px;
}
.tag-x:hover { opacity: 1; }

.tag-add {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border: 1.5px dashed #c8e6d8;
  border-radius: var(--radius-full);
  font-size: var(--font-sm);
  color: var(--text-hint);
  cursor: pointer;
  transition: all 0.15s;
}
.tag-add.editing {
  border-color: var(--color-primary);
  background: var(--bg-white);
  padding: 3px 8px;
}
.add-placeholder {
  white-space: nowrap;
}
</style>
