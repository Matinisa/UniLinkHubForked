<script setup lang="ts">
import NavBar from "@/components/NavBar.vue";
</script>

<template>
  <div class="min-h-screen bg-soft-grey">
    <NavBar />
    <main class="mx-auto max-w-6xl px-4 py-6 sm:px-6">
      <RouterView />
    </main>
  </div>
</template>
