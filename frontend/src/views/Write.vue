<template>
  <div class="write-container">
    <header class="header">
      <div class="header-left">
        <button class="back-btn" @click="goBack">
          <i class="el-icon-arrow-left"></i>
        </button>
        <span class="title">写文章</span>
      </div>
      <div class="header-right">
        <el-button @click="saveDraft">存草稿</el-button>
        <el-button type="primary" @click="submitArticle">发布</el-button>
      </div>
    </header>
    
    <main class="content">
      <div class="editor-area">
        <!-- 文章标题 -->
        <input 
          v-model="articleForm.title" 
          type="text" 
          class="title-input"
          placeholder="请输入文章标题"
        />
        
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="tool-group">
            <button class="tool-btn" @click="insertImage" title="插入图片">
              <i class="el-icon-picture-outline"></i>
            </button>
            <button class="tool-btn" @click="insertVideo" title="插入视频">
              <i class="el-icon-video-camera"></i>
            </button>
            <button class="tool-btn" @click="insertAudio" title="插入音频">
              <i class="el-icon-headphones"></i>
            </button>
          </div>
          <div class="tool-group">
            <button class="tool-btn" @click="formatText('bold')" title="加粗">
              <i class="el-icon-bold"></i>
            </button>
            <button class="tool-btn" @click="formatText('italic')" title="斜体">
              <i class="el-icon-italic"></i>
            </button>
            <button class="tool-btn" @click="formatText('underline')" title="下划线">
              <i class="el-icon-underline"></i>
            </button>
          </div>
          <div class="tool-group">
            <button class="tool-btn" @click="formatText('h1')" title="标题1">
              H1
            </button>
            <button class="tool-btn" @click="formatText('h2')" title="标题2">
              H2
            </button>
            <button class="tool-btn" @click="formatText('list')" title="列表">
              <i class="el-icon-list"></i>
            </button>
          </div>
        </div>
        
        <!-- 编辑器 -->
        <div class="editor-wrapper">
          <textarea 
            v-model="articleForm.content" 
            class="content-textarea"
            placeholder="开始撰写你的文章..."
            rows="20"
          ></textarea>
        </div>
        
        <!-- 多媒体预览 -->
        <div class="media-preview" v-if="mediaFiles.length > 0">
          <h3>已添加的媒体文件</h3>
          <div class="media-list">
            <div 
              v-for="(file, index) in mediaFiles" 
              :key="index" 
              class="media-item"
            >
              <div class="media-thumb" :class="file.type">
                <img v-if="file.type === 'image'" :src="file.url" alt="" />
                <i v-else-if="file.type === 'video'" class="el-icon-video-camera"></i>
                <i v-else class="el-icon-headphones"></i>
              </div>
              <span class="media-name">{{ file.name }}</span>
              <button class="remove-btn" @click="removeMedia(index)">
                <i class="el-icon-delete"></i>
              </button>
            </div>
          </div>
        </div>
        
        <!-- 文章摘要 -->
        <div class="summary-section">
          <label>文章摘要</label>
          <textarea 
            v-model="articleForm.summary" 
            class="summary-textarea"
            placeholder="简要描述你的文章（可选）"
            rows="3"
          ></textarea>
        </div>
        
        <!-- 分类和标签 -->
        <div class="meta-section">
          <div class="category-select">
            <label>分类</label>
            <el-select v-model="articleForm.categoryId" placeholder="选择分类">
              <el-option label="技术博客" value="1" />
              <el-option label="生活随笔" value="2" />
              <el-option label="读书笔记" value="3" />
              <el-option label="AI人工智能" value="4" />
            </el-select>
          </div>
          <div class="tags-input">
            <label>标签</label>
            <el-input 
              v-model="tagInput" 
              placeholder="输入标签后按回车添加"
              @keyup.enter="addTag"
            />
            <div class="selected-tags" v-if="articleForm.tagList.length > 0">
              <el-tag 
                v-for="(tag, index) in articleForm.tagList" 
                :key="index"
                closable
                @close="removeTag(index)"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <!-- 匿名发布选项 -->
        <div class="anonymous-section">
          <label class="anonymous-label">
            <el-switch v-model="articleForm.anonymous" />
            <span>匿名发布</span>
          </label>
          <p class="anonymous-hint">开启后将以匿名用户身份发布文章，不显示您的头像和昵称</p>
        </div>
        
        <!-- 封面图片 -->
        <div class="cover-section">
          <label>封面图片</label>
          <div class="cover-upload">
            <div 
              class="cover-preview" 
              :class="{ 'has-image': articleForm.cover }"
              @click="uploadCover"
            >
              <img v-if="articleForm.cover" :src="articleForm.cover" alt="封面" />
              <div v-else class="upload-hint">
                <i class="el-icon-plus"></i>
                <span>上传封面</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
    
    <!-- 图片上传弹窗 -->
    <el-dialog title="上传图片" :visible.sync="showImageDialog" width="500px">
      <div class="upload-area" @click="selectImageFile">
        <input 
          ref="imageInput" 
          type="file" 
          accept="image/*" 
          class="file-input"
          @change="handleImageUpload"
        />
        <div class="upload-icon">
          <i class="el-icon-upload"></i>
        </div>
        <p>点击或拖拽上传图片</p>
      </div>
      <div class="uploaded-images" v-if="uploadedImages.length > 0">
        <h4>已上传图片</h4>
        <div class="image-grid">
          <div 
            v-for="(img, index) in uploadedImages" 
            :key="index"
            class="grid-item"
            :class="{ selected: selectedImageIndex === index }"
            @click="selectImage(index)"
          >
            <img :src="img.url" alt="" />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showImageDialog = false">取消</el-button>
        <el-button type="primary" @click="insertSelectedImage">插入图片</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '@/api/article'

const router = useRouter()

const articleForm = reactive({
  title: '',
  content: '',
  summary: '',
  categoryId: '',
  tagList: [],
  cover: '',
  status: 1,
  anonymous: false
})

const tagInput = ref('')
const mediaFiles = ref([])
const showImageDialog = ref(false)
const uploadedImages = ref([])
const selectedImageIndex = ref(-1)
const imageInput = ref(null)

const goBack = () => {
  router.back()
}

const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !articleForm.tagList.includes(tag)) {
    articleForm.tagList.push(tag)
    tagInput.value = ''
  }
}

const removeTag = (index) => {
  articleForm.tagList.splice(index, 1)
}

const insertImage = () => {
  showImageDialog.value = true
}

const insertVideo = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'video/*'
  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (file) {
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        const response = await fetch('/iwan/api/v1/upload/video', {
          method: 'POST',
          body: formData,
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        })
        const result = await response.json()
        if (result.code === 200) {
          mediaFiles.value.push({ type: 'video', url: result.data.url, name: file.name })
        }
      } catch (error) {
        console.error('上传失败:', error)
      }
    }
  }
  input.click()
}

const insertAudio = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'audio/*'
  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (file) {
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        const response = await fetch('/iwan/api/v1/upload/audio', {
          method: 'POST',
          body: formData,
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        })
        const result = await response.json()
        if (result.code === 200) {
          mediaFiles.value.push({ type: 'audio', url: result.data.url, name: file.name })
        }
      } catch (error) {
        console.error('上传失败:', error)
      }
    }
  }
  input.click()
}

const formatText = (type) => {
  const content = articleForm.content
  switch (type) {
    case 'bold':
      articleForm.content = `**${content}**`
      break
    case 'italic':
      articleForm.content = `*${content}*`
      break
    case 'underline':
      articleForm.content = `__${content}__`
      break
    case 'h1':
      articleForm.content = `# ${content}`
      break
    case 'h2':
      articleForm.content = `## ${content}`
      break
    case 'list':
      articleForm.content = `- ${content}`
      break
  }
}

const selectImageFile = () => {
  imageInput.value?.click()
}

const handleImageUpload = async (event) => {
  const file = event.target.files[0]
  if (file) {
    const formData = new FormData()
    formData.append('file', file)
    
    try {
      const response = await fetch('/iwan/api/v1/upload/image', {
        method: 'POST',
        body: formData,
        headers: {
          'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
      })
      const result = await response.json()
      if (result.code === 200) {
        uploadedImages.value.push({ url: result.data.url, name: file.name })
      }
    } catch (error) {
      console.error('上传失败:', error)
    }
  }
}

const selectImage = (index) => {
  selectedImageIndex.value = index
}

const insertSelectedImage = () => {
  if (selectedImageIndex.value >= 0) {
    const image = uploadedImages.value[selectedImageIndex.value]
    articleForm.content += `\n![${image.name}](${image.url})\n`
    mediaFiles.value.push({ type: 'image', url: image.url, name: image.name })
    showImageDialog.value = false
    selectedImageIndex.value = -1
  }
}

const removeMedia = (index) => {
  mediaFiles.value.splice(index, 1)
}

const uploadCover = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (event) => {
    const file = event.target.files[0]
    if (file) {
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        const response = await fetch('/iwan/api/v1/upload/image', {
          method: 'POST',
          body: formData,
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        })
        const result = await response.json()
        if (result.code === 200) {
          articleForm.cover = result.data.url
        }
      } catch (error) {
        console.error('上传失败:', error)
      }
    }
  }
  input.click()
}

const saveDraft = () => {
  localStorage.setItem('articleDraft', JSON.stringify(articleForm))
  alert('草稿已保存')
}

const submitArticle = async () => {
  if (!articleForm.title.trim()) {
    alert('请输入文章标题')
    return
  }
  if (!articleForm.content.trim()) {
    alert('请输入文章内容')
    return
  }
  
  try {
    const response = await articleApi.create({
      title: articleForm.title,
      content: articleForm.content,
      summary: articleForm.summary,
      categoryId: articleForm.categoryId,
      tagList: articleForm.tagList,
      cover: articleForm.cover,
      status: articleForm.status,
      anonymous: articleForm.anonymous
    })
    
    if (response.code === 200) {
      alert('发布成功')
      localStorage.removeItem('articleDraft')
      router.push('/square')
    } else {
      alert('发布失败: ' + response.message)
    }
  } catch (error) {
    console.error('发布失败:', error)
    alert('发布失败')
  }
}

// 加载草稿
const draft = localStorage.getItem('articleDraft')
if (draft) {
  Object.assign(articleForm, JSON.parse(draft))
}
</script>

<style scoped>
.write-container {
  min-height: 100vh;
  background-color: var(--bg-primary, #f5f5f5);
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: var(--bg-secondary, white);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-secondary, #666);
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary, #333);
}

.header-right {
  display: flex;
  gap: 10px;
}

.content {
  padding: 80px 20px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.editor-area {
  background: var(--bg-secondary, white);
  border-radius: 8px;
  padding: 20px;
}

.title-input {
  width: 100%;
  font-size: 24px;
  font-weight: 600;
  border: none;
  outline: none;
  margin-bottom: 16px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color, #eee);
  background: transparent;
  color: var(--text-primary, #333);
}

.toolbar {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color, #eee);
  margin-bottom: 16px;
}

.tool-group {
  display: flex;
  gap: 4px;
  padding: 4px;
  background-color: var(--bg-primary, #f5f5f5);
  border-radius: 6px;
}

.tool-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-secondary, #666);
  transition: all 0.3s;
  
  &:hover {
    background-color: var(--hover-bg, #e5e5e5);
    color: #667eea;
  }
}

.editor-wrapper {
  margin-bottom: 16px;
}

.content-textarea {
  width: 100%;
  min-height: 400px;
  border: none;
  outline: none;
  resize: vertical;
  font-size: 16px;
  line-height: 1.8;
  background: transparent;
  color: var(--text-primary, #333);
}

.media-preview {
  margin-bottom: 16px;
  padding: 16px;
  background-color: var(--bg-primary, #f5f5f5);
  border-radius: 8px;
}

.media-preview h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 12px;
}

.media-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.media-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: var(--bg-secondary, white);
  border-radius: 6px;
}

.media-thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  &.video, &.audio {
    background-color: #667eea;
    color: white;
    font-size: 18px;
  }
}

.media-name {
  font-size: 13px;
  color: var(--text-secondary, #666);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remove-btn {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  
  &:hover {
    color: #ff4d4f;
  }
}

.summary-section, .meta-section, .cover-section {
  margin-bottom: 16px;
}

.summary-section label, .meta-section label, .cover-section label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary, #333);
  margin-bottom: 8px;
}

.summary-textarea {
  width: 100%;
  min-height: 80px;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  background: transparent;
  color: var(--text-primary, #333);
  
  &:focus {
    border-color: #667eea;
  }
}

.meta-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.category-select, .tags-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cover-section {
  margin-top: 20px;
}

.anonymous-section {
  margin-top: 16px;
  padding: 16px;
  background-color: var(--bg-primary, #f5f5f5);
  border-radius: 8px;
}

.anonymous-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #333);
}

.anonymous-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.cover-upload {
  display: flex;
  gap: 16px;
}

.cover-preview {
  width: 200px;
  height: 120px;
  border: 2px dashed var(--border-color, #ddd);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  
  &.has-image {
    border-style: solid;
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.upload-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--text-tertiary, #999);
}

/* 弹窗样式 */
.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  margin-bottom: 16px;
  
  &:hover {
    border-color: #667eea;
  }
}

.file-input {
  display: none;
}

.upload-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 12px;
}

.uploaded-images h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.grid-item {
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  
  &.selected {
    border-color: #667eea;
  }
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}
</style>