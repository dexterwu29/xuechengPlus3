<script setup lang="ts">
// 媒资管理 - 待实现：统一管理上传文件，管理员可删除/撤销违规内容
</script>

<template>
  <div class="manage-media-page">
    <div class="container">
      <h1 class="page-title">媒资管理</h1>
      <p class="placeholder">功能开发中：统一管理上传文件，支持管理员删除/撤销违规内容</p>
    </div>
  </div>
</template>

<style scoped>
.manage-media-page {
  padding: var(--space-6) var(--space-4);
}
.container {
  max-width: 960px;
  margin: 0 auto;
}
.page-title {
  font-size: 1.5rem;
  margin-bottom: var(--space-4);
}
.placeholder {
  color: var(--color-text-muted);
}
</style>
