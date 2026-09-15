# UniLinkHub

Connect. Buy. Sell & Succeed. — a centralized digital marketplace for student entrepreneurs
in university residences. See `UniLinkHub_Documentation.pdf` for the full project brief and
`UniLinkHub Brand Guide.pdf` for the visual/voice standards this UI follows.

## Stack

- **Backend:** Spring Boot 3.3 (Java 21), Spring Security + JWT, Spring Data JPA, MySQL
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
  provider is wired up yet) and JWT login. As a fallback to that (since there's no real inbox
  to click the link from), the admin console can also approve a pending account directly.
- Single-account model: any student can call "Become a Seller" from their dashboard rather
  than registering a separate seller account.
- Register/manage a business, request verification.
- Create/edit/deactivate/reactivate Product or Service listings, editable inline from the
  dashboard (title, category, description, price, stock/duration/availability, status).
- Public browse with keyword/category search, price range, product/service type, verified-
  sellers-only, and sort (newest, price asc/desc, most viewed); per-listing view counts.
- Report/flag a listing, plus an admin review queue (begin-review / resolve / dismiss) that
  resolves reporter/target ids into names for display, and a status-counts endpoint.
- Business verification: admin can list pending businesses and verify/reject them.
- Student account approval: admin can list accounts stuck in PENDING_VERIFICATION and approve
  them directly, unblocking login without needing to click the (console-only) verification
  link.
- Saved/favourited listings (heart toggle on any listing card or the listing detail page),
  recently viewed listings (tracked client-side, per browser), and a "your reports" status
  list - the Buyer Dashboard requirements from Section 11.1, all on the same single dashboard
  alongside the seller section.
- Provider profile page (business info, verification badge, stats, their active listings).
- Account settings page ("My account"): edit profile, edit business details, and change
  password (with current-password verification).
- A functional Vue UI for all of the above: browse with filters, listing detail + report,
  provider profile, login/register, account settings, a combined buyer/seller dashboard, and
  an admin console (report queue, business verification, student account approval) gated by
  role.

All of the above has been exercised end-to-end against a real MySQL database (see the smoke
test script below) — it isn't just "compiles", it actually runs.

## Not built yet (next steps)

- Any way to promote a user to ADMIN other than a direct SQL `UPDATE` (see below) — there's no
  self-service or seed-admin flow yet.
- Real email delivery for verification links.
- Deployment/hosting decision (Section 13.1 in the docs still flags this as open).
- Automated tests beyond the one Spring context smoke test — no unit/integration tests for
  the use cases or controllers yet.

## Database: MySQL setup

The app talks to MySQL via `spring-boot-starter-data-jpa` + `mysql-connector-j`, and creates/
updates its own schema on startup (`spring.jpa.hibernate.ddl-auto=update` in
`src/main/resources/application.yml`) — **you do not need to write `CREATE TABLE` statements
yourself.** You only need to create the database and a user once.

### 1. Create the database and app user

Run this once against your MySQL server (as root, or whichever account can create databases):

```sql
CREATE DATABASE IF NOT EXISTS unilinkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'unilinkhub'@'localhost' IDENTIFIED BY 'unilinkhub';
GRANT ALL PRIVILEGES ON unilinkhub.* TO 'unilinkhub'@'localhost';
FLUSH PRIVILEGES;
```

From a shell: `mysql -u root -p < setup.sql` (paste the block above into `setup.sql`), or run it
interactively in `mysql -u root -p`. If you're on the bundled XAMPP MySQL with the default empty
root password, drop `-p`.

If you'd rather use different credentials, change the `IDENTIFIED BY '...'` password above and
set `DB_USERNAME` / `DB_PASSWORD` env vars to match when you run the app (defaults are both
`unilinkhub`, matching the block above).

### 2. The schema Hibernate creates

For reference (e.g. if you need to hand in a schema, or want to create tables without starting
the app), this is the actual DDL Hibernate generates from the entities — captured with
`SHOW CREATE TABLE <name>` after a real run:

```sql
CREATE TABLE `users` (
  `id` binary(16) NOT NULL,
  `account_status` enum('ACTIVE','DEACTIVATED','PENDING_VERIFICATION','SUSPENDED') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(254) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone_number` varchar(32) DEFAULT NULL,
  `role` enum('ADMIN','STUDENT') NOT NULL,
  `is_seller` bit(1) NOT NULL,
  `student_number` varchar(32) NOT NULL,
  `verification_token` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_student_number` (`student_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `businesses` (
  `id` binary(16) NOT NULL,
  `business_name` varchar(150) NOT NULL,
  `category` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(1000) NOT NULL,
  `owner_id` binary(16) NOT NULL,
  `verification_status` enum('PENDING','REJECTED','VERIFIED') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Product and Service both live in this one table (single-table inheritance);
-- listing_type discriminates between them, and each subtype's own columns are just NULL
-- on rows of the other type.
CREATE TABLE `listings` (
  `listing_type` varchar(31) NOT NULL,
  `id` binary(16) NOT NULL,
  `business_id` binary(16) NOT NULL,
  `category` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(2000) NOT NULL,
  `name` varchar(150) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `status` enum('ACTIVE','INACTIVE','SOLD_OUT') NOT NULL,
  `view_count` bigint(20) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `stock_quantity` int(11) DEFAULT NULL,
  `availability_schedule` varchar(255) DEFAULT NULL,
  `duration_minutes` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `reports` (
  `id` binary(16) NOT NULL,
  `admin_note` varchar(2000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `details` varchar(2000) DEFAULT NULL,
  `reason` enum('INAPPROPRIATE_CONDUCT','MISREPRESENTATION','NON_DELIVERY','OTHER','SPAM') NOT NULL,
  `reporter_id` binary(16) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `reviewed_by_admin_id` binary(16) DEFAULT NULL,
  `status` enum('DISMISSED','OPEN','RESOLVED','UNDER_REVIEW') NOT NULL,
  `target_id` binary(16) NOT NULL,
  `target_type` enum('LISTING','USER') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

`id` columns are `binary(16)` because entity IDs are Java `UUID`s.

### 3. Useful queries once you've used the app for a bit

```sql
-- See registered users and their verification/seller state
SELECT student_number, email, account_status, is_seller, role FROM unilinkhub.users;

-- See listings with their owning business
SELECT l.name, l.listing_type, l.price, l.status, l.view_count, b.business_name
FROM unilinkhub.listings l
JOIN unilinkhub.businesses b ON b.id = l.business_id;

-- Open reports waiting for admin review
SELECT id, reason, details, status, created_at FROM unilinkhub.reports WHERE status = 'OPEN';

-- Promote a user to ADMIN (there's no API for this yet - see "Not built yet")
UPDATE unilinkhub.users SET role = 'ADMIN' WHERE email = 'someone@mycput.ac.za';
```

## Running it locally

### Backend

1. Run the two `CREATE DATABASE` / `CREATE USER` statements above, once.
2. Start the app (no local Maven install needed if you're on IntelliJ — it bundles one; or use
   `mvn` directly if you have it on PATH):
   ```sh
   mvn spring-boot:run
   ```
   Env vars you can override: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (set a real 32+ byte
   secret before this ever goes near production), `SERVER_PORT` (defaults to **8081**, not
   8080 — picked to avoid clashing with another local Spring Boot project on this machine;
   change it back in `application.yml` if that's not an issue for you).
3. API is served at `http://localhost:8081/api`. `mvn test` runs against an in-memory H2
   database, so tests don't need MySQL running at all.

### Frontend

```sh
cd frontend
npm install
npm run dev
```

Served at `http://localhost:5173` (or the next free port if that's taken); Vite proxies `/api`
to `localhost:8081` in dev (`frontend/vite.config.ts`), so no CORS config is needed locally.

## Testing the system end-to-end (curl)

This is the exact sequence used to verify the backend against a real MySQL database - register,
verify, log in, become a seller, create a business + listing, browse/search publicly, file a
report, and review it as an admin. Save as `smoke-test.sh` and run with the backend up:

```sh
#!/usr/bin/env bash
set -e
API=http://localhost:8081/api

extract() { echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | cut -d'"' -f4; }

echo "== register =="
curl -s -X POST "$API/auth/register" -H "Content-Type: application/json" -d '{
  "studentNumber": "222357614",
  "firstName": "Siphokuhle",
  "lastName": "Test",
  "email": "siphokuhle.test@mycput.ac.za",
  "password": "password123"
}'; echo

echo "Grab the verification token from the backend console log:"
echo '  grep "Verification link" <backend log>'
read -rp "Paste the token here: " VERIFY_TOKEN

echo "== verify =="
curl -s "$API/auth/verify?token=$VERIFY_TOKEN"; echo

echo "== login =="
LOGIN_JSON=$(curl -s -X POST "$API/auth/login" -H "Content-Type: application/json" -d '{
  "email": "siphokuhle.test@mycput.ac.za",
  "password": "password123"
}')
JWT=$(extract "$LOGIN_JSON" "token")
echo "$LOGIN_JSON"; echo

echo "== become seller =="
curl -s -X POST "$API/users/me/become-seller" -H "Authorization: Bearer $JWT"; echo

echo "== create business =="
BUSINESS_JSON=$(curl -s -X POST "$API/businesses" -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" -d '{
  "businessName": "Siphos Prints",
  "description": "Affordable printing and binding for res students",
  "category": "Printing"
}')
echo "$BUSINESS_JSON"
BUSINESS_ID=$(extract "$BUSINESS_JSON" "id")

echo "== create a product listing =="
LISTING_JSON=$(curl -s -X POST "$API/listings/products" -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" -d "{
  \"businessId\": \"$BUSINESS_ID\",
  \"name\": \"A4 Black & White Printing\",
  \"description\": \"10c per page, same-day turnaround\",
  \"category\": \"Printing\",
  \"price\": 0.10,
  \"stockQuantity\": 5000
}")
echo "$LISTING_JSON"
LISTING_ID=$(extract "$LISTING_JSON" "id")

echo "== public search (no auth needed) =="
curl -s "$API/listings?keyword=printing"; echo

echo "== public listing detail (view count increments each call) =="
curl -s "$API/listings/$LISTING_ID"; echo

echo "== file a report =="
curl -s -X POST "$API/reports" -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" -d "{
  \"targetType\": \"LISTING\",
  \"targetId\": \"$LISTING_ID\",
  \"reason\": \"MISREPRESENTATION\",
  \"details\": \"Testing the report flow\"
}"; echo

echo "To review it as admin, promote this account first:"
echo "  UPDATE unilinkhub.users SET role='ADMIN' WHERE email='siphokuhle.test@mycput.ac.za';"
echo "...then log in again for a token with ROLE_ADMIN and call:"
echo "  GET  $API/admin/reports?status=OPEN"
echo "  POST $API/admin/reports/{id}/resolve   -d '{\"note\": \"...\"}'"
```

## Project management docs still to fill in

The Project Documentation PDF flags a few `[TEAM TO COMPLETE]` sections that are outside what
code can answer for you: team/role allocation, Trello sprint breakdown, survey sample size,
and final wireframes/Figma link.
