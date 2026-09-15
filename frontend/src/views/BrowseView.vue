<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import { useCategories } from "@/lib/categories";
import type { ListingDTO } from "@/lib/types";
import ListingCard from "@/components/ListingCard.vue";

const listings = ref<ListingDTO[]>([]);
const categories = useCategories();
const categoryCounts = ref<Record<string, number>>({});
const trending = ref<ListingDTO[]>([]);
const keyword = ref("");
const category = ref("");
const minPrice = ref("");
const maxPrice = ref("");
const kind = ref<"ALL" | "PRODUCT" | "SERVICE">("ALL");
const verifiedOnly = ref(false);
const sort = ref("newest");
const mobileFiltersOpen = ref(false);
const loading = ref(false);
const error = ref("");

let debounceHandle: ReturnType<typeof setTimeout> | undefined;

async function loadCategoryCounts() {
  try {
    const { data } = await api.get<ListingDTO[]>("/listings");
    const counts: Record<string, number> = {};
    for (const l of data) counts[l.category] = (counts[l.category] ?? 0) + 1;
    categoryCounts.value = counts;
  } catch {
    // Category tile counts are a nice-to-have; ignore failures here.
  }
}

async function loadTrending() {
  try {
    const { data } = await api.get<ListingDTO[]>("/listings", { params: { sort: "views" } });
    trending.value = data.slice(0, 4);
  } catch {
    // Trending is a nice-to-have; ignore failures here.
  }
}

function browseCategory(c: string) {
  category.value = c;
}

async function search() {
  loading.value = true;
  error.value = "";
  try {
    const { data } = await api.get<ListingDTO[]>("/listings", {
      params: {
        keyword: keyword.value || undefined,
        category: category.value || undefined,
        minPrice: minPrice.value || undefined,
        maxPrice: maxPrice.value || undefined,
        type: kind.value === "ALL" ? undefined : kind.value,
        verifiedOnly: verifiedOnly.value || undefined,
        sort: sort.value,
      },
    });
    listings.value = data;
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

function clearFilters() {
  category.value = "";
  minPrice.value = "";
  maxPrice.value = "";
  kind.value = "ALL";
  verifiedOnly.value = false;
  sort.value = "newest";
}

const resultsLabel = computed(() =>
  listings.value.length === 1 ? "1 listing found" : `${listings.value.length} listings found`,
);

watch([keyword, category, minPrice, maxPrice, kind, verifiedOnly, sort], () => {
  clearTimeout(debounceHandle);
  debounceHandle = setTimeout(search, 250);
});

onMounted(() => {
  loadCategoryCounts();
  loadTrending();
  search();
});
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

    <!-- Browse by category -->
    <div v-if="categories.length > 0">
      <h2 class="mb-3 font-display text-lg font-semibold text-uni-navy">Browse by category</h2>
      <div class="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-8">
        <button
          v-for="c in categories"
          :key="c"
          class="card flex flex-col items-center gap-1 py-4 text-center transition hover:shadow-md"
          @click="browseCategory(c)"
        >
          <span class="text-sm font-semibold text-uni-navy">{{ c }}</span>
          <span class="text-xs text-medium-grey">{{ categoryCounts[c] ?? 0 }} listings</span>
        </button>
      </div>
    </div>

    <!-- Trending -->
    <div v-if="trending.length > 0">
      <div class="mb-3 flex items-center gap-2">
        <span class="text-lg">🔥</span>
        <h2 class="font-display text-lg font-semibold text-uni-navy">Trending this week</h2>
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <ListingCard v-for="listing in trending" :key="listing.id" :listing="listing" />
      </div>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row">
      <input
        v-model="keyword"
        type="search"
        placeholder="Search listings..."
        class="input-field sm:max-w-sm"
      />
      <button class="btn-secondary flex items-center gap-2 text-sm lg:hidden" @click="mobileFiltersOpen = !mobileFiltersOpen">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#163D72" stroke-width="2">
          <path d="M4 6h16M7 12h10M10 18h4" />
        </svg>
        Filters
      </button>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-[240px_1fr]">
      <aside class="card h-fit space-y-5" :class="mobileFiltersOpen ? 'block' : 'hidden lg:block'">
        <div class="flex items-center justify-between">
          <h2 class="font-display text-sm font-semibold text-uni-navy">Filters</h2>
          <button class="text-xs font-medium text-campus-teal underline" @click="clearFilters">Clear all</button>
        </div>

        <div class="space-y-2">
          <p class="text-xs font-semibold uppercase tracking-wide text-medium-grey">Category</p>
          <div class="flex flex-wrap gap-2">
            <button
              class="badge cursor-pointer"
              :class="category === '' ? 'bg-uni-navy text-white' : 'bg-soft-grey text-charcoal'"
              @click="category = ''"
            >
              All
            </button>
            <button
              v-for="c in categories"
              :key="c"
              class="badge cursor-pointer"
              :class="category === c ? 'bg-uni-navy text-white' : 'bg-soft-grey text-charcoal'"
              @click="category = c"
            >
              {{ c }}
            </button>
          </div>
        </div>

        <div class="space-y-2 border-t border-light-grey pt-4">
          <p class="text-xs font-semibold uppercase tracking-wide text-medium-grey">Price range (ZAR)</p>
          <div class="flex items-center gap-2">
            <input v-model="minPrice" type="number" min="0" placeholder="Min" class="input-field" />
            <span class="text-medium-grey">-</span>
            <input v-model="maxPrice" type="number" min="0" placeholder="Max" class="input-field" />
          </div>
        </div>

        <div class="space-y-2 border-t border-light-grey pt-4">
          <p class="text-xs font-semibold uppercase tracking-wide text-medium-grey">Type</p>
          <label class="flex items-center gap-2 text-sm text-charcoal">
            <input v-model="kind" type="radio" value="ALL" class="accent-campus-teal" /> All
          </label>
          <label class="flex items-center gap-2 text-sm text-charcoal">
            <input v-model="kind" type="radio" value="PRODUCT" class="accent-campus-teal" /> Products
          </label>
          <label class="flex items-center gap-2 text-sm text-charcoal">
            <input v-model="kind" type="radio" value="SERVICE" class="accent-campus-teal" /> Services
          </label>
        </div>

        <div class="border-t border-light-grey pt-4">
          <label class="flex items-center gap-2 text-sm text-charcoal">
            <input v-model="verifiedOnly" type="checkbox" class="accent-campus-teal" /> Verified sellers only
          </label>
        </div>
      </aside>

      <div class="space-y-4">
        <div class="flex items-center justify-between">
          <p class="text-sm text-medium-grey">{{ resultsLabel }}</p>
          <div class="flex items-center gap-2">
            <label class="text-xs text-medium-grey">Sort by</label>
            <select v-model="sort" class="input-field w-44">
              <option value="newest">Newest</option>
              <option value="price_asc">Price: Low to high</option>
              <option value="price_desc">Price: High to low</option>
              <option value="views">Most viewed</option>
            </select>
          </div>
        </div>

        <p v-if="error" class="text-sm text-danger">{{ error }}</p>
        <p v-else-if="loading" class="text-sm text-medium-grey">Loading listings...</p>
        <p v-else-if="listings.length === 0" class="text-sm text-medium-grey">
          No listings match your filters yet. Try widening your search.
        </p>

        <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <ListingCard v-for="listing in listings" :key="listing.id" :listing="listing" />
        </div>
      </div>
    </div>
  </section>
</template>
