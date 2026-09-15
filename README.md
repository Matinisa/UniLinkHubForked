# UniLinkHub

Connect. Buy. Sell & Succeed. — a centralized digital marketplace for student entrepreneurs
in university residences. See `UniLinkHub_Documentation.pdf` for the full project brief and
`UniLinkHub Brand Guide.pdf` for the visual/voice standards this UI follows.

## Stack

- **Backend:** Spring Boot 3.3 (Java 21), Spring Security + JWT, Spring Data JPA, PostgreSQL
  (H2 for tests), Domain-Driven Design package layout (`domain` / `repository` port /
  `infrastructure` adapter / `application` / `web` per bounded context).
- **Frontend:** Vue 3 + TypeScript, Vite, Pinia, Vue Router, Tailwind CSS (configured with the
  brand guide's exact colour tokens and Poppins/Inter type scale).

The project docs floated Next.js/React in one section and Vue.js in another (the two brand/
project documents don't fully agree); this build follows the Project Documentation's explicit
technical decision (Vue.js + Spring Boot, "familiar from coursework") since that's the one tied
to a rationale, and it matches the Spring Boot backend that was already scaffolded.

## What's implemented so far

MVP scope only (Project Documentation, Section 11.1) — post-MVP items (ratings, in-app
messaging, payments, appointments, notifications) are intentionally left as the empty stub
packages they started as.

- Student registration + email verification (link is logged to the console — no SMTP
  provider is wired up yet) and JWT login.
- Single-account model: any student can call "Become a Seller" from their dashboard rather
  than registering a separate seller account.
- Register/manage a business, request verification.
- Create/edit/deactivate Product or Service listings; public browse + keyword/category search;
  per-listing view counts.
- Report/flag a listing, plus an admin review queue (begin-review / resolve / dismiss).
- A minimal but functional Vue UI for all of the above: browse, listing detail + report,
  login/register, and a combined buyer/seller dashboard.

## Not built yet (next steps)

- Provider profile page (currently only listing detail exists) and buyer "saved listings" /
  "recently viewed" (the backend has no favourites endpoint yet).
- Admin UI screens (the API exists: `/api/admin/reports`, business verification review).
- Real email delivery for verification links.
- Deployment/hosting decision (Section 13.1 in the docs still flags this as open).
- Automated tests beyond the one Spring context smoke test — no unit/integration tests for
  the use cases or controllers yet.

## Running it locally

### Backend

1. Create the database once (adjust user/password as you like, then set the env vars below
   to match):
   ```sh
   psql -U postgres -c "CREATE DATABASE unilinkhub;"
   psql -U postgres -c "CREATE USER unilinkhub WITH PASSWORD 'unilinkhub';"
   psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE unilinkhub TO unilinkhub;"
   ```
2. Run the app (no local Maven install needed if you're on IntelliJ — it bundles one; or use
   `mvn` directly if you have it on PATH):
   ```sh
   mvn spring-boot:run
   ```
   Env vars you can override: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (set a real 32+ byte
   secret before this ever goes near production — see `src/main/resources/application.yml`).
3. API is served at `http://localhost:8080/api`. `mvn test` runs against an in-memory H2
   database, so tests don't need Postgres running at all.

### Frontend

```sh
cd frontend
npm install
npm run dev
```

Served at `http://localhost:5173`; Vite proxies `/api` to `localhost:8080` in dev
(`frontend/vite.config.ts`), so no CORS config is needed locally.

## Project management docs still to fill in

The Project Documentation PDF flags a few `[TEAM TO COMPLETE]` sections that are outside what
code can answer for you: team/role allocation, Trello sprint breakdown, survey sample size,
and final wireframes/Figma link.
