<template>
  <div class="settings">
    <el-card>
      <template #header>
        <span>系统配置</span>
      </template>
      <el-form label-width="120px">
        <el-form-item label="系统名称">
          <el-input v-model="settings.name" />
        </el-form-item>
        <el-form-item label="系统描述">
          <el-input v-model="settings.description" type="textarea" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const settings = reactive({
  name: 'FinRag4j',
  description: 'RAG知识库系统'
})
</script>
