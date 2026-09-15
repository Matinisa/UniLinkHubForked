import { defineStore } from "pinia";
import { api } from "@/lib/api";
import type { ProviderProfileDTO } from "@/lib/types";

export const useFollowedProvidersStore = defineStore("followedProviders", {
  state: () => ({
    ids: new Set<string>(),
    providers: [] as ProviderProfileDTO[],
    initialized: false,
  }),
  actions: {
    async fetchFollowed() {
      try {
        const { data } = await api.get<ProviderProfileDTO[]>("/businesses/followed/mine");
        this.providers = data;
        this.ids = new Set(data.map((p) => p.businessId));
      } finally {
        this.initialized = true;
      }
    },
    isFollowing(businessId: string): boolean {
      return this.ids.has(businessId);
    },
    async toggleFollow(profile: ProviderProfileDTO) {
      try {
        if (this.ids.has(profile.businessId)) {
          await api.delete(`/businesses/${profile.businessId}/follow`);
          this.ids.delete(profile.businessId);
          this.providers = this.providers.filter((p) => p.businessId !== profile.businessId);
        } else {
          await api.post(`/businesses/${profile.businessId}/follow`);
          this.ids.add(profile.businessId);
          this.providers.unshift(profile);
        }
      } catch (err) {
        console.error("Failed to toggle followed provider", err);
      }
    },
    reset() {
      this.ids = new Set();
      this.providers = [];
      this.initialized = false;
    },
  },
});
