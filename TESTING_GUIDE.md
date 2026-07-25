# 🧪 SaaS Billing Platform — Complete Testing Guide

This guide walks you through testing **every feature** of the billing platform step by step.

---

## 📋 Prerequisites

| Tool | Required | How to Check |
|------|----------|-------------|
| Docker Desktop | ✅ | `docker --version` |
| Git | ✅ | `git --version` |
| Web Browser | ✅ | Chrome/Edge recommended |

---

## 🚀 Step 1: Start the Platform

### Option A: Docker (Recommended for testing)

```powershell
# Open PowerShell in the project root folder
.\start.ps1
```

This will:
- Start PostgreSQL database
- Build & start the Spring Boot backend
- Build & start the React frontend
- Open your browser to http://localhost:3000

### Option B: Manual Start

```powershell
# Start database
docker-compose up database -d

# Start backend (needs Java 17+)
cd backend
mvn spring-boot:run

# Start frontend (needs Node 18+)
cd frontend
npm install
npm run dev
```

### Verify Services Are Running

| Service | URL | Expected |
|---------|-----|----------|
| Frontend | http://localhost:3000 | Landing page loads |
| Backend API | http://localhost:8080/api/plans | JSON array of 3 plans |
| Swagger Docs | http://localhost:8080/swagger-ui.html | API documentation UI |
| Health Check | http://localhost:8080/actuator/health | `{"status":"UP"}` |

---

## 🔐 Step 2: Test Authentication

### 2.1 — Admin Login

1. Go to http://localhost:3000/login
2. Enter credentials:
   - **Email:** `admin@billing.local`
   - **Password:** `Admin@123`
3. Click **Sign In**
4. ✅ **Expected:** Redirected to Dashboard, sidebar shows "Admin Organization"

### 2.2 — Register a New User

1. Go to http://localhost:3000/register
2. Fill in:
   - **First Name:** John
   - **Last Name:** Doe
   - **Email:** john@example.com
   - **Password:** Test@1234
   - **Organization Name:** Acme Corp
3. Click **Create Account**
4. ✅ **Expected:** Redirected to Dashboard with "Acme Corp" shown, Basic Plan trial started automatically

### 2.3 — Test Logout & Re-Login

1. Click the **Logout** button in the sidebar
2. ✅ **Expected:** Redirected to login page
3. Login again with `john@example.com` / `Test@1234`
4. ✅ **Expected:** Dashboard loads with Acme Corp data

---

## 📊 Step 3: Test Dashboard

After logging in as John (the registered user):

1. Navigate to **Dashboard** (sidebar)
2. ✅ **Expected items visible:**
   - **Active Plan** card → shows "Basic Plan" with "TRIAL" badge
   - **Next Billing Date** card → shows a date 14 days from registration
   - **API Usage Percentage** card → shows "0.0%"
   - **Metered API Requests** chart → static demo chart
   - **Feature Limits Summary** → may be empty until usage is logged
   - **Recent Invoices** → may show "No billing history found"

---

## 💳 Step 4: Test Subscription Plans

1. Navigate to **Plans** (sidebar)
2. ✅ **Expected:** 3 plan cards displayed:
   - Basic Plan — $19/month (marked "Active Subscription")
   - Pro Plan — $49/month
   - Enterprise Plan — $299/month
3. Each plan shows features (max users, storage, API calls)

### 4.1 — Upgrade Plan

1. Click **Subscribe / Modify** on the "Pro Plan" card
2. ✅ **Expected:** Alert says "Plan changed successfully!"
3. The Pro Plan card now shows "Active Subscription" badge
4. Go back to Dashboard → Active Plan should now show "Pro Plan"

### 4.2 — Downgrade Plan

1. Go to Plans → click **Subscribe / Modify** on "Basic Plan"
2. ✅ **Expected:** Alert says "Plan changed successfully!" (downgrade)
3. Basic Plan now shows "Active Subscription" again

---

## 🧾 Step 5: Test Invoices

1. Navigate to **Invoices** (sidebar)
2. ✅ **Expected:** Invoice list table with columns:
   - Invoice Number, Date, Due Date, Subtotal, GST/Tax, Total, Status, Actions
3. If subscriptions were changed, invoices may have been generated

### 5.1 — Search & Filter

1. Type an invoice number in the search box → list filters
2. Click **PAID** / **OPEN** / **ALL** filter buttons → list updates
3. ✅ **Expected:** Filters work correctly

### 5.2 — Download Invoice PDF

1. Click **Download** on any invoice row
2. ✅ **Expected:** PDF file downloads with invoice details

### 5.3 — Email Invoice

1. Click **Email** on any invoice row
2. ✅ **Expected:** Success message appears (email may fail silently if SMTP not configured, that's OK)

---

## 📈 Step 6: Test Usage Tracking

1. Navigate to **Usage** (sidebar)
2. ✅ **Expected:** Resource usage cards for each feature (max_users, max_storage_gb, api_calls_limit)
3. Progress bars show current consumption vs plan limits

### 6.1 — Log Usage via API (Swagger)

1. Open http://localhost:8080/swagger-ui.html
2. Click **Authorize** → paste JWT token (get it from browser localStorage → `auth_token`)
3. Find **POST /api/usage/log** endpoint
4. Execute with parameters:
   - `metricKey`: `api_calls_limit`
   - `quantity`: `500`
5. ✅ **Expected:** "Usage logged successfully"
6. Go back to Usage page → refresh → bar should show updated quantity

---

## 💰 Step 7: Test Billing Page

1. Navigate to **Billing** (sidebar)
2. ✅ **Expected sections:**
   - **Promo Coupons** — input field + apply button
   - **Tax Profile** — shows Country, Currency, optionally GSTIN
   - **Transaction History** — payment records table

### 7.1 — Apply Coupon Code

1. Type `PROMO20` in the coupon input
2. Click **Apply Coupon**
3. ✅ **Expected:** Success message appears (demo mode — no real discount applied)

---

## ⚙️ Step 8: Test Settings

1. Navigate to **Settings** (sidebar)
2. ✅ **Expected form fields:**
   - Organization Name
   - Billing Email
   - Country Code (dropdown)
   - Currency (dropdown)
   - Tax ID / Business Number
   - GSTIN (shows only when India is selected)

### 8.1 — Update Settings

1. Change Organization Name to "Acme Industries"
2. Change Country to "India (IN)"
3. GSTIN field should appear — enter `27AAAAA0000A1Z5`
4. Click **Save Settings**
5. ✅ **Expected:** "Settings updated successfully!" message
6. Sidebar should update to show "Acme Industries"

---

## 🆘 Step 9: Test Support Tickets

1. Navigate to **Support** (sidebar)
2. ✅ **Expected:** Ticket submission form + ticket history table

### 9.1 — Submit a Ticket

1. Fill in:
   - **Subject:** "Need storage upgrade"
   - **Priority:** HIGH
   - **Description:** "Our team needs more than 10GB storage"
2. Click **Send Ticket**
3. ✅ **Expected:** Success message + ticket appears in the history table with status "OPEN"

---

## 🛡️ Step 10: Test Admin Panel

1. **Logout** from the user account
2. Login as admin: `admin@billing.local` / `Admin@123`
3. Navigate to **Admin Panel** (sidebar — only visible to admins)

### 10.1 — Admin Dashboard

✅ **Expected KPI cards:**
- Monthly Recurring Revenue (MRR)
- Annual Recurring Revenue (ARR)
- Active Subscribers count
- Payment Success Rate

✅ **Expected charts:**
- Monthly Revenue bar chart
- Plan Tier Distribution pie chart

### 10.2 — Resolve Support Ticket

1. In the "Pending Support Tickets" table, find the ticket created earlier
2. Click **Mark Resolved**
3. ✅ **Expected:** Ticket status changes to "RESOLVED"

### 10.3 — Export Revenue CSV

1. Click **Export Revenue CSV** button (top right)
2. ✅ **Expected:** CSV file downloads with revenue data

---

## 🔌 Step 11: Test Swagger API Directly

Open http://localhost:8080/swagger-ui.html

### Key API Endpoints to Test:

| Category | Endpoint | Method | Auth Required |
|----------|----------|--------|--------------|
| Auth | `/api/auth/login` | POST | No |
| Auth | `/api/auth/register` | POST | No |
| Plans | `/api/plans` | GET | No |
| Subscriptions | `/api/subscriptions` | GET | Yes |
| Subscriptions | `/api/subscriptions/subscribe` | POST | Yes |
| Invoices | `/api/invoices` | GET | Yes |
| Usage | `/api/usage/current` | GET | Yes |
| Usage | `/api/usage/log` | POST | Yes |
| Payments | `/api/payments/history` | GET | Yes |
| Tickets | `/api/tickets` | GET/POST | Yes |
| Settings | `/api/organizations/settings` | GET/PUT | Yes |
| Analytics | `/api/analytics/platform` | GET | Admin |
| Health | `/actuator/health` | GET | No |

### How to Authenticate in Swagger:

1. Call **POST /api/auth/login** with body:
   ```json
   { "email": "admin@billing.local", "password": "Admin@123" }
   ```
2. Copy the `token` from the response
3. Click **Authorize** button (top right)
4. Enter: `Bearer <your-token-here>`
5. Now all authenticated endpoints will work

---

## 🏗️ Step 12: Render Deployment Test

### Deploy to Render:

1. Push your code to GitHub:
   ```bash
   git add -A
   git commit -m "Full billing platform ready for deployment"
   git push origin main
   ```

2. Go to https://dashboard.render.com
3. Click **New** → **Blueprint**
4. Connect your GitHub repo
5. Render reads `render.yaml` and creates:
   - PostgreSQL database
   - Backend web service (Java)
   - Frontend static site (React)
6. Set the **VITE_API_URL** environment variable on the frontend to your backend URL:
   - Example: `https://saas-billing-backend.onrender.com`

### Verify Render Deployment:

| Check | URL | Expected |
|-------|-----|----------|
| Backend health | `https://your-backend.onrender.com/actuator/health` | `{"status":"UP"}` |
| Plans API | `https://your-backend.onrender.com/api/plans` | 3 plans JSON |
| Frontend | `https://your-frontend.onrender.com` | Landing page |

---

## 🛑 Step 13: Stop the Platform

```powershell
.\stop.ps1
# OR
docker-compose down
```

To also delete the database data:
```powershell
docker-compose down -v
```

---

## ❓ Troubleshooting

| Problem | Solution |
|---------|----------|
| Backend won't start | Check `docker-compose logs backend` for errors |
| "Unauthorized" on API calls | Login again — JWT token may have expired (24h) |
| Frontend shows blank page | Clear browser cache, check browser console (F12) |
| Database connection refused | Make sure PostgreSQL container is running: `docker ps` |
| Port already in use | Stop other services on ports 3000/8080/5432 |
| Email features don't work | Configure SMTP in `.env` file (optional feature) |

---

## 📝 Default Credentials

| Account | Email | Password | Role |
|---------|-------|----------|------|
| Admin | admin@billing.local | Admin@123 | ROLE_ADMIN |
| Test User | (register your own) | (your choice) | ROLE_ORGANIZATION |

---

## ✅ Test Checklist Summary

- [ ] Platform starts with `.\start.ps1`
- [ ] Admin login works
- [ ] New user registration works (creates org + trial subscription)
- [ ] Dashboard shows subscription info
- [ ] Plans page shows 3 plans
- [ ] Plan upgrade/downgrade works
- [ ] Invoices page loads
- [ ] Invoice PDF download works
- [ ] Usage page shows resource meters
- [ ] Billing page loads with tax profile
- [ ] Settings update works
- [ ] Support ticket creation works
- [ ] Admin panel shows analytics
- [ ] Admin can resolve tickets
- [ ] Swagger API works with JWT auth
- [ ] Platform stops cleanly
