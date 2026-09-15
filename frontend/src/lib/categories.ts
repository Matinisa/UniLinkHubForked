import { ref } from "vue";
import { api } from "@/lib/api";

const categories = ref<string[]>([]);
let loaded = false;

export function useCategories() {
  if (!loaded) {
    loaded = true;
    api
      .get<string[]>("/categories")
      .then(({ data }) => {
        categories.value = data;
      })
      .catch(() => {
        loaded = false;
      });
  }
  return categories;
}
