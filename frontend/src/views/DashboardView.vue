<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useSavedListingsStore } from "@/stores/savedListings";
import { getRecentlyViewed } from "@/lib/recentlyViewed";
import { api, extractErrorMessage } from "@/lib/api";
import ListingCard from "@/components/ListingCard.vue";
import type { BusinessDTO, ListingDTO, ReportStatus, ReportSummaryView } from "@/lib/types";

const auth = useAuthStore();
const saved = useSavedListingsStore();

const businesses = ref<BusinessDTO[]>([]);
const listingsByBusiness = ref<Record<string, ListingDTO[]>>({});
const error = ref("");
const becomingSeller = ref(false);

const newBusiness = ref({ businessName: "", description: "", category: "" });
const creatingBusiness = ref(false);

const newListing = ref({
  businessId: "",
  kind: "PRODUCT" as "PRODUCT" | "SERVICE",
  name: "",
  description: "",
  category: "",
  price: 0,
  stockQuantity: 1,
  imageUrl: "",
  durationMinutes: 30,
  availabilitySchedule: "",
});
const creatingListing = ref(false);

// ---- Buyer: recently viewed ----
const recentlyViewed = ref<ListingDTO[]>([]);

// ---- Buyer: my reports ----
const myReports = ref<ReportSummaryView[]>([]);
const reportsLoading = ref(false);
const reportsError = ref("");

const REASON_LABELS: Record<string, string> = {
  MISREPRESENTATION: "Misrepresentation",
  NON_DELIVERY: "Non-delivery",
  INAPPROPRIATE_CONDUCT: "Inappropriate conduct",
  SPAM: "Spam",
  OTHER: "Other",
};

const STATUS_STYLES: Record<ReportStatus, string> = {
  OPEN: "bg-warning/15 text-warning",
  UNDER_REVIEW: "bg-info/15 text-info",
  RESOLVED: "bg-success/15 text-success",
  DISMISSED: "bg-medium-grey/15 text-medium-grey",
};

const STATUS_LABELS: Record<ReportStatus, string> = {
  OPEN: "Open",
  UNDER_REVIEW: "Under review",
  RESOLVED: "Resolved",
  DISMISSED: "Dismissed",
};

function relativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const minutes = Math.round(diffMs / 60000);
  if (minutes < 60) return `${Math.max(minutes, 1)}m ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  return `${days}d ago`;
}

async function loadReports() {
  reportsLoading.value = true;
  reportsError.value = "";
  try {
    const { data } = await api.get<ReportSummaryView[]>("/reports/mine");
    myReports.value = data;
  } catch (err) {
    reportsError.value = extractErrorMessage(err);
  } finally {
    reportsLoading.value = false;
  }
}

// ---- Seller ----
async function loadBusinesses() {
  if (!auth.isSeller) return;
  try {
    const { data } = await api.get<BusinessDTO[]>("/businesses/mine");
    businesses.value = data;
    for (const business of data) {
      const { data: listings } = await api.get<ListingDTO[]>(`/listings/business/${business.id}`);
      listingsByBusiness.value[business.id] = listings;
    }
  } catch (err) {
    error.value = extractErrorMessage(err);
  }
}

async function becomeSeller() {
  becomingSeller.value = true;
  try {
    await auth.becomeSeller();
    await loadBusinesses();
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    becomingSeller.value = false;
  }
}

async function createBusiness() {
  creatingBusiness.value = true;
  error.value = "";
  try {
    await api.post("/businesses", newBusiness.value);
    newBusiness.value = { businessName: "", description: "", category: "" };
    await loadBusinesses();
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    creatingBusiness.value = false;
  }
}

async function createListing() {
  creatingListing.value = true;
  error.value = "";
  try {
    const payload = {
      businessId: newListing.value.businessId,
      name: newListing.value.name,
      description: newListing.value.description,
      category: newListing.value.category,
      price: newListing.value.price,
    };
    if (newListing.value.kind === "PRODUCT") {
      await api.post("/listings/products", {
        ...payload,
        stockQuantity: newListing.value.stockQuantity,
        imageUrl: newListing.value.imageUrl || null,
      });
    } else {
      await api.post("/listings/services", {
        ...payload,
        durationMinutes: newListing.value.durationMinutes,
        availabilitySchedule: newListing.value.availabilitySchedule || null,
      });
    }
    newListing.value.name = "";
    newListing.value.description = "";
    newListing.value.category = "";
    newListing.value.price = 0;
    await loadBusinesses();
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    creatingListing.value = false;
  }
}

async function deactivateListing(id: string) {
  try {
    await api.post(`/listings/${id}/deactivate`);
    await loadBusinesses();
  } catch (err) {
    error.value = extractErrorMessage(err);
  }
}

async function reactivateListing(id: string) {
  try {
    await api.post(`/listings/${id}/reactivate`);
    await loadBusinesses();
  } catch (err) {
    error.value = extractErrorMessage(err);
  }
}

// ---- Seller: edit listing ----
const editingListingId = ref<string | null>(null);
const editForm = ref({
  name: "",
  category: "",
  description: "",
  price: 0,
  status: "ACTIVE" as "ACTIVE" | "INACTIVE",
  stockQuantity: 0,
  imageUrl: "",
  durationMinutes: 0,
  availabilitySchedule: "",
});
const savingEdit = ref(false);

function startEdit(listing: ListingDTO) {
  editingListingId.value = listing.id;
  editForm.value = {
    name: listing.name,
    category: listing.category,
    description: listing.description,
    price: listing.price,
    status: listing.status === "INACTIVE" ? "INACTIVE" : "ACTIVE",
    stockQuantity: listing.stockQuantity ?? 0,
    imageUrl: listing.imageUrl ?? "",
    durationMinutes: listing.durationMinutes ?? 0,
    availabilitySchedule: listing.availabilitySchedule ?? "",
  };
}

function cancelEdit() {
  editingListingId.value = null;
}

async function saveEdit(listing: ListingDTO) {
  savingEdit.value = true;
  error.value = "";
  try {
    await api.patch(`/listings/${listing.id}`, {
      name: editForm.value.name,
      category: editForm.value.category,
      description: editForm.value.description,
      price: editForm.value.price,
      status: editForm.value.status,
      stockQuantity: listing.type === "PRODUCT" ? editForm.value.stockQuantity : undefined,
      imageUrl: listing.type === "PRODUCT" ? editForm.value.imageUrl : undefined,
      durationMinutes: listing.type === "SERVICE" ? editForm.value.durationMinutes : undefined,
      availabilitySchedule: listing.type === "SERVICE" ? editForm.value.availabilitySchedule : undefined,
    });
    editingListingId.value = null;
    await loadBusinesses();
  } catch (err) {
    error.value = extractErrorMessage(err);
  } finally {
    savingEdit.value = false;
  }
}

onMounted(async () => {
  recentlyViewed.value = getRecentlyViewed();
  await Promise.all([loadBusinesses(), loadReports(), saved.fetchSaved()]);
});
</script>

<template>
  <section class="space-y-6">
    <div class="card">
      <h1 class="font-display text-xl font-bold text-uni-navy">
        Welcome, {{ auth.user?.firstName }}
      </h1>
      <p class="text-sm text-medium-grey">{{ auth.user?.email }} · {{ auth.user?.studentNumber }}</p>
    </div>

    <p v-if="error" class="text-sm text-danger">{{ error }}</p>

    <!-- Saved listings -->
    <div>
      <div class="mb-3 flex items-center gap-2">
        <h2 class="font-display text-lg font-semibold text-uni-navy">Saved listings</h2>
        <span v-if="saved.listings.length > 0" class="text-xs text-medium-grey">{{ saved.listings.length }} saved</span>
      </div>
      <div v-if="saved.listings.length === 0" class="card text-sm text-medium-grey">
        Tap the heart on any listing to save it here for later.
      </div>
      <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <ListingCard v-for="listing in saved.listings" :key="listing.id" :listing="listing" />
      </div>
    </div>

    <!-- Recently viewed -->
    <div v-if="recentlyViewed.length > 0">
      <h2 class="mb-3 font-display text-lg font-semibold text-uni-navy">Recently viewed</h2>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <ListingCard v-for="listing in recentlyViewed" :key="listing.id" :listing="listing" />
      </div>
    </div>

    <!-- My reports -->
    <div>
      <h2 class="mb-3 font-display text-lg font-semibold text-uni-navy">Your reports</h2>
      <p v-if="reportsError" class="text-sm text-danger">{{ reportsError }}</p>
      <p v-else-if="reportsLoading" class="text-sm text-medium-grey">Loading...</p>
      <div v-else-if="myReports.length === 0" class="card text-sm text-medium-grey">
        Reports you file on listings or providers will show up here with their status.
      </div>
      <div v-else class="space-y-2">
        <div
          v-for="r in myReports"
          :key="r.id"
          class="flex items-center justify-between rounded-card border border-light-grey bg-white px-4 py-3"
        >
          <div>
            <p class="text-sm font-semibold text-uni-navy">{{ REASON_LABELS[r.reason] ?? r.reason }}</p>
            <p class="text-xs text-medium-grey">{{ r.target.label }} &middot; Filed {{ relativeTime(r.createdAt) }}</p>
          </div>
          <span class="badge" :class="STATUS_STYLES[r.status]">{{ STATUS_LABELS[r.status] }}</span>
        </div>
      </div>
    </div>

    <div class="h-px bg-light-grey"></div>

    <div v-if="!auth.isSeller" class="card space-y-3">
      <h2 class="font-display text-lg font-semibold text-uni-navy">Have something to offer?</h2>
      <p class="text-sm text-charcoal">
        Unlock seller features on this same account - no separate sign-up needed.
      </p>
      <button class="btn-primary" :disabled="becomingSeller" @click="becomeSeller">
        {{ becomingSeller ? "Unlocking..." : "Become a Seller" }}
      </button>
    </div>

    <template v-else>
      <div class="card space-y-3">
        <h2 class="font-display text-lg font-semibold text-uni-navy">Your businesses</h2>

        <div v-if="businesses.length === 0" class="text-sm text-medium-grey">
          You haven't registered a business yet - add one below to start listing.
        </div>

        <ul v-else class="space-y-2">
          <li v-for="business in businesses" :key="business.id" class="rounded-control border border-light-grey p-3">
            <div class="flex items-center justify-between">
              <span class="font-semibold text-uni-navy">{{ business.businessName }}</span>
              <span
                class="badge"
                :class="{
                  'bg-success/15 text-success': business.verificationStatus === 'VERIFIED',
                  'bg-warning/15 text-warning': business.verificationStatus === 'PENDING',
                  'bg-danger/15 text-danger': business.verificationStatus === 'REJECTED',
                }"
              >
                {{ business.verificationStatus }}
              </span>
            </div>
            <p class="text-sm text-medium-grey">{{ business.category }}</p>

            <ul class="mt-2 space-y-2">
              <li v-for="listing in listingsByBusiness[business.id] ?? []" :key="listing.id">
                <!-- Normal row -->
                <div v-if="editingListingId !== listing.id" class="flex items-center justify-between rounded-control bg-soft-grey px-3 py-2 text-sm">
                  <span :class="{ 'text-medium-grey line-through': listing.status === 'INACTIVE' }">
                    {{ listing.name }} · {{ listing.status }} · {{ listing.viewCount }} views
                  </span>
                  <div class="flex items-center gap-3">
                    <button class="text-xs font-medium text-campus-teal underline" @click="startEdit(listing)">Edit</button>
                    <button
                      v-if="listing.status === 'ACTIVE'"
                      class="text-xs font-medium text-danger underline"
                      @click="deactivateListing(listing.id)"
                    >
                      Deactivate
                    </button>
                    <button v-else class="text-xs font-medium text-success underline" @click="reactivateListing(listing.id)">
                      Reactivate
                    </button>
                  </div>
                </div>

                <!-- Edit form -->
                <div v-else class="space-y-3 rounded-control border border-campus-teal bg-white p-3">
                  <div class="flex items-center justify-between text-sm font-medium text-uni-navy">
                    <span>Editing: {{ listing.name }}</span>
                    <span class="badge bg-sky-blue/20 text-uni-navy">{{ listing.type }}</span>
                  </div>
                  <div class="grid gap-2 sm:grid-cols-2">
                    <input v-model="editForm.name" class="input-field sm:col-span-2" placeholder="Title" />
                    <input v-model="editForm.category" class="input-field" placeholder="Category" />
                    <select v-model="editForm.status" class="input-field">
                      <option value="ACTIVE">Active</option>
                      <option value="INACTIVE">Inactive</option>
                    </select>
                    <textarea v-model="editForm.description" class="input-field sm:col-span-2" rows="2"></textarea>
                    <input v-model.number="editForm.price" type="number" min="0" step="0.01" class="input-field" placeholder="Price (ZAR)" />
                    <template v-if="listing.type === 'PRODUCT'">
                      <input
                        v-model.number="editForm.stockQuantity"
                        type="number"
                        min="0"
                        class="input-field"
                        placeholder="Stock quantity"
                      />
                      <input
                        v-model="editForm.imageUrl"
                        class="input-field sm:col-span-2"
                        placeholder="Image URL (optional)"
                      />
                    </template>
                    <template v-else>
                      <input
                        v-model.number="editForm.durationMinutes"
                        type="number"
                        min="0"
                        class="input-field"
                        placeholder="Duration (minutes)"
                      />
                      <input
                        v-model="editForm.availabilitySchedule"
                        class="input-field sm:col-span-2"
                        placeholder="Availability (e.g. Weekdays 2-6pm)"
                      />
                    </template>
                  </div>
                  <div class="flex justify-end gap-2">
                    <button class="btn-secondary text-sm" @click="cancelEdit">Cancel</button>
                    <button class="btn-primary text-sm" :disabled="savingEdit" @click="saveEdit(listing)">
                      {{ savingEdit ? "Saving..." : "Save changes" }}
                    </button>
                  </div>
                </div>
              </li>
            </ul>
          </li>
        </ul>

        <form class="grid gap-2 border-t border-light-grey pt-3 sm:grid-cols-3" @submit.prevent="createBusiness">
          <input v-model="newBusiness.businessName" required placeholder="Business name" class="input-field" />
          <input v-model="newBusiness.category" required placeholder="Category" class="input-field" />
          <input v-model="newBusiness.description" required placeholder="Short description" class="input-field" />
          <button type="submit" class="btn-secondary sm:col-span-3" :disabled="creatingBusiness">
            {{ creatingBusiness ? "Adding..." : "Add business" }}
          </button>
        </form>
      </div>

      <div v-if="businesses.length > 0" class="card space-y-3">
        <h2 class="font-display text-lg font-semibold text-uni-navy">New listing</h2>
        <form class="grid gap-2 sm:grid-cols-2" @submit.prevent="createListing">
          <select v-model="newListing.businessId" required class="input-field sm:col-span-2">
            <option value="" disabled>Select business</option>
            <option v-for="business in businesses" :key="business.id" :value="business.id">
              {{ business.businessName }}
            </option>
          </select>
          <select v-model="newListing.kind" class="input-field">
            <option value="PRODUCT">Product</option>
            <option value="SERVICE">Service</option>
          </select>
          <input v-model="newListing.category" required placeholder="Category" class="input-field" />
          <input v-model="newListing.name" required placeholder="Title" class="input-field sm:col-span-2" />
          <textarea
            v-model="newListing.description"
            required
            placeholder="Description"
            class="input-field sm:col-span-2"
            rows="2"
          ></textarea>
          <input
            v-model.number="newListing.price"
            type="number"
            min="0"
            step="0.01"
            required
            placeholder="Price (ZAR)"
            class="input-field"
          />

          <input
            v-if="newListing.kind === 'PRODUCT'"
            v-model.number="newListing.stockQuantity"
            type="number"
            min="0"
            placeholder="Stock quantity"
            class="input-field"
          />
          <input
            v-if="newListing.kind === 'PRODUCT'"
            v-model="newListing.imageUrl"
            placeholder="Image URL (optional)"
            class="input-field sm:col-span-2"
          />
          <input
            v-if="newListing.kind === 'SERVICE'"
            v-model.number="newListing.durationMinutes"
            type="number"
            min="0"
            placeholder="Duration (minutes)"
            class="input-field"
          />

          <input
            v-if="newListing.kind === 'SERVICE'"
            v-model="newListing.availabilitySchedule"
            placeholder="Availability (e.g. Weekdays 2-6pm)"
            class="input-field sm:col-span-2"
          />

          <button type="submit" class="btn-primary sm:col-span-2" :disabled="creatingListing">
            {{ creatingListing ? "Publishing..." : "Publish listing" }}
          </button>
        </form>
      </div>
    </template>
  </section>
</template>
