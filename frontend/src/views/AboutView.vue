<script setup lang="ts">
</script>

<template>
  <div class="about-page">
    <div class="container">
      <h1 class="page-title">关于我们</h1>
      <div class="content">
        <p>学成在线是专业的在线教育平台，致力于为学员提供优质的课程内容与学习体验。</p>
        <p>平台汇聚一线大厂讲师，覆盖主流技术栈与职场技能，助力职业成长。</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  padding: var(--space-6) var(--space-4);
}
.container {
  max-width: 720px;
  margin: 0 auto;
}
.page-title {
  font-size: 1.5rem;
  margin-bottom: var(--space-6);
}
.content p {
  margin-bottom: var(--space-4);
  color: var(--color-text);
  line-height: 1.7;
}
</style>
