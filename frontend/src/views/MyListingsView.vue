<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { api, extractErrorMessage } from "@/lib/api";
import type { BusinessDTO, ListingDTO } from "@/lib/types";

const router = useRouter();

const listings = ref<ListingDTO[]>([]);
const businesses = ref<BusinessDTO[]>([]);
const loading = ref(false);
const error = ref("");
const statusFilter = ref<"ALL" | "ACTIVE" | "SOLD_OUT" | "INACTIVE">("ALL");
const keyword = ref("");

const businessNameById = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {};
  for (const b of businesses.value) map[b.id] = b.businessName;
  return map;
});

function statusCountFor(value: string): number {
  if (value === "ALL") return listings.value.length;
  return listings.value.filter((l) => l.status === value).length;
}

const filteredListings = computed(() => {
  let list = listings.value;
  if (statusFilter.value !== "ALL") {
    list = list.filter((l) => l.status === statusFilter.value);
  }
  const needle = keyword.value.trim().toLowerCase();
  if (needle) {
    list = list.filter((l) => l.name.toLowerCase().includes(needle) || l.category.toLowerCase().includes(needle));
  }
  return list;
});

function formatPrice(price: number) {
  return new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" }).format(price);
}

const STATUS_STYLES: Record<string, string> = {
  ACTIVE: "bg-success/15 text-success",
  SOLD_OUT: "bg-medium-grey/15 text-medium-grey",
  INACTIVE: "bg-danger/15 text-danger",
};

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: "Active",
  SOLD_OUT: "Sold out",
  INACTIVE: "Inactive",
};

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const [{ data: listingData }, { data: businessData }] = await Promise.all([
      api.get<ListingDTO[]>("/listings/mine"),
      api.get<BusinessDTO[]>("/businesses/mine"),
    ]);
    listings.value = listingData;
    businesses.value = businessData;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

function manage() {
  router.push("/dashboard");
}

onMounted(load);
</script>

<template>
  <section class="mx-auto max-w-4xl space-y-5">
    <h1 class="font-display text-xl font-bold text-uni-navy">My listings</h1>
    <p class="text-sm text-medium-grey">
      Every listing across all your businesses, in one table instead of hunting per-business.
    </p>

    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex flex-wrap gap-2">
        <button
          v-for="s in ['ALL', 'ACTIVE', 'SOLD_OUT', 'INACTIVE']"
          :key="s"
          class="rounded-full border px-3.5 py-1.5 text-[13px] font-semibold"
          :class="statusFilter === s ? 'border-campus-teal bg-campus-teal text-white' : 'border-light-grey bg-white text-charcoal hover:border-campus-teal'"
          @click="statusFilter = s as typeof statusFilter"
        >
          {{ s === "ALL" ? "All" : STATUS_LABELS[s] }} &nbsp;{{ statusCountFor(s) }}
        </button>
      </div>
      <input v-model="keyword" type="search" placeholder="Search my listings..." class="input-field sm:w-64" />
    </div>

    <p v-if="error" class="text-sm text-danger">{{ error }}</p>
    <p v-else-if="loading" class="text-sm text-medium-grey">Loading...</p>
    <p v-else-if="filteredListings.length === 0" class="card text-sm text-medium-grey">No listings match this filter.</p>

    <div v-else class="card overflow-x-auto p-0">
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b border-light-grey text-left text-xs uppercase tracking-wide text-medium-grey">
            <th class="px-4 py-3">Listing</th>
            <th class="px-4 py-3">Business</th>
            <th class="px-4 py-3">Price</th>
            <th class="px-4 py-3">Status</th>
            <th class="px-4 py-3">Views</th>
            <th class="px-4 py-3"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="l in filteredListings" :key="l.id" class="border-b border-light-grey last:border-b-0">
            <td class="px-4 py-3 font-medium text-charcoal">{{ l.name }}</td>
            <td class="px-4 py-3 text-medium-grey">{{ businessNameById[l.businessId] ?? "-" }}</td>
            <td class="px-4 py-3 font-medium text-campus-teal">{{ formatPrice(l.price) }}</td>
            <td class="px-4 py-3"><span class="badge" :class="STATUS_STYLES[l.status]">{{ STATUS_LABELS[l.status] }}</span></td>
            <td class="px-4 py-3 text-medium-grey">{{ l.viewCount }}</td>
            <td class="px-4 py-3 text-right">
              <button class="text-xs font-medium text-campus-teal underline" @click="manage">Manage</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
