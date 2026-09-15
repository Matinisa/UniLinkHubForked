<script setup lang="ts">
import { onMounted, ref } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import type { BookingSummaryView } from "@/lib/types";

const bookings = ref<BookingSummaryView[]>([]);
const loading = ref(false);
const error = ref("");

const STATUS_STYLES: Record<string, string> = {
  PENDING: "bg-warning/15 text-warning",
  ACCEPTED: "bg-success/15 text-success",
  DECLINED: "bg-danger/15 text-danger",
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: "Pending",
  ACCEPTED: "Accepted",
  DECLINED: "Declined",
};

function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString("en-ZA", { weekday: "short", day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" });
}

function relativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const minutes = Math.round(diffMs / 60000);
  if (minutes < 60) return `${Math.max(minutes, 1)}m ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  return `${days}d ago`;
}

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const { data } = await api.get<BookingSummaryView[]>("/bookings/mine");
    bookings.value = data;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <section class="mx-auto max-w-2xl space-y-4">
    <h1 class="font-display text-xl font-bold text-uni-navy">My bookings</h1>
    <p class="text-sm text-medium-grey">Service requests you've sent to providers.</p>

    <p v-if="error" class="text-sm text-danger">{{ error }}</p>
    <p v-else-if="loading" class="text-sm text-medium-grey">Loading...</p>
    <p v-else-if="bookings.length === 0" class="card text-sm text-medium-grey">
      You haven't requested any bookings yet - find a service listing and request a time that works for you.
    </p>

    <div v-else class="space-y-2">
      <div v-for="b in bookings" :key="b.id" class="card space-y-1.5" :class="{ 'opacity-70': b.status === 'DECLINED' }">
        <div class="flex items-center justify-between">
          <span class="font-display text-sm font-semibold text-uni-navy">{{ b.listingName }}</span>
          <span class="badge" :class="STATUS_STYLES[b.status]">{{ STATUS_LABELS[b.status] }}</span>
        </div>
        <p class="text-xs text-medium-grey">{{ b.businessName }} &middot; {{ b.status === "ACCEPTED" ? "Confirmed for" : "Requested for" }} {{ formatDateTime(b.preferredAt) }}</p>
        <p v-if="b.status === 'DECLINED' && b.declineReason" class="text-xs italic text-medium-grey">"{{ b.declineReason }}"</p>
        <p class="text-xs text-medium-grey">Sent {{ relativeTime(b.createdAt) }}</p>
      </div>
    </div>
  </section>
</template>
