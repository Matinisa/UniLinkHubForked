<script setup lang="ts">
import { onMounted, ref, watch } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import type { ListingDTO } from "@/lib/types";
import ListingCard from "@/components/ListingCard.vue";

const listings = ref<ListingDTO[]>([]);
const keyword = ref("");
const category = ref("");
const loading = ref(false);
const error = ref("");

let debounceHandle: ReturnType<typeof setTimeout> | undefined;

async function search() {
  loading.value = true;
  error.value = "";
  try {
    const { data } = await api.get<ListingDTO[]>("/listings", {
      params: {
        keyword: keyword.value || undefined,
        category: category.value || undefined,
      },
    });
    listings.value = data;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

watch([keyword, category], () => {
  clearTimeout(debounceHandle);
  debounceHandle = setTimeout(search, 250);
});

onMounted(search);
</script>

<template>
  <section class="space-y-6">
    <div class="rounded-card bg-uni-navy px-6 py-10 text-white">
      <h1 class="font-display text-2xl font-bold sm:text-3xl">Discover student businesses on campus</h1>
      <p class="mt-2 max-w-2xl text-sm text-white/80">
        One trusted, searchable place for everything your fellow students are offering - no more
        scattered WhatsApp groups and lost social posts.
      </p>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row">
      <input
        v-model="keyword"
        type="search"
        placeholder="Search listings..."
        class="input-field sm:max-w-sm"
      />
      <input
        v-model="category"
        type="text"
        placeholder="Category (e.g. tutoring, printing)"
        class="input-field sm:max-w-xs"
      />
    </div>

    <p v-if="error" class="text-sm text-danger">{{ error }}</p>
    <p v-else-if="loading" class="text-sm text-medium-grey">Loading listings...</p>
    <p v-else-if="listings.length === 0" class="text-sm text-medium-grey">
      No listings yet. Be the first student to share a product or service with your campus community.
    </p>

    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <ListingCard v-for="listing in listings" :key="listing.id" :listing="listing" />
    </div>
  </section>
</template>
