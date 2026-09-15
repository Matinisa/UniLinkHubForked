<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { api, extractErrorMessage } from "@/lib/api";

const router = useRouter();

const email = ref("");
const requesting = ref(false);
const requestStatus = ref("");
const requestError = ref("");

async function requestReset() {
  requesting.value = true;
  requestStatus.value = "";
  requestError.value = "";
  try {
    await api.post("/auth/forgot-password", { email: email.value });
    requestStatus.value = "If that email is registered, a reset code has been sent.";
  } catch (err) {
    requestError.value = extractErrorMessage(err);
  } finally {
    requesting.value = false;
  }
}

const token = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const resetting = ref(false);
const resetStatus = ref("");
const resetError = ref("");

async function resetPassword() {
  resetStatus.value = "";
  resetError.value = "";
  if (newPassword.value !== confirmPassword.value) {
    resetError.value = "New password and confirmation do not match.";
    return;
  }
  resetting.value = true;
  try {
    await api.post("/auth/reset-password", { token: token.value, newPassword: newPassword.value });
    resetStatus.value = "Password reset. Redirecting to login...";
    setTimeout(() => router.push({ name: "login" }), 1500);
  } catch (err) {
    resetError.value = extractErrorMessage(err);
  } finally {
    resetting.value = false;
  }
}
</script>

<template>
  <section class="mx-auto max-w-3xl">
    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2">
      <div class="card space-y-4">
        <div>
          <h1 class="font-display text-xl font-bold text-uni-navy">Forgot your password?</h1>
          <p class="mt-1 text-sm text-medium-grey">Enter your student email and we'll send you a reset code.</p>
        </div>
        <form class="space-y-3" @submit.prevent="requestReset">
          <input v-model="email" type="email" required placeholder="Student email" class="input-field" />
          <button type="submit" class="btn-primary w-full" :disabled="requesting">
            {{ requesting ? "Sending..." : "Send reset code" }}
          </button>
        </form>
        <p v-if="requestError" class="text-sm text-danger">{{ requestError }}</p>
        <p v-else-if="requestStatus" class="text-sm text-success">{{ requestStatus }}</p>
        <p class="text-xs text-medium-grey">
          Remembered it after all? <RouterLink to="/login" class="font-medium text-campus-teal underline">Back to log in</RouterLink>
        </p>
      </div>

      <div class="card space-y-4">
        <div>
          <h1 class="font-display text-xl font-bold text-uni-navy">Reset your password</h1>
          <p class="mt-1 text-sm text-medium-grey">Paste the reset code from your email, then choose a new password.</p>
        </div>
        <form class="space-y-3" @submit.prevent="resetPassword">
          <input v-model="token" required placeholder="Reset code" class="input-field font-mono text-xs" />
          <input v-model="newPassword" type="password" required placeholder="New password" class="input-field" />
          <input v-model="confirmPassword" type="password" required placeholder="Confirm new password" class="input-field" />
          <button type="submit" class="btn-primary w-full" :disabled="resetting">
            {{ resetting ? "Resetting..." : "Reset password" }}
          </button>
        </form>
        <p v-if="resetError" class="text-sm text-danger">{{ resetError }}</p>
        <p v-else-if="resetStatus" class="text-sm text-success">{{ resetStatus }}</p>
        <p class="rounded-control bg-soft-grey p-2.5 text-xs text-medium-grey">
          No SMTP provider is wired up yet in dev - the reset code is logged to the backend
          console the same way the verification link is.
        </p>
      </div>
    </div>
  </section>
</template>
