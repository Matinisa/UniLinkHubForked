<script setup lang="ts">
import { useCompareStore } from "@/stores/compare";

const compare = useCompareStore();
</script>

<template>
  <div v-if="compare.ids.length > 0" class="fixed bottom-0 left-0 right-0 border-t border-light-grey bg-uni-navy px-4 py-3 sm:px-6">
    <div class="mx-auto flex max-w-6xl items-center justify-between">
      <p class="text-sm font-semibold text-white">{{ compare.ids.length }} listing{{ compare.ids.length === 1 ? "" : "s" }} to compare</p>
      <div class="flex items-center gap-3">
        <RouterLink to="/compare" class="inline-flex items-center gap-1.5 rounded-control bg-campus-teal px-3 py-1.5 text-sm font-semibold text-white">
          Compare now
        </RouterLink>
        <button class="text-sm font-medium text-white/70 hover:text-white" @click="compare.clear()">Clear</button>
      </div>
    </div>
  </div>
</template>
