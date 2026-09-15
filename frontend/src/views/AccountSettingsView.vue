<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { api, extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/stores/auth";
import type { BusinessDTO } from "@/lib/types";

const auth = useAuthStore();

// ---- Profile ----
const profileForm = ref({ firstName: "", lastName: "", phoneNumber: "" });
const savingProfile = ref(false);
const profileStatus = ref("");
const profileError = ref("");

function resetProfileForm() {
  profileForm.value = {
    firstName: auth.user?.firstName ?? "",
    lastName: auth.user?.lastName ?? "",
    phoneNumber: auth.user?.phoneNumber ?? "",
  };
}

async function saveProfile() {
  savingProfile.value = true;
  profileStatus.value = "";
  profileError.value = "";
  try {
    await auth.updateProfile(profileForm.value);
    profileStatus.value = "Profile updated.";
  } catch (err) {
    profileError.value = extractErrorMessage(err);
  } finally {
    savingProfile.value = false;
  }
}

// ---- Business ----
const businesses = ref<BusinessDTO[]>([]);
const selectedBusinessId = ref("");
const businessForm = ref({ businessName: "", description: "", category: "", imageUrl: "" });
const savingBusiness = ref(false);
const businessStatus = ref("");
const businessError = ref("");

const selectedBusiness = computed(() => businesses.value.find((b) => b.id === selectedBusinessId.value) ?? null);

watch(selectedBusiness, (business) => {
  if (business) {
    businessForm.value = {
      businessName: business.businessName,
      description: business.description,
      category: business.category,
      imageUrl: business.imageUrl ?? "",
    };
  }
});

async function loadBusinesses() {
  if (!auth.isSeller) return;
  try {
    const { data } = await api.get<BusinessDTO[]>("/businesses/mine");
    businesses.value = data;
    if (data.length > 0) {
      selectedBusinessId.value = data[0].id;
    }
  } catch (err) {
    businessError.value = extractErrorMessage(err);
  }
}

async function saveBusiness() {
  if (!selectedBusiness.value) return;
  savingBusiness.value = true;
  businessStatus.value = "";
  businessError.value = "";
  try {
    const { data } = await api.patch<BusinessDTO>(`/businesses/${selectedBusiness.value.id}`, businessForm.value);
    const index = businesses.value.findIndex((b) => b.id === data.id);
    if (index !== -1) businesses.value[index] = data;
    businessStatus.value = "Business details updated.";
  } catch (err) {
    businessError.value = extractErrorMessage(err);
  } finally {
    savingBusiness.value = false;
  }
}

const resubmitting = ref(false);

async function resubmitVerification() {
  if (!selectedBusiness.value) return;
  resubmitting.value = true;
  businessStatus.value = "";
  businessError.value = "";
  try {
    const { data } = await api.post<BusinessDTO>(`/businesses/${selectedBusiness.value.id}/request-verification`);
    const index = businesses.value.findIndex((b) => b.id === data.id);
    if (index !== -1) businesses.value[index] = data;
    businessStatus.value = "Resubmitted - an admin will take another look.";
  } catch (err) {
    businessError.value = extractErrorMessage(err);
  } finally {
    resubmitting.value = false;
  }
}

// ---- Password ----
const passwordForm = ref({ currentPassword: "", newPassword: "", confirmPassword: "" });
const savingPassword = ref(false);
const passwordStatus = ref("");
const passwordError = ref("");

async function savePassword() {
  passwordStatus.value = "";
  passwordError.value = "";
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = "New password and confirmation do not match.";
    return;
  }
  savingPassword.value = true;
  try {
    await api.post("/users/me/change-password", {
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
    });
    passwordStatus.value = "Password updated.";
    passwordForm.value = { currentPassword: "", newPassword: "", confirmPassword: "" };
  } catch (err) {
    passwordError.value = extractErrorMessage(err);
  } finally {
    savingPassword.value = false;
  }
}

onMounted(() => {
  resetProfileForm();
  loadBusinesses();
});
</script>

<template>
  <section class="mx-auto max-w-2xl space-y-6">
    <h1 class="font-display text-xl font-bold text-uni-navy">My account</h1>

    <!-- Profile -->
    <div class="card space-y-3">
      <h2 class="font-display text-base font-semibold text-uni-navy">Your profile</h2>
      <div class="grid gap-3 sm:grid-cols-2">
        <div>
          <label class="mb-1 block text-xs font-medium text-medium-grey">First name</label>
          <input v-model="profileForm.firstName" class="input-field" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-medium text-medium-grey">Last name</label>
          <input v-model="profileForm.lastName" class="input-field" />
        </div>
        <div class="sm:col-span-2">
          <label class="mb-1 block text-xs font-medium text-medium-grey">Email</label>
          <input :value="auth.user?.email" disabled class="input-field bg-soft-grey text-medium-grey" />
        </div>
        <div class="sm:col-span-2">
          <label class="mb-1 block text-xs font-medium text-medium-grey">Contact number</label>
          <input v-model="profileForm.phoneNumber" class="input-field" />
        </div>
      </div>
      <p v-if="profileError" class="text-sm text-danger">{{ profileError }}</p>
      <p v-else-if="profileStatus" class="text-sm text-success">{{ profileStatus }}</p>
      <div class="flex justify-end">
        <button class="btn-primary text-sm" :disabled="savingProfile" @click="saveProfile">
          {{ savingProfile ? "Saving..." : "Save profile" }}
        </button>
      </div>
    </div>

    <!-- Business details -->
    <div v-if="auth.isSeller && businesses.length > 0" class="card space-y-3">
      <div class="flex items-center justify-between">
        <h2 class="font-display text-base font-semibold text-uni-navy">Business details</h2>
        <select v-model="selectedBusinessId" class="input-field w-40 text-sm">
          <option v-for="b in businesses" :key="b.id" :value="b.id">{{ b.businessName }}</option>
        </select>
      </div>

      <div v-if="selectedBusiness?.verificationStatus === 'REJECTED'" class="flex items-start gap-2.5 rounded-control bg-danger/10 p-3">
        <span class="badge bg-danger/15 text-danger shrink-0">Rejected</span>
        <p class="text-[13px] text-charcoal">
          This business wasn't approved on its last review. Update the details below if needed,
          then resubmit for another look.
        </p>
      </div>

      <div class="flex items-center gap-3">
        <div
          v-if="businessForm.imageUrl"
          class="h-16 w-16 shrink-0 rounded-full border border-light-grey bg-cover bg-center"
          :style="{ backgroundImage: `url(${businessForm.imageUrl})` }"
        ></div>
        <div v-else class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full border border-light-grey bg-soft-grey text-xs text-medium-grey">
          No logo
        </div>
        <div class="flex-1 space-y-1.5">
          <label class="block text-xs font-medium text-medium-grey">Logo URL <span class="font-normal">(optional)</span></label>
          <input v-model="businessForm.imageUrl" placeholder="https://..." class="input-field" />
        </div>
      </div>

      <div class="grid gap-3 sm:grid-cols-2">
        <div class="sm:col-span-2">
          <label class="mb-1 block text-xs font-medium text-medium-grey">Business name</label>
          <input v-model="businessForm.businessName" class="input-field" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-medium text-medium-grey">Category</label>
          <input v-model="businessForm.category" class="input-field" />
        </div>
        <div>
          <label class="mb-1 block text-xs font-medium text-medium-grey">Status</label>
          <input
            :value="selectedBusiness?.verificationStatus"
            disabled
            class="input-field bg-soft-grey font-medium"
            :class="{
              'text-success': selectedBusiness?.verificationStatus === 'VERIFIED',
              'text-warning': selectedBusiness?.verificationStatus === 'PENDING',
              'text-danger': selectedBusiness?.verificationStatus === 'REJECTED',
            }"
          />
        </div>
        <div class="sm:col-span-2">
          <label class="mb-1 block text-xs font-medium text-medium-grey">Description</label>
          <textarea v-model="businessForm.description" class="input-field" rows="2"></textarea>
        </div>
      </div>
      <p v-if="businessError" class="text-sm text-danger">{{ businessError }}</p>
      <p v-else-if="businessStatus" class="text-sm text-success">{{ businessStatus }}</p>
      <div class="flex flex-wrap justify-end gap-2">
        <button class="btn-secondary text-sm" :disabled="savingBusiness" @click="saveBusiness">
          {{ savingBusiness ? "Saving..." : "Save business" }}
        </button>
        <button
          v-if="selectedBusiness?.verificationStatus === 'REJECTED'"
          class="btn-primary text-sm"
          :disabled="resubmitting"
          @click="resubmitVerification"
        >
          {{ resubmitting ? "Resubmitting..." : "Resubmit for verification" }}
        </button>
      </div>
    </div>

    <!-- Change password -->
    <div class="card space-y-3">
      <h2 class="font-display text-base font-semibold text-uni-navy">Change password</h2>
      <div class="grid gap-3">
        <div>
          <label class="mb-1 block text-xs font-medium text-medium-grey">Current password</label>
          <input v-model="passwordForm.currentPassword" type="password" class="input-field" />
        </div>
        <div class="grid gap-3 sm:grid-cols-2">
          <div>
            <label class="mb-1 block text-xs font-medium text-medium-grey">New password</label>
            <input v-model="passwordForm.newPassword" type="password" placeholder="At least 8 characters" class="input-field" />
          </div>
          <div>
            <label class="mb-1 block text-xs font-medium text-medium-grey">Confirm new password</label>
            <input v-model="passwordForm.confirmPassword" type="password" class="input-field" />
          </div>
        </div>
      </div>
      <p v-if="passwordError" class="text-sm text-danger">{{ passwordError }}</p>
      <p v-else-if="passwordStatus" class="text-sm text-success">{{ passwordStatus }}</p>
      <div class="flex justify-end">
        <button class="btn-primary text-sm" :disabled="savingPassword" @click="savePassword">
          {{ savingPassword ? "Updating..." : "Update password" }}
        </button>
      </div>
    </div>
  </section>
</template>
