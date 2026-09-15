import { defineStore } from "pinia";
import { api } from "@/lib/api";
import type { NotificationDTO } from "@/lib/types";

export const useNotificationsStore = defineStore("notifications", {
  state: () => ({
    notifications: [] as NotificationDTO[],
    unreadCount: 0,
    initialized: false,
  }),
  actions: {
    async fetchAll() {
      try {
        const { data } = await api.get<NotificationDTO[]>("/notifications");
        this.notifications = data;
      } finally {
        this.initialized = true;
      }
    },
    async fetchUnreadCount() {
      try {
        const { data } = await api.get<{ count: number }>("/notifications/unread-count");
        this.unreadCount = data.count;
      } catch {
        // The bell badge is a nice-to-have; ignore failures here.
      }
    },
    async markAllRead() {
      try {
        await api.post("/notifications/mark-all-read");
        this.notifications = this.notifications.map((n) => ({ ...n, read: true }));
        this.unreadCount = 0;
      } catch {
        // Ignore - the user can retry by reopening the bell.
      }
    },
    reset() {
      this.notifications = [];
      this.unreadCount = 0;
      this.initialized = false;
    },
  },
});
