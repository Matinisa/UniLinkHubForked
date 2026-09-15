<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import AdminNav from "@/components/AdminNav.vue";
import type { AdminBusinessView, AdminReportView, ReportStatus, ReportStatusCounts } from "@/lib/types";

type Section = "reports" | "businesses";
const activeSection = ref<Section>("reports");

// ---- Reports ----
const reports = ref<AdminReportView[]>([]);
const counts = ref<ReportStatusCounts>({ open: 0, underReview: 0, resolved: 0, dismissed: 0 });
const activeFilter = ref<ReportStatus | "ALL">("ALL");
const selectedId = ref<string | null>(null);
const adminNote = ref("");
const reportsLoading = ref(false);
const acting = ref(false);
const reportsError = ref("");

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
    reportsError.value = extractErrorMessage(err);
  }
}

async function loadReports() {
  reportsLoading.value = true;
  reportsError.value = "";
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
    reportsError.value = extractErrorMessage(err);
  } finally {
    reportsLoading.value = false;
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
  reportsError.value = "";
  try {
    if (action === "begin-review") {
      await api.post(`/admin/reports/${selected.value.id}/begin-review`);
    } else {
      await api.post(`/admin/reports/${selected.value.id}/${action}`, { note: adminNote.value });
    }
    await Promise.all([loadReports(), loadCounts()]);
  } catch (err) {
    reportsError.value = extractErrorMessage(err);
  } finally {
    acting.value = false;
  }
}

// ---- Business verification ----
const pending = ref<AdminBusinessView[]>([]);
const decided = ref<AdminBusinessView[]>([]);
const businessesLoading = ref(false);
const actingBusinessId = ref<string | null>(null);
const businessesError = ref("");

function relativeDays(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const days = Math.round(diffMs / 86400000);
  if (days < 1) return "today";
  if (days === 1) return "1 day ago";
  return `${days} days ago`;
}

async function loadBusinesses() {
  businessesLoading.value = true;
  businessesError.value = "";
  try {
    const [pendingRes, decidedRes] = await Promise.all([
      api.get<AdminBusinessView[]>("/admin/businesses", { params: { status: "PENDING" } }),
      api.get<AdminBusinessView[]>("/admin/businesses/recently-decided", { params: { limit: 5 } }),
    ]);
    pending.value = pendingRes.data;
    decided.value = decidedRes.data;
  } catch (err) {
    businessesError.value = extractErrorMessage(err);
  } finally {
    businessesLoading.value = false;
  }
}

async function decide(id: string, action: "verify" | "reject") {
  actingBusinessId.value = id;
  businessesError.value = "";
  try {
    await api.post(`/admin/businesses/${id}/${action}`);
    await loadBusinesses();
  } catch (err) {
    businessesError.value = extractErrorMessage(err);
  } finally {
    actingBusinessId.value = null;
  }
}

onMounted(async () => {
  await Promise.all([loadCounts(), loadReports(), loadBusinesses()]);
});
</script>

<template>
  <div class="min-h-screen bg-soft-grey">
    <AdminNav />

    <main class="mx-auto max-w-6xl space-y-5 px-4 py-6 sm:px-6">
      <div>
        <h1 class="font-display text-2xl font-bold text-uni-navy sm:text-[26px]">Admin dashboard</h1>
        <p class="text-sm text-medium-grey">Trust &amp; safety and business verification, in one place.</p>
      </div>

      <div class="flex gap-2 border-b border-light-grey">
        <button
          class="border-b-2 px-1 pb-3 text-sm font-semibold"
          :class="activeSection === 'reports' ? 'border-campus-teal text-uni-navy' : 'border-transparent text-medium-grey hover:text-charcoal'"
          @click="activeSection = 'reports'"
        >
          Reports
          <span class="ml-1.5 rounded-full bg-soft-grey px-2 py-0.5 text-xs">{{ countFor('ALL') }}</span>
        </button>
        <button
          class="border-b-2 px-1 pb-3 text-sm font-semibold"
          :class="activeSection === 'businesses' ? 'border-campus-teal text-uni-navy' : 'border-transparent text-medium-grey hover:text-charcoal'"
          @click="activeSection = 'businesses'"
        >
          Business verification
          <span class="ml-1.5 rounded-full bg-soft-grey px-2 py-0.5 text-xs">{{ pending.length }}</span>
        </button>
      </div>

      <!-- Reports section -->
      <section v-if="activeSection === 'reports'" class="space-y-5">
        <p v-if="reportsError" class="text-sm text-danger">{{ reportsError }}</p>

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

        <p v-if="reportsLoading" class="text-sm text-medium-grey">Loading...</p>

        <div v-else-if="reports.length === 0" class="card text-sm text-medium-grey">
          No reports in this filter.
        </div>

        <div v-else class="flex flex-col gap-5 lg:flex-row lg:items-start">
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
      </section>

      <!-- Business verification section -->
      <section v-else class="space-y-6">
        <p v-if="businessesError" class="text-sm text-danger">{{ businessesError }}</p>
        <p v-else-if="businessesLoading" class="text-sm text-medium-grey">Loading...</p>

        <template v-else>
          <div>
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
                    Owner: {{ b.ownerFullName }} &middot; #{{ b.ownerStudentNumber }} &middot; Submitted {{ relativeDays(b.createdAt) }}
                  </p>
                </div>
                <div class="flex shrink-0 gap-2.5">
                  <button
                    class="inline-flex items-center justify-center rounded-control border border-danger bg-white px-4 py-2.5 text-sm font-semibold text-danger disabled:opacity-50"
                    :disabled="actingBusinessId === b.id"
                    @click="decide(b.id, 'reject')"
                  >
                    Reject
                  </button>
                  <button
                    class="inline-flex items-center justify-center rounded-control bg-success px-4 py-2.5 text-sm font-semibold text-white disabled:opacity-50"
                    :disabled="actingBusinessId === b.id"
                    @click="decide(b.id, 'verify')"
                  >
                    Verify
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="decided.length > 0">
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
          </div>
        </template>
      </section>
    </main>
  </div>
</template>
