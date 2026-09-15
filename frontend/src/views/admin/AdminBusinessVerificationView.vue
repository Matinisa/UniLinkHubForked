<script setup lang="ts">
import { onMounted, ref } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import AdminNav from "@/components/AdminNav.vue";
import type { AdminBusinessView } from "@/lib/types";

const pending = ref<AdminBusinessView[]>([]);
const decided = ref<AdminBusinessView[]>([]);
const loading = ref(false);
const actingId = ref<string | null>(null);
const error = ref("");

function relativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const days = Math.round(diffMs / 86400000);
  if (days < 1) return "today";
  if (days === 1) return "1 day ago";
  return `${days} days ago`;
}

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const [pendingRes, decidedRes] = await Promise.all([
      api.get<AdminBusinessView[]>("/admin/businesses", { params: { status: "PENDING" } }),
      api.get<AdminBusinessView[]>("/admin/businesses/recently-decided", { params: { limit: 5 } }),
    ]);
    pending.value = pendingRes.data;
    decided.value = decidedRes.data;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function decide(id: string, action: "verify" | "reject") {
  actingId.value = id;
  error.value = "";
  try {
    await api.post(`/admin/businesses/${id}/${action}`);
    await load();
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    actingId.value = null;
  }
}

onMounted(load);
</script>

<template>
  <div class="min-h-screen bg-soft-grey">
    <AdminNav />

    <main class="mx-auto max-w-6xl space-y-6 px-4 py-6 sm:px-6">
      <div>
        <h1 class="font-display text-2xl font-bold text-uni-navy sm:text-[26px]">Business verification</h1>
        <p class="text-sm text-medium-grey">
          Approve new businesses before their listings show a verified badge to buyers.
        </p>
      </div>

      <p v-if="error" class="text-sm text-danger">{{ error }}</p>
      <p v-else-if="loading" class="text-sm text-medium-grey">Loading...</p>

      <template v-else>
        <section>
          <div class="mb-3 flex items-center gap-2">
            <h3 class="font-display text-[15px] font-semibold text-uni-navy">Awaiting review</h3>
            <span class="badge bg-warning/15 text-warning">{{ pending.length }} pending</span>
          </div>

          <p v-if="pending.length === 0" class="card text-sm text-medium-grey">Nothing waiting on review right now.</p>

          <div v-else class="flex flex-col gap-3">
            <div
              v-for="b in pending"
              :key="b.id"
              class="card flex flex-col items-start justify-between gap-4 p-[18px] sm:flex-row"
            >
              <div class="flex-1">
                <div class="mb-1.5 flex items-center gap-2.5">
                  <span class="text-base font-semibold text-uni-navy">{{ b.businessName }}</span>
                  <span class="badge bg-academic-gold/20 text-uni-navy">{{ b.category }}</span>
                </div>
                <p class="mb-2 text-[13px] leading-relaxed text-charcoal">{{ b.description }}</p>
                <p class="text-xs text-medium-grey">
                  Owner: {{ b.ownerFullName }} &middot; #{{ b.ownerStudentNumber }} &middot; Submitted {{ relativeTime(b.createdAt) }}
                </p>
              </div>
              <div class="flex shrink-0 gap-2.5">
                <button
                  class="inline-flex items-center justify-center rounded-control border border-danger bg-white px-4 py-2.5 text-sm font-semibold text-danger disabled:opacity-50"
                  :disabled="actingId === b.id"
                  @click="decide(b.id, 'reject')"
                >
                  Reject
                </button>
                <button
                  class="inline-flex items-center justify-center rounded-control bg-success px-4 py-2.5 text-sm font-semibold text-white disabled:opacity-50"
                  :disabled="actingId === b.id"
                  @click="decide(b.id, 'verify')"
                >
                  Verify
                </button>
              </div>
            </div>
          </div>
        </section>

        <section v-if="decided.length > 0">
          <h3 class="mb-3 font-display text-[15px] font-semibold text-uni-navy">Recently decided</h3>
          <div class="flex flex-col gap-2">
            <div
              v-for="b in decided"
              :key="b.id"
              class="flex items-center justify-between rounded-card border border-light-grey bg-white px-4 py-3 opacity-75"
            >
              <div class="flex items-center gap-2.5">
                <span class="text-sm font-semibold text-uni-navy">{{ b.businessName }}</span>
                <span class="text-xs text-medium-grey">{{ b.category }}</span>
              </div>
              <span
                class="badge"
                :class="b.verificationStatus === 'VERIFIED' ? 'bg-success/15 text-success' : 'bg-danger/15 text-danger'"
              >
                {{ b.verificationStatus === "VERIFIED" ? "Verified" : "Rejected" }}
              </span>
            </div>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>
