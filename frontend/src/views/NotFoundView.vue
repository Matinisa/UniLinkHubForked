<script setup lang="ts"></script>

<template>
  <section class="mx-auto flex max-w-lg flex-col items-center gap-3 px-6 py-24 text-center">
    <span class="font-display text-6xl font-bold text-campus-teal">404</span>
    <h1 class="font-display text-2xl font-bold text-uni-navy">Page not found</h1>
    <p class="text-sm text-medium-grey">
      The page you're looking for doesn't exist or may have been moved. Let's get you back on
      track.
    </p>
    <div class="mt-2 flex gap-3">
      <RouterLink to="/" class="btn-primary">Back to Browse</RouterLink>
      <RouterLink to="/dashboard" class="btn-secondary">Go to Dashboard</RouterLink>
    </div>
  </section>
</template>
