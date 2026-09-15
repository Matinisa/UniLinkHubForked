<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/stores/auth";
import { useFollowedProvidersStore } from "@/stores/followedProviders";
import ListingCard from "@/components/ListingCard.vue";
import type { ListingDTO, ProviderProfileDTO } from "@/lib/types";

const route = useRoute();
const auth = useAuthStore();
const followed = useFollowedProvidersStore();

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
        <div class="flex items-start gap-4">
          <div
            v-if="profile.imageUrl"
            class="h-16 w-16 shrink-0 rounded-full border border-light-grey bg-cover bg-center"
            :style="{ backgroundImage: `url(${profile.imageUrl})` }"
          ></div>
          <div v-else class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-sky-blue/20 font-display text-xl font-bold text-uni-navy">
            {{ profile.businessName.charAt(0) }}
          </div>

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
        </div>

        <div class="flex shrink-0 flex-wrap gap-2">
          <button
            v-if="auth.isAuthenticated"
            class="inline-flex items-center gap-1.5 whitespace-nowrap rounded-control border px-4 py-2 text-sm font-semibold"
            :class="followed.isFollowing(profile.businessId)
              ? 'border-campus-teal bg-campus-teal/10 text-campus-teal'
              : 'border-uni-navy bg-white text-uni-navy hover:bg-soft-grey'"
            @click="followed.toggleFollow(profile)"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" :fill="followed.isFollowing(profile.businessId) ? '#2A9BB4' : 'none'" stroke="currentColor" stroke-width="2">
              <path d="M12 21s-8-4.5-8-10.5A4.5 4.5 0 0 1 12 6a4.5 4.5 0 0 1 8 4.5C20 16.5 12 21 12 21Z" />
            </svg>
            {{ followed.isFollowing(profile.businessId) ? "Following" : "Follow" }}
          </button>
          <button
            v-if="auth.isAuthenticated && !reportOpen"
            class="inline-flex items-center justify-center whitespace-nowrap rounded-control border border-danger bg-white px-4 py-2 text-sm font-semibold text-danger"
            @click="reportOpen = true"
          >
            Report this provider
          </button>
        </div>
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
