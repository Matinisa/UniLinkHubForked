<script setup lang="ts">
import type { ListingDTO } from "@/lib/types";
import { useAuthStore } from "@/stores/auth";
import { useSavedListingsStore } from "@/stores/savedListings";
import { useCompareStore } from "@/stores/compare";

const props = defineProps<{ listing: ListingDTO }>();

const auth = useAuthStore();
const saved = useSavedListingsStore();
const compare = useCompareStore();

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" }).format(price);
}
</script>

<template>
  <RouterLink
    :to="`/listings/${listing.id}`"
    class="card relative flex flex-col gap-2 overflow-hidden p-0 transition hover:shadow-md"
    :class="{ 'opacity-70': listing.status !== 'ACTIVE' }"
  >
    <img v-if="listing.imageUrl" :src="listing.imageUrl" alt="" class="h-32 w-full object-cover" />
    <span
      v-if="listing.status === 'SOLD_OUT'"
      class="badge absolute left-2.5 top-2.5 bg-medium-grey/90 text-white"
    >
      Sold out
    </span>
    <span
      v-else-if="listing.status === 'INACTIVE'"
      class="badge absolute left-2.5 top-2.5 bg-danger/90 text-white"
    >
      Inactive
    </span>
    <button
      v-if="auth.isAuthenticated"
      class="absolute right-2.5 top-2.5 flex h-[30px] w-[30px] items-center justify-center rounded-full bg-white/90"
      :aria-label="saved.isSaved(listing.id) ? 'Unsave listing' : 'Save listing'"
      @click.stop.prevent="saved.toggleSave(props.listing)"
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

    <div class="flex flex-1 flex-col gap-2 p-4">
      <div class="flex items-start justify-between gap-2" :class="{ 'pr-7': !listing.imageUrl }">
        <h3 class="font-display text-base font-semibold text-uni-navy">{{ listing.name }}</h3>
        <span
          class="badge shrink-0"
          :class="listing.type === 'PRODUCT' ? 'bg-sky-blue/20 text-uni-navy' : 'bg-academic-gold/20 text-uni-navy'"
        >
          {{ listing.type === "PRODUCT" ? "Product" : "Service" }}
        </span>
      </div>
      <p class="line-clamp-2 text-sm text-medium-grey">{{ listing.description }}</p>
      <div class="mt-auto flex items-center justify-between pt-2">
        <span class="font-display text-lg font-semibold text-campus-teal">{{ formatPrice(listing.price) }}</span>
        <span class="text-xs text-medium-grey">{{ listing.category }}</span>
      </div>
      <label
        class="flex items-center gap-1.5 text-xs text-medium-grey"
        :class="{ 'opacity-50': !compare.isComparing(listing.id) && compare.isFull }"
        @click.stop
      >
        <input
          type="checkbox"
          class="accent-campus-teal"
          :checked="compare.isComparing(listing.id)"
          :disabled="!compare.isComparing(listing.id) && compare.isFull"
          @change="compare.toggle(listing.id)"
        />
        Compare
      </label>
    </div>
  </RouterLink>
</template>
