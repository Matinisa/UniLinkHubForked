<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/stores/auth";
import { useSavedListingsStore } from "@/stores/savedListings";
import { recordView } from "@/lib/recentlyViewed";
import type { BusinessDTO, ListingDTO } from "@/lib/types";

const route = useRoute();
const auth = useAuthStore();
const saved = useSavedListingsStore();

const listing = ref<ListingDTO | null>(null);
const business = ref<BusinessDTO | null>(null);
const error = ref("");
const reportOpen = ref(false);
const reportReason = ref("MISREPRESENTATION");
const reportDetails = ref("");
const reportStatus = ref("");

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" }).format(price);
}

async function load() {
  try {
    const { data } = await api.get<ListingDTO>(`/listings/${route.params.id}`);
    listing.value = data;
    recordView(data);
    const { data: businessData } = await api.get<BusinessDTO>(`/businesses/${data.businessId}`);
    business.value = businessData;
  } catch (err) {
    error.value = extractErrorMessage(err);
  }
}

async function submitReport() {
  if (!listing.value) return;
  reportStatus.value = "";
  try {
    await api.post("/reports", {
      targetType: "LISTING",
      targetId: listing.value.id,
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
  <section v-if="listing" class="mx-auto max-w-2xl space-y-4">
    <div class="card overflow-hidden p-0">
      <img v-if="listing.imageUrl" :src="listing.imageUrl" alt="" class="h-56 w-full object-cover" />
      <div class="space-y-3 p-4">
      <div class="flex items-start justify-between gap-2">
        <h1 class="font-display text-2xl font-bold text-uni-navy">{{ listing.name }}</h1>
        <div class="flex shrink-0 items-center gap-2">
          <span class="badge bg-sky-blue/20 text-uni-navy">{{ listing.type }}</span>
          <button
            v-if="auth.isAuthenticated"
            class="flex h-8 w-8 items-center justify-center rounded-full border border-light-grey"
            :aria-label="saved.isSaved(listing.id) ? 'Unsave listing' : 'Save listing'"
            @click="saved.toggleSave(listing)"
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              :fill="saved.isSaved(listing.id) ? '#DC2626' : 'none'"
              stroke="#DC2626"
              stroke-width="2"
            >
              <path
                d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.6l-1-1a5.5 5.5 0 0 0-7.8 7.8l1 1L12 21l7.8-7.6 1-1a5.5 5.5 0 0 0 0-7.8Z"
              />
            </svg>
          </button>
        </div>
      </div>
      <p class="text-sm text-medium-grey">{{ listing.category }}</p>
      <RouterLink
        v-if="business"
        :to="`/providers/${business.id}`"
        class="inline-flex w-fit items-center gap-1.5 text-sm text-charcoal hover:text-campus-teal"
      >
        Sold by <span class="font-medium underline">{{ business.businessName }}</span>
        <span v-if="business.verificationStatus === 'VERIFIED'" class="badge bg-success/15 text-success">Verified</span>
      </RouterLink>
      <p class="whitespace-pre-line text-charcoal">{{ listing.description }}</p>
      <p class="font-display text-2xl font-semibold text-campus-teal">{{ formatPrice(listing.price) }}</p>

      <dl v-if="listing.type === 'PRODUCT'" class="text-sm text-medium-grey">
        <dt class="inline font-medium">In stock:</dt>
        <dd class="inline"> {{ listing.stockQuantity ?? "N/A" }}</dd>
      </dl>
      <dl v-else class="text-sm text-medium-grey">
        <dt class="inline font-medium">Availability:</dt>
        <dd class="inline"> {{ listing.availabilitySchedule ?? "Contact seller" }}</dd>
      </dl>
      </div>
    </div>

    <div v-if="auth.isAuthenticated" class="card">
      <button v-if="!reportOpen" class="btn-secondary text-sm" @click="reportOpen = true">
        Report this listing
      </button>
      <form v-else class="space-y-3" @submit.prevent="submitReport">
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
      <p v-if="reportStatus" class="mt-2 text-sm text-medium-grey">{{ reportStatus }}</p>
    </div>
    <p v-else class="text-sm text-medium-grey">
      <RouterLink to="/login" class="text-campus-teal underline">Log in</RouterLink> to report a listing.
    </p>
  </section>

  <p v-else-if="error" class="text-sm text-danger">{{ error }}</p>
  <p v-else class="text-sm text-medium-grey">Loading...</p>
</template>
