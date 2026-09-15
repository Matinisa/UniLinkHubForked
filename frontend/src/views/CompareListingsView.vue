<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import { useCompareStore } from "@/stores/compare";
import type { ListingDTO } from "@/lib/types";

const compare = useCompareStore();
const listings = ref<ListingDTO[]>([]);
const loading = ref(false);
const error = ref("");

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" }).format(price);
}

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: "Active",
  INACTIVE: "Inactive",
  SOLD_OUT: "Sold out",
};

async function load() {
  if (compare.ids.length === 0) {
    listings.value = [];
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    const results = await Promise.all(compare.ids.map((id) => api.get<ListingDTO>(`/listings/${id}`)));
    listings.value = results.map((r) => r.data);
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

function remove(id: string) {
  compare.remove(id);
}

watch(() => compare.ids.slice(), load);
onMounted(load);
</script>

<template>
  <section class="space-y-5">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="font-display text-xl font-bold text-uni-navy">Comparing {{ listings.length }} listing{{ listings.length === 1 ? "" : "s" }}</h1>
        <p class="text-sm text-medium-grey">Tick "Compare" on any listing to add it here (up to 3).</p>
      </div>
      <button v-if="listings.length > 0" class="btn-secondary text-sm" @click="compare.clear()">Clear all</button>
    </div>

    <p v-if="error" class="text-sm text-danger">{{ error }}</p>
    <p v-else-if="loading" class="text-sm text-medium-grey">Loading...</p>

    <div v-else-if="listings.length === 0" class="card text-sm text-medium-grey">
      Nothing to compare yet. Browse listings and tick "Compare" on up to three to see them side by side here.
    </div>

    <template v-else>
      <div class="grid gap-4" :class="listings.length === 1 ? 'grid-cols-1' : listings.length === 2 ? 'grid-cols-2' : 'grid-cols-3'">
        <div v-for="l in listings" :key="l.id" class="card relative space-y-2">
          <button class="absolute right-2 top-2 text-medium-grey hover:text-danger" @click="remove(l.id)">&times;</button>
          <img v-if="l.imageUrl" :src="l.imageUrl" alt="" class="h-24 w-full rounded-control object-cover" />
          <div v-else class="h-24 w-full rounded-control bg-sky-blue/20"></div>
          <RouterLink :to="`/listings/${l.id}`" class="font-display text-sm font-semibold text-uni-navy hover:underline">{{ l.name }}</RouterLink>
          <p class="font-display text-lg font-bold text-uni-navy">{{ formatPrice(l.price) }}</p>
        </div>
      </div>

      <div class="card overflow-x-auto !p-0">
        <table class="w-full text-sm">
          <tbody>
            <tr class="border-b border-light-grey">
              <td class="w-40 bg-soft-grey px-4 py-3 font-semibold text-uni-navy">Category</td>
              <td v-for="l in listings" :key="l.id" class="px-4 py-3">{{ l.category }}</td>
            </tr>
            <tr class="border-b border-light-grey">
              <td class="bg-soft-grey px-4 py-3 font-semibold text-uni-navy">Status</td>
              <td v-for="l in listings" :key="l.id" class="px-4 py-3">{{ STATUS_LABELS[l.status] ?? l.status }}</td>
            </tr>
            <tr class="border-b border-light-grey">
              <td class="bg-soft-grey px-4 py-3 font-semibold text-uni-navy">Views</td>
              <td v-for="l in listings" :key="l.id" class="px-4 py-3">{{ l.viewCount }}</td>
            </tr>
            <tr>
              <td class="bg-soft-grey px-4 py-3 font-semibold text-uni-navy">Saved by</td>
              <td v-for="l in listings" :key="l.id" class="px-4 py-3">{{ l.savedCount }} student{{ l.savedCount === 1 ? "" : "s" }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </section>
</template>
