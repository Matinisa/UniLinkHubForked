import { defineStore } from "pinia";
import { api } from "@/lib/api";
import type { ListingDTO } from "@/lib/types";

export const useSavedListingsStore = defineStore("savedListings", {
  state: () => ({
    ids: new Set<string>(),
    listings: [] as ListingDTO[],
    initialized: false,
  }),
  actions: {
    async fetchSaved() {
      try {
        const { data } = await api.get<ListingDTO[]>("/listings/saved/mine");
        this.listings = data;
        this.ids = new Set(data.map((l) => l.id));
      } finally {
        this.initialized = true;
      }
    },
    isSaved(listingId: string): boolean {
      return this.ids.has(listingId);
    },
    async toggleSave(listing: ListingDTO) {
      try {
        if (this.ids.has(listing.id)) {
          await api.delete(`/listings/${listing.id}/save`);
          this.ids.delete(listing.id);
          this.listings = this.listings.filter((l) => l.id !== listing.id);
        } else {
          await api.post(`/listings/${listing.id}/save`);
          this.ids.add(listing.id);
          this.listings.unshift(listing);
        }
      } catch (err) {
        console.error("Failed to toggle saved listing", err);
      }
    },
    reset() {
      this.ids = new Set();
      this.listings = [];
      this.initialized = false;
    },
  },
});
