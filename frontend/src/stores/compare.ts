import { defineStore } from "pinia";

const MAX_COMPARE = 3;

export const useCompareStore = defineStore("compare", {
  state: () => ({
    ids: [] as string[],
  }),
  getters: {
    isComparing: (state) => (listingId: string) => state.ids.includes(listingId),
    isFull: (state) => state.ids.length >= MAX_COMPARE,
  },
  actions: {
    toggle(listingId: string) {
      const index = this.ids.indexOf(listingId);
      if (index !== -1) {
        this.ids.splice(index, 1);
      } else if (this.ids.length < MAX_COMPARE) {
        this.ids.push(listingId);
      }
    },
    remove(listingId: string) {
      this.ids = this.ids.filter((id) => id !== listingId);
    },
    clear() {
      this.ids = [];
    },
  },
});
