/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        // Brand Identity Standards Manual, Section 3.4 - Colour System
        "uni-navy": "#163D72",
        "campus-teal": "#2A9BB4",
        "academic-gold": "#B89A5E",
        "sky-blue": "#79C7E8",
        "slate-blue": "#5E78A6",
        "soft-grey": "#F5F7FA",
        "light-grey": "#D9E1EA",
        "medium-grey": "#8A94A6",
        charcoal: "#2C3440",
        success: "#16A34A",
        warning: "#F59E0B",
        danger: "#DC2626",
        info: "#2563EB",
      },
      fontFamily: {
        display: ["Poppins", "system-ui", "sans-serif"],
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "monospace"],
      },
      borderRadius: {
        card: "12px",
        control: "8px",
        modal: "16px",
      },
    },
  },
  plugins: [],
};
