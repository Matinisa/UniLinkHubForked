<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const initials = () => {
  const first = auth.user?.firstName?.[0] ?? "";
  const last = auth.user?.lastName?.[0] ?? "";
  return (first + last).toUpperCase() || "A";
};

function handleLogout() {
  auth.logout();
  router.push({ name: "login" });
}
</script>

<template>
  <header class="border-b border-light-grey bg-white">
    <div class="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
      <div class="flex items-baseline gap-2.5">
        <RouterLink to="/" class="font-display text-xl font-bold text-uni-navy">UniLinkHub</RouterLink>
        <span class="rounded-full border border-academic-gold bg-academic-gold/15 px-2 py-0.5 text-[11px] font-semibold text-uni-navy">
          Admin console
        </span>
      </div>

      <nav class="flex items-center gap-6">
        <RouterLink
          to="/admin/reports"
          class="border-b-2 pb-1 text-sm font-medium"
          :class="route.name === 'admin-reports' ? 'border-campus-teal font-semibold text-uni-navy' : 'border-transparent text-charcoal hover:text-campus-teal'"
        >
          Reports
        </RouterLink>
        <RouterLink
          to="/admin/businesses"
          class="border-b-2 pb-1 text-sm font-medium"
          :class="route.name === 'admin-businesses' ? 'border-campus-teal font-semibold text-uni-navy' : 'border-transparent text-charcoal hover:text-campus-teal'"
        >
          Business verification
        </RouterLink>

        <div class="h-5 w-px bg-light-grey"></div>

        <div class="flex items-center gap-2">
          <div class="flex h-[30px] w-[30px] items-center justify-center rounded-full bg-uni-navy font-display text-xs font-bold text-white">
            {{ initials() }}
          </div>
          <span class="text-sm font-medium text-charcoal">{{ auth.user?.firstName }} {{ auth.user?.lastName }}</span>
        </div>

        <button class="btn-secondary text-sm" @click="handleLogout">Log out</button>
      </nav>
    </div>
  </header>
</template>
