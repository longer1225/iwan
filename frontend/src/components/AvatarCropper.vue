<template>
  <div v-if="visible" class="cropper-modal" @click.self="close">
    <div class="cropper-container">
      <div class="cropper-header">
        <span class="title">裁剪头像</span>
        <button class="close-btn" @click="close">×</button>
      </div>
      <div class="cropper-body">
        <div class="image-container">
          <img 
            ref="imageRef" 
            :src="imageSrc" 
            @load="onImageLoad"
            :style="imageStyle"
            draggable="false"
          />
          <div class="crop-area" :style="cropAreaStyle"></div>
          <div class="move-handle" @mousedown="startMove"></div>
        </div>
        <div class="preview-container">
          <div class="preview-title">预览</div>
          <div class="preview-box" :style="previewStyle"></div>
        </div>
      </div>
      <div class="cropper-footer">
        <input 
          type="range" 
          v-model="currentScale" 
          :min="minScale" 
          :max="maxScale" 
          step="0.1"
          class="zoom-slider"
        />
        <div class="zoom-label">{{ Math.round(currentScale * 100) }}%</div>
        <button class="btn-cancel" @click="close">取消</button>
        <button class="btn-confirm" @click="confirmCrop">确认裁剪</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visible: Boolean,
  imageSrc: String
})

const emit = defineEmits(['close', 'confirm'])

const imageRef = ref(null)
const currentScale = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const isDragging = ref(false)
const startPos = ref({ x: 0, y: 0 })
const startOffset = ref({ x: 0, y: 0 })
const imageWidth = ref(0)
const imageHeight = ref(0)
const minScale = ref(0.1)
const maxScale = ref(5)

const containerSize = 300
const cropSize = 200

const imageStyle = computed(() => {
  return {
    width: `${imageWidth.value * currentScale.value}px`,
    height: `${imageHeight.value * currentScale.value}px`,
    transform: `translate(${offsetX.value}px, ${offsetY.value}px)`
  }
})

const cropAreaStyle = computed(() => {
  const left = (containerSize - cropSize) / 2
  const top = (containerSize - cropSize) / 2
  return {
    width: `${cropSize}px`,
    height: `${cropSize}px`,
    left: `${left}px`,
    top: `${top}px`
  }
})

const previewStyle = computed(() => {
  // 裁剪区域在容器中的位置
  const cropAreaX = (containerSize - cropSize) / 2
  const cropAreaY = (containerSize - cropSize) / 2
  
  // 图片在裁剪区域左上角的起始位置
  const imageStartX = cropAreaX - offsetX.value
  const imageStartY = cropAreaY - offsetY.value
  
  // 计算背景图大小（需要覆盖整个预览框）
  const previewSize = 80
  const scaleToPreview = previewSize / cropSize
  
  return {
    backgroundImage: `url(${props.imageSrc})`,
    backgroundSize: `${imageWidth.value * currentScale.value * scaleToPreview}px ${imageHeight.value * currentScale.value * scaleToPreview}px`,
    backgroundPosition: `-${imageStartX * scaleToPreview}px -${imageStartY * scaleToPreview}px`
  }
})

const onImageLoad = () => {
  if (imageRef.value) {
    imageWidth.value = imageRef.value.naturalWidth
    imageHeight.value = imageRef.value.naturalHeight
    resetPosition()
  }
}

const resetPosition = () => {
  if (imageWidth.value <= 0 || imageHeight.value <= 0) return
  
  const scaleX = containerSize / imageWidth.value
  const scaleY = containerSize / imageHeight.value
  const fitScale = Math.min(scaleX, scaleY)
  
  const initialScale = Math.min(fitScale, 1.5)
  currentScale.value = initialScale
  
  const scaledWidth = imageWidth.value * initialScale
  const scaledHeight = imageHeight.value * initialScale
  
  offsetX.value = (containerSize - scaledWidth) / 2
  offsetY.value = (containerSize - scaledHeight) / 2
  
  minScale.value = Math.max(0.1, fitScale * 0.5)
  maxScale.value = 5
}

const startMove = (e) => {
  e.preventDefault()
  isDragging.value = true
  startPos.value = { x: e.clientX, y: e.clientY }
  startOffset.value = { x: offsetX.value, y: offsetY.value }
}

const handleMouseMove = (e) => {
  if (!isDragging.value) return
  
  const deltaX = e.clientX - startPos.value.x
  const deltaY = e.clientY - startPos.value.y
  
  offsetX.value = startOffset.value.x + deltaX
  offsetY.value = startOffset.value.y + deltaY
  
  const scaledWidth = imageWidth.value * currentScale.value
  const scaledHeight = imageHeight.value * currentScale.value
  
  const halfContainer = (containerSize - cropSize) / 2
  
  offsetX.value = Math.max(halfContainer - scaledWidth + cropSize, Math.min(halfContainer, offsetX.value))
  offsetY.value = Math.max(halfContainer - scaledHeight + cropSize, Math.min(halfContainer, offsetY.value))
}

const handleMouseUp = () => {
  isDragging.value = false
}

const confirmCrop = () => {
  const canvas = document.createElement('canvas')
  canvas.width = cropSize
  canvas.height = cropSize
  
  const ctx = canvas.getContext('2d')
  const img = new Image()
  img.crossOrigin = 'anonymous'
  
  img.onload = () => {
    const cropAreaX = (containerSize - cropSize) / 2
    const cropAreaY = (containerSize - cropSize) / 2
    
    const sourceX = (cropAreaX - offsetX.value) / currentScale.value
    const sourceY = (cropAreaY - offsetY.value) / currentScale.value
    const sourceSize = cropSize / currentScale.value
    
    ctx.drawImage(img, sourceX, sourceY, sourceSize, sourceSize, 0, 0, cropSize, cropSize)
    
    const croppedImage = canvas.toDataURL('image/png')
    emit('confirm', croppedImage)
  }
  
  img.src = props.imageSrc
}

const close = () => {
  emit('close')
}

watch(() => props.visible, (val) => {
  if (val && props.imageSrc) {
    setTimeout(() => {
      resetPosition()
    }, 50)
  }
})

watch(() => props.imageSrc, () => {
  if (props.visible) {
    setTimeout(() => {
      resetPosition()
    }, 50)
  }
})

onMounted(() => {
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped>
.cropper-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.cropper-container {
  background: white;
  border-radius: 12px;
  width: 420px;
  overflow: hidden;
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}

.close-btn:hover {
  background: #e0e0e0;
}

.cropper-body {
  display: flex;
  padding: 20px;
  gap: 20px;
}

.image-container {
  position: relative;
  width: 300px;
  height: 300px;
  background: #f5f5f5;
  overflow: hidden;
  border-radius: 8px;
}

.image-container img {
  position: absolute;
  top: 0;
  left: 0;
}

.crop-area {
  position: absolute;
  border: 2px solid #409eff;
  border-radius: 8px;
  background: rgba(64, 158, 255, 0.1);
  pointer-events: none;
  box-shadow: inset 0 0 0 9999px rgba(0, 0, 0, 0.5);
}

.move-handle {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  cursor: move;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.preview-title {
  font-size: 12px;
  color: #999;
}

.preview-box {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 2px solid #eee;
  background-size: cover;
  background-repeat: no-repeat;
}

.cropper-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.zoom-slider {
  flex: 1;
  max-width: 150px;
}

.zoom-label {
  font-size: 12px;
  color: #666;
  min-width: 50px;
  text-align: center;
}

.btn-cancel, .btn-confirm {
  padding: 8px 20px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-confirm {
  background: #409eff;
  color: white;
}

.btn-confirm:hover {
  background: #66b1ff;
}
</style>
