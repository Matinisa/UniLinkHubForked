<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/stores/auth";
import ListingCard from "@/components/ListingCard.vue";
import type { ListingDTO, ProviderProfileDTO } from "@/lib/types";

const route = useRoute();
const auth = useAuthStore();

const profile = ref<ProviderProfileDTO | null>(null);
const listings = ref<ListingDTO[]>([]);
const error = ref("");
const reportOpen = ref(false);
const reportReason = ref("MISREPRESENTATION");
const reportDetails = ref("");
const reportStatus = ref("");

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString("en-ZA", { month: "short", year: "numeric" });
}

async function load() {
  const businessId = route.params.businessId as string;
  try {
    const [profileRes, listingsRes] = await Promise.all([
      api.get<ProviderProfileDTO>(`/businesses/${businessId}/profile`),
      api.get<ListingDTO[]>(`/listings/business/${businessId}`),
    ]);
    profile.value = profileRes.data;
    listings.value = listingsRes.data.filter((l) => l.status === "ACTIVE");
  } catch (err) {
    error.value = extractErrorMessage(err);
  }
}

async function submitReport() {
  if (!profile.value) return;
  reportStatus.value = "";
  try {
    await api.post("/reports", {
      targetType: "USER",
      targetId: profile.value.ownerId,
      reason: reportReason.value,
      details: reportDetails.value,
    });
    reportStatus.value = "Thanks - our team will review this shortly.";
    reportOpen.value = false;
    reportDetails.value = "";
  } catch (err) {
    reportStatus.value = extractErrorMessage(err);
  }
}

onMounted(load);
</script>

<template>
  <section v-if="profile" class="space-y-5">
    <RouterLink to="/" class="text-sm text-medium-grey hover:text-campus-teal">&larr; Back to browse</RouterLink>

    <div class="card space-y-5">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <div class="mb-2 flex flex-wrap items-center gap-2">
            <h1 class="font-display text-2xl font-bold text-uni-navy sm:text-[26px]">{{ profile.businessName }}</h1>
            <span
              v-if="profile.verificationStatus === 'VERIFIED'"
              class="badge bg-success/15 text-success"
            >
              Verified
            </span>
            <span v-else class="badge bg-warning/15 text-warning">Pending verification</span>
            <span class="badge bg-academic-gold/20 text-uni-navy">{{ profile.category }}</span>
          </div>
          <p class="mb-3.5 text-sm text-medium-grey">Run by {{ profile.ownerFullName }}</p>
          <p class="max-w-xl text-sm leading-relaxed text-charcoal">{{ profile.description }}</p>
        </div>

        <button
          v-if="auth.isAuthenticated && !reportOpen"
          class="inline-flex shrink-0 items-center justify-center whitespace-nowrap rounded-control border border-danger bg-white px-4 py-2 text-sm font-semibold text-danger"
          @click="reportOpen = true"
        >
          Report this provider
        </button>
      </div>

      <div class="flex flex-wrap gap-7 border-t border-light-grey pt-4">
        <div>
          <p class="font-display text-xl font-bold text-uni-navy">{{ profile.activeListingCount }}</p>
          <p class="text-xs text-medium-grey">Active listings</p>
        </div>
        <div>
          <p class="font-display text-xl font-bold text-uni-navy">{{ profile.totalViews }}</p>
          <p class="text-xs text-medium-grey">Total views</p>
        </div>
        <div>
          <p class="font-display text-xl font-bold text-uni-navy">{{ formatDate(profile.memberSince) }}</p>
          <p class="text-xs text-medium-grey">Member since</p>
        </div>
      </div>
    </div>

    <div v-if="reportOpen" class="card space-y-3">
      <form class="space-y-3" @submit.prevent="submitReport">
        <select v-model="reportReason" class="input-field">
          <option value="MISREPRESENTATION">Misrepresentation</option>
          <option value="NON_DELIVERY">Non-delivery</option>
          <option value="INAPPROPRIATE_CONDUCT">Inappropriate conduct</option>
          <option value="SPAM">Spam</option>
          <option value="OTHER">Other</option>
        </select>
        <textarea
          v-model="reportDetails"
          class="input-field"
          rows="3"
          placeholder="Tell us what happened (optional)"
        ></textarea>
        <div class="flex gap-2">
          <button type="submit" class="btn-primary text-sm">Submit report</button>
          <button type="button" class="btn-secondary text-sm" @click="reportOpen = false">Cancel</button>
        </div>
      </form>
    </div>
    <p v-if="reportStatus" class="text-sm text-medium-grey">{{ reportStatus }}</p>
    <p v-else-if="!auth.isAuthenticated" class="text-sm text-medium-grey">
      <RouterLink to="/login" class="text-campus-teal underline">Log in</RouterLink> to report a provider.
    </p>

    <div>
      <h2 class="mb-3 font-display text-lg font-semibold text-uni-navy">Listings from {{ profile.businessName }}</h2>

      <p v-if="listings.length === 0" class="card text-sm text-medium-grey">No active listings right now.</p>
      <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <ListingCard v-for="listing in listings" :key="listing.id" :listing="listing" />
      </div>
    </div>
  </section>

  <p v-else-if="error" class="text-sm text-danger">{{ error }}</p>
  <p v-else class="text-sm text-medium-grey">Loading...</p>
</template>
