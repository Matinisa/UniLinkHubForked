import type { ListingDTO } from "./types";

const KEY = "unilinkhub.recentlyViewed";
const MAX_ITEMS = 8;

export function recordView(listing: ListingDTO) {
  try {
    const items = getRecentlyViewed().filter((l) => l.id !== listing.id);
    items.unshift(listing);
    localStorage.setItem(KEY, JSON.stringify(items.slice(0, MAX_ITEMS)));
  } catch {
    // localStorage unavailable (private browsing, etc.) - recently viewed just won't persist.
  }
}

export function getRecentlyViewed(): ListingDTO[] {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as ListingDTO[]) : [];
  } catch {
    return [];
  }
}
