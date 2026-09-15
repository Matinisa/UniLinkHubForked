<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { extractErrorMessage } from "@/lib/api";

const auth = useAuthStore();
const router = useRouter();

const studentNumber = ref("");
const firstName = ref("");
const lastName = ref("");
const email = ref("");
const password = ref("");
const error = ref("");
const success = ref(false);
const loading = ref(false);

async function submit() {
  loading.value = true;
  error.value = "";
  try {
    await auth.register({
      studentNumber: studentNumber.value,
      firstName: firstName.value,
      lastName: lastName.value,
      email: email.value,
      password: password.value,
    });
    success.value = true;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="mx-auto max-w-sm">
    <div v-if="success" class="card space-y-3">
      <h1 class="font-display text-xl font-bold text-uni-navy">Check your account</h1>
      <p class="text-sm text-charcoal">
        We've created your account. In this MVP, the verification link is printed to the backend
        console (no email provider is wired up yet) - open it there, then log in below.
      </p>
      <RouterLink to="/login" class="btn-primary inline-flex">Go to login</RouterLink>
    </div>

    <div v-else class="card space-y-4">
      <h1 class="font-display text-xl font-bold text-uni-navy">Join UniLinkHub</h1>
      <form class="space-y-3" @submit.prevent="submit">
        <input v-model="studentNumber" required placeholder="Student number" class="input-field" />
        <div class="flex gap-3">
          <input v-model="firstName" required placeholder="First name" class="input-field" />
          <input v-model="lastName" required placeholder="Last name" class="input-field" />
        </div>
        <input v-model="email" type="email" required placeholder="Student email" class="input-field" />
        <input
          v-model="password"
          type="password"
          required
          minlength="8"
          placeholder="Password (min 8 characters)"
          class="input-field"
        />
        <button type="submit" class="btn-primary w-full" :disabled="loading">
          {{ loading ? "Creating account..." : "Create account" }}
        </button>
      </form>
      <p v-if="error" class="text-sm text-danger">{{ error }}</p>
      <p class="text-sm text-medium-grey">
        Already have an account?
        <RouterLink to="/login" class="text-campus-teal underline">Log in</RouterLink>
      </p>
    </div>
  </section>
</template>
