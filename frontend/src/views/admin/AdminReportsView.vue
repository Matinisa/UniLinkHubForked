<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import AdminNav from "@/components/AdminNav.vue";
import type { AdminReportView, ReportStatus, ReportStatusCounts } from "@/lib/types";

const reports = ref<AdminReportView[]>([]);
const counts = ref<ReportStatusCounts>({ open: 0, underReview: 0, resolved: 0, dismissed: 0 });
const activeFilter = ref<ReportStatus | "ALL">("ALL");
const selectedId = ref<string | null>(null);
const adminNote = ref("");
const loading = ref(false);
const acting = ref(false);
const error = ref("");

const filters: { value: ReportStatus | "ALL"; label: string }[] = [
  { value: "ALL", label: "All" },
  { value: "OPEN", label: "Open" },
  { value: "UNDER_REVIEW", label: "Under review" },
  { value: "RESOLVED", label: "Resolved" },
  { value: "DISMISSED", label: "Dismissed" },
];

function countFor(value: ReportStatus | "ALL"): number {
  if (value === "ALL") return counts.value.open + counts.value.underReview + counts.value.resolved + counts.value.dismissed;
  if (value === "OPEN") return counts.value.open;
  if (value === "UNDER_REVIEW") return counts.value.underReview;
  if (value === "RESOLVED") return counts.value.resolved;
  return counts.value.dismissed;
}

const selected = computed(() => reports.value.find((r) => r.id === selectedId.value) ?? null);

const REASON_LABELS: Record<string, string> = {
  MISREPRESENTATION: "Misrepresentation",
  NON_DELIVERY: "Non-delivery",
  INAPPROPRIATE_CONDUCT: "Inappropriate conduct",
  SPAM: "Spam",
  OTHER: "Other",
};

const STATUS_STYLES: Record<ReportStatus, string> = {
  OPEN: "bg-warning/15 text-warning",
  UNDER_REVIEW: "bg-info/15 text-info",
  RESOLVED: "bg-success/15 text-success",
  DISMISSED: "bg-medium-grey/15 text-medium-grey",
};

const STATUS_LABELS: Record<ReportStatus, string> = {
  OPEN: "Open",
  UNDER_REVIEW: "Under review",
  RESOLVED: "Resolved",
  DISMISSED: "Dismissed",
};

function relativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const minutes = Math.round(diffMs / 60000);
  if (minutes < 60) return `${Math.max(minutes, 1)}m ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  return `${days}d ago`;
}

async function loadCounts() {
  try {
    const { data } = await api.get<ReportStatusCounts>("/admin/reports/counts");
    counts.value = data;
  } catch (err) {
    error.value = extractErrorMessage(err);
  }
}

async function loadReports() {
  loading.value = true;
  error.value = "";
  try {
    const { data } = await api.get<AdminReportView[]>("/admin/reports", {
      params: activeFilter.value === "ALL" ? {} : { status: activeFilter.value },
    });
    reports.value = data;
    if (!data.find((r) => r.id === selectedId.value)) {
      selectedId.value = data[0]?.id ?? null;
      adminNote.value = "";
    }
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

function selectReport(id: string) {
  selectedId.value = id;
  adminNote.value = selected.value?.adminNote ?? "";
}

function setFilter(value: ReportStatus | "ALL") {
  activeFilter.value = value;
  loadReports();
}

async function act(action: "begin-review" | "resolve" | "dismiss") {
  if (!selected.value) return;
  acting.value = true;
  error.value = "";
  try {
    if (action === "begin-review") {
      await api.post(`/admin/reports/${selected.value.id}/begin-review`);
    } else {
      await api.post(`/admin/reports/${selected.value.id}/${action}`, { note: adminNote.value });
    }
    await Promise.all([loadReports(), loadCounts()]);
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    acting.value = false;
  }
}

onMounted(async () => {
  await Promise.all([loadCounts(), loadReports()]);
});
</script>

<template>
  <div class="min-h-screen bg-soft-grey">
    <AdminNav />

    <main class="mx-auto max-w-6xl space-y-5 px-4 py-6 sm:px-6">
      <div>
        <h1 class="font-display text-2xl font-bold text-uni-navy sm:text-[26px]">Report queue</h1>
        <p class="text-sm text-medium-grey">
          Review flagged listings and providers, then resolve or dismiss - Section 12.2 of the trust &amp; safety design.
        </p>
      </div>

      <p v-if="error" class="text-sm text-danger">{{ error }}</p>

      <div class="flex flex-wrap gap-2">
        <button
          v-for="f in filters"
          :key="f.value"
          class="rounded-full border px-3.5 py-1.5 text-[13px] font-semibold"
          :class="activeFilter === f.value ? 'border-campus-teal bg-campus-teal text-white' : 'border-light-grey bg-white text-charcoal hover:border-campus-teal'"
          @click="setFilter(f.value)"
        >
          {{ f.label }} &nbsp;{{ countFor(f.value) }}
        </button>
      </div>

      <p v-if="loading" class="text-sm text-medium-grey">Loading...</p>

      <div v-else-if="reports.length === 0" class="card text-sm text-medium-grey">
        No reports in this filter.
      </div>

      <div v-else class="flex flex-col gap-5 lg:flex-row lg:items-start">
        <!-- List -->
        <div class="flex w-full flex-col gap-2.5 lg:w-[400px] lg:shrink-0">
          <button
            v-for="r in reports"
            :key="r.id"
            class="rounded-card border bg-white p-3.5 text-left shadow-sm transition"
            :class="r.id === selectedId ? 'border-[1.5px] border-campus-teal' : 'border-light-grey hover:border-campus-teal'"
            @click="selectReport(r.id)"
          >
            <div class="mb-1.5 flex items-center justify-between">
              <span class="text-sm font-semibold text-uni-navy">{{ REASON_LABELS[r.reason] ?? r.reason }}</span>
              <span class="badge" :class="STATUS_STYLES[r.status]">{{ STATUS_LABELS[r.status] }}</span>
            </div>
            <p class="mb-2 line-clamp-2 text-[13px] text-charcoal">{{ r.details || "No further details provided." }}</p>
            <div class="flex items-center justify-between text-xs text-medium-grey">
              <span>{{ r.target.type === "LISTING" ? "Listing" : "Provider" }} &middot; {{ r.target.label }}</span>
              <span>{{ relativeTime(r.createdAt) }}</span>
            </div>
          </button>
        </div>

        <!-- Detail -->
        <div v-if="selected" class="card flex-1 space-y-5 p-6">
          <div>
            <div class="mb-1 flex items-center gap-2.5">
              <h2 class="font-display text-[19px] font-semibold text-uni-navy">
                {{ REASON_LABELS[selected.reason] ?? selected.reason }}
              </h2>
              <span class="badge" :class="STATUS_STYLES[selected.status]">{{ STATUS_LABELS[selected.status] }}</span>
            </div>
            <p class="text-[13px] text-medium-grey">
              Report #{{ selected.id.slice(0, 8) }} &middot; Filed {{ relativeTime(selected.createdAt) }}
            </p>
          </div>

          <div class="grid grid-cols-1 gap-4 rounded-control bg-soft-grey p-4 sm:grid-cols-2">
            <div>
              <p class="mb-1 text-xs uppercase tracking-wide text-medium-grey">Reported by</p>
              <p class="text-sm font-medium text-charcoal">
                {{ selected.reporter.fullName }} &middot; #{{ selected.reporter.studentNumber }}
              </p>
            </div>
            <div>
              <p class="mb-1 text-xs uppercase tracking-wide text-medium-grey">Target</p>
              <p class="text-sm font-medium text-charcoal">
                <RouterLink v-if="selected.target.type === 'LISTING'" :to="`/listings/${selected.target.id}`" class="underline">
                  {{ selected.target.label }}
                </RouterLink>
                <span v-else>{{ selected.target.label }}</span>
                &middot; {{ selected.target.type === "LISTING" ? "Listing" : "Provider" }}
              </p>
            </div>
            <div v-if="selected.target.secondaryLabel">
              <p class="mb-1 text-xs uppercase tracking-wide text-medium-grey">
                {{ selected.target.type === "LISTING" ? "Business" : "Details" }}
              </p>
              <p class="text-sm font-medium text-charcoal">{{ selected.target.secondaryLabel }}</p>
            </div>
            <div>
              <p class="mb-1 text-xs uppercase tracking-wide text-medium-grey">Reason</p>
              <p class="text-sm font-medium text-charcoal">{{ REASON_LABELS[selected.reason] ?? selected.reason }}</p>
            </div>
          </div>

          <div>
            <p class="mb-1.5 text-xs uppercase tracking-wide text-medium-grey">Details from the reporter</p>
            <p class="text-sm leading-relaxed text-charcoal">{{ selected.details || "No further details provided." }}</p>
          </div>

          <div>
            <label class="mb-1.5 block text-xs uppercase tracking-wide text-medium-grey">
              Admin note (visible to reviewers only)
            </label>
            <textarea
              v-model="adminNote"
              rows="3"
              placeholder="What did you check, and what's the outcome?"
              class="input-field resize-y"
            ></textarea>
          </div>

          <div class="flex flex-wrap gap-2.5 border-t border-light-grey pt-4">
            <button
              class="btn-secondary text-sm"
              :disabled="acting || selected.status !== 'OPEN'"
              @click="act('begin-review')"
            >
              Begin review
            </button>
            <button
              class="inline-flex items-center justify-center rounded-control bg-success px-[18px] py-2.5 text-sm font-semibold text-white disabled:opacity-50"
              :disabled="acting || selected.status === 'RESOLVED' || selected.status === 'DISMISSED'"
              @click="act('resolve')"
            >
              Resolve
            </button>
            <button
              class="ml-auto inline-flex items-center justify-center rounded-control border border-danger px-[18px] py-2.5 text-sm font-semibold text-danger disabled:opacity-50"
              :disabled="acting || selected.status === 'RESOLVED' || selected.status === 'DISMISSED'"
              @click="act('dismiss')"
            >
              Dismiss
            </button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
