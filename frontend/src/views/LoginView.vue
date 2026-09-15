<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { extractErrorMessage } from "@/lib/api";

const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

const email = ref("");
const password = ref("");
const error = ref("");
const loading = ref(false);

async function submit() {
  loading.value = true;
  error.value = "";
  try {
    await auth.login(email.value, password.value);
    const redirect = (route.query.redirect as string) || "/dashboard";
    router.push(redirect);
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="mx-auto max-w-sm">
    <div class="card space-y-4">
      <h1 class="font-display text-xl font-bold text-uni-navy">Welcome back</h1>
      <form class="space-y-3" @submit.prevent="submit">
        <input v-model="email" type="email" required placeholder="Student email" class="input-field" />
        <input v-model="password" type="password" required placeholder="Password" class="input-field" />
        <button type="submit" class="btn-primary w-full" :disabled="loading">
          {{ loading ? "Logging in..." : "Log in" }}
        </button>
      </form>
      <p v-if="error" class="text-sm text-danger">{{ error }}</p>
      <p class="text-sm text-medium-grey">
        <RouterLink to="/forgot-password" class="text-campus-teal underline">Forgot your password?</RouterLink>
      </p>
      <p class="text-sm text-medium-grey">
        New to UniLinkHub?
        <RouterLink to="/register" class="text-campus-teal underline">Create an account</RouterLink>
      </p>
    </div>
  </section>
</template>
