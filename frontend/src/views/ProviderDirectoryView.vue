<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import type { ProviderProfileDTO } from "@/lib/types";

const businesses = ref<ProviderProfileDTO[]>([]);
const keyword = ref("");
const category = ref("");
const verifiedOnly = ref(false);
const loading = ref(false);
const error = ref("");

let debounceHandle: ReturnType<typeof setTimeout> | undefined;

async function search() {
  loading.value = true;
  error.value = "";
  try {
    const { data } = await api.get<ProviderProfileDTO[]>("/businesses", {
      params: {
        keyword: keyword.value || undefined,
        category: category.value || undefined,
        verifiedOnly: verifiedOnly.value || undefined,
      },
    });
    businesses.value = data;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

const resultsLabel = computed(() =>
  businesses.value.length === 1 ? "1 business found" : `${businesses.value.length} businesses found`,
);

watch([keyword, category, verifiedOnly], () => {
  clearTimeout(debounceHandle);
  debounceHandle = setTimeout(search, 250);
});

onMounted(search);
</script>

<template>
  <section class="space-y-6">
    <div class="rounded-card bg-uni-navy px-6 py-10 text-white">
      <h1 class="font-display text-2xl font-bold sm:text-3xl">Meet the businesses on campus</h1>
      <p class="mt-2 max-w-2xl text-sm text-white/80">
        Browse every student-run business on UniLinkHub and see what they offer.
      </p>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row">
      <input v-model="keyword" type="search" placeholder="Search businesses..." class="input-field sm:max-w-sm" />
      <input v-model="category" type="text" placeholder="Category (e.g. tutoring, printing)" class="input-field sm:max-w-xs" />
      <label class="flex items-center gap-2 text-sm text-charcoal">
        <input v-model="verifiedOnly" type="checkbox" class="accent-campus-teal" /> Verified only
      </label>
    </div>

    <p v-if="error" class="text-sm text-danger">{{ error }}</p>
    <p v-else-if="loading" class="text-sm text-medium-grey">Loading businesses...</p>
    <p v-else-if="businesses.length === 0" class="text-sm text-medium-grey">No businesses match your search yet.</p>
    <p v-else class="text-sm text-medium-grey">{{ resultsLabel }}</p>

    <div v-if="!loading && businesses.length > 0" class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
      <RouterLink
        v-for="b in businesses"
        :key="b.businessId"
        :to="`/providers/${b.businessId}`"
        class="card space-y-2 transition hover:shadow-md"
      >
        <div class="flex items-center gap-2.5">
          <div
            v-if="b.imageUrl"
            class="h-10 w-10 shrink-0 rounded-full border border-light-grey bg-cover bg-center"
            :style="{ backgroundImage: `url(${b.imageUrl})` }"
          ></div>
          <div v-else class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-sky-blue/20 font-display text-sm font-bold text-uni-navy">
            {{ b.businessName.charAt(0) }}
          </div>
          <div class="flex-1">
            <div class="flex items-center justify-between gap-2">
              <h3 class="font-display text-base font-semibold text-uni-navy">{{ b.businessName }}</h3>
              <span
                class="badge shrink-0"
                :class="b.verificationStatus === 'VERIFIED' ? 'bg-success/15 text-success' : 'bg-warning/15 text-warning'"
              >
                {{ b.verificationStatus === "VERIFIED" ? "Verified" : "Pending" }}
              </span>
            </div>
          </div>
        </div>
        <p class="text-xs text-medium-grey">{{ b.category }}</p>
        <p class="line-clamp-2 text-sm text-charcoal">{{ b.description }}</p>
        <div class="flex items-center justify-between pt-2 text-xs text-medium-grey">
          <span>{{ b.activeListingCount }} active listing{{ b.activeListingCount === 1 ? "" : "s" }}</span>
          <span class="font-medium text-campus-teal underline">View profile</span>
        </div>
      </RouterLink>
    </div>
  </section>
</template>
