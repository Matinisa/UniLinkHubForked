import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { useSavedListingsStore } from "@/stores/savedListings";
import { useFollowedProvidersStore } from "@/stores/followedProviders";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "browse", component: () => import("@/views/BrowseView.vue") },
    { path: "/listings/:id", name: "listing-detail", component: () => import("@/views/ListingDetailView.vue") },
    { path: "/providers", name: "provider-directory", component: () => import("@/views/ProviderDirectoryView.vue") },
    { path: "/providers/:businessId", name: "provider-profile", component: () => import("@/views/ProviderProfileView.vue") },
    { path: "/login", name: "login", component: () => import("@/views/LoginView.vue") },
    { path: "/register", name: "register", component: () => import("@/views/RegisterView.vue") },
    { path: "/forgot-password", name: "forgot-password", component: () => import("@/views/ForgotPasswordView.vue") },
    {
      path: "/dashboard",
      name: "dashboard",
      component: () => import("@/views/DashboardView.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/account",
      name: "account-settings",
      component: () => import("@/views/AccountSettingsView.vue"),
      meta: { requiresAuth: true },
    },
    {
      path: "/admin",
      name: "admin-dashboard",
      component: () => import("@/views/admin/AdminDashboardView.vue"),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    { path: "/:pathMatch(.*)*", name: "not-found", component: () => import("@/views/NotFoundView.vue") },
  ],
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (!auth.initialized) {
    await auth.fetchCurrentUser();
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: "login", query: { redirect: to.fullPath } };
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: "browse" };
  }

  const saved = useSavedListingsStore();
  if (auth.isAuthenticated && !saved.initialized) {
    saved.fetchSaved().catch(() => {});
  }

  const followed = useFollowedProvidersStore();
  if (auth.isAuthenticated && !followed.initialized) {
    followed.fetchFollowed().catch(() => {});
  }

  return true;
});

export default router;
