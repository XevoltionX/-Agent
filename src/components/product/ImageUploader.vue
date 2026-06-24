<template>
  <div class="uploader">
    <div class="upload-grid">
      <!-- 已上传的图片 -->
      <div v-for="(img, i) in images" :key="i" class="upload-item">
        <img :src="img" class="upload-img" alt="" />
        <div class="upload-remove" @click="remove(i)">×</div>
      </div>

      <!-- 添加按钮 -->
      <div v-if="images.length < maxCount" class="upload-add" @click="triggerUpload">
        <van-icon name="plus" size="28" color="#c0c4cc" />
        <span class="add-text">添加图片</span>
        <span class="add-count">{{ images.length }}/{{ maxCount }}</span>
      </div>
    </div>
    <!-- 隐藏的file input -->
    <input ref="fileInput" type="file" accept="image/*" multiple style="display:none" @change="onFileChange" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { showToast } from 'vant'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  maxCount: { type: Number, default: 9 }
})

const emit = defineEmits(['update:modelValue'])
const fileInput = ref(null)

const images = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const triggerUpload = () => {
  fileInput.value?.click()
}

const onFileChange = (e) => {
  const files = Array.from(e.target.files || [])
  if (images.value.length + files.length > props.maxCount) {
    showToast(`最多上传${props.maxCount}张图片`)
    return
  }
  files.forEach(file => {
    // 8MB过滤
    if (file.size > 8 * 1024 * 1024) { showToast('图片不能超过8MB'); return }
    const reader = new FileReader()
    reader.onload = (ev) => {
      images.value = [...images.value, ev.target.result]
    }
    reader.readAsDataURL(file)
  })
  // 重置input以允许重复选择同一文件
  e.target.value = ''
}

const remove = (i) => {
  const arr = [...images.value]
  arr.splice(i, 1)
  images.value = arr
}
</script>


<style scoped>
.uploader {
  padding: var(--space-3);
}
.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-2);
}
.upload-item {
  position: relative;
  aspect-ratio: 3 / 4;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 1px solid #e5e7eb;
}
.upload-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.upload-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  background: rgba(0,0,0,0.5);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
  line-height: 1;
}
.upload-add {
  aspect-ratio: 3 / 4;
  border: 2px dashed #d4ede0;
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  background: var(--color-primary-lighter);
  transition: border-color 0.2s;
}
.upload-add:active {
  border-color: var(--color-primary);
}
.add-text {
  font-size: var(--font-xs);
  color: var(--text-hint);
}
.add-count {
  font-size: 10px;
  color: var(--text-hint);
}
</style>
