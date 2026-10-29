<script setup>
defineProps({
  title: String,
  value: [String, Number],
  subtitle: String,
  icon: Object
})
</script>

<template>
  <div class="metric-card">
    <div class="metric-content">
      <span class="metric-title">{{ title }}</span>
      <strong class="metric-value">{{ value }}</strong>
      <span class="metric-subtitle">{{ subtitle }}</span>
    </div>

    <div class="metric-icon">
      <component :is="icon" :size="24" />
    </div>
  </div>
</template>