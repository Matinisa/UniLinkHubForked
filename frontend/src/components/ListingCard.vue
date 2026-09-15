<script setup lang="ts">
import type { ListingDTO } from "@/lib/types";

defineProps<{ listing: ListingDTO }>();

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" }).format(price);
}
</script>

<template>
  <RouterLink :to="`/listings/${listing.id}`" class="card flex flex-col gap-2 transition hover:shadow-md">
    <div class="flex items-start justify-between gap-2">
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
  </RouterLink>
</template>
