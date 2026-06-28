<template>
  <router-view v-slot="{ Component, route }">
    <transition name="fade" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
// App 根组件 —— 纯粹的视图容器，路由切换带 fade 过渡动画
</script>
