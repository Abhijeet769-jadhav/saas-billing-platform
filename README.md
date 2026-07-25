# 💳 SaaS Billing & Subscription Management Platform

A production-ready, full-stack billing system inspired by **Stripe Billing**, **Chargebee**, and **Zuora**. Manage subscriptions, generate invoices, track usage, and handle payments — all from a beautiful dashboard.

---

## ✨ Features

| Category | Features |
|----------|----------|
| **Auth** | JWT login/register, role-based access (Admin, Organization, User), password reset |
| **Subscriptions** | Subscribe, upgrade, downgrade, pause, resume, cancel — with full history |
| **Plans** | 3 tiers (Basic $19, Pro $49, Enterprise $299), feature limits per plan |
| **Invoices** | Auto-generated invoices, PDF download, email dispatch, GST/tax calculation |
| **Usage Tracking** | Metered feature usage (API calls, storage, users) with limit enforcement |
| **Payments** | Stripe integration (mock mode included), payment history, refunds, retries |
| **Billing** | Coupon codes, tax profiles (GSTIN/GST), multi-currency support |
| **Admin Panel** | MRR/ARR analytics, plan distribution charts, support ticket management |
| **Support** | Ticket system with priority levels and resolution workflow |
| **Settings** | Organization profile, billing email, country/currency, tax registration |

---

## 🏗️ Architecture

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   React UI   │────▶│  Spring Boot │────▶│  PostgreSQL  │
│  (Vite/TW)   │     │   REST API   │     │   Database   │
│  Port 3000   │     │  Port 8080   │     │  Port 5432   │
└──────────────┘     └──────────────┘     └──────────────┘
                           │
                     ┌─────┴─────┐
                     │  Stripe   │
                     │ (Mock/API)│
                     └───────────┘
```

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, TailwindCSS, Chart.js, Lucide Icons |
| Backend | Spring Boot 3.2, Spring Security, JPA/Hibernate, JWT |
| Database | PostgreSQL 15 |
| Payments | Stripe Java SDK (mock mode by default) |
| Docs | Swagger/OpenAPI via SpringDoc |
| Deploy | Docker Compose (local) / Render (cloud) |

---

## 🚀 Quick Start (Docker — 1 command)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running

### Start
```powershell
.\start.ps1
```

This builds everything and opens your browser. That's it.

### Default Admin Login
| Field | Value |
|-------|-------|
| Email | `admin@billing.local` |
| Password | `Admin@123` |

### URLs
| Service | URL |
|---------|-----|
| App | http://localhost:3000 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

### Stop
```powershell
.\stop.ps1
```

---

## ☁️ Deploy to Render (Free)

1. Push to GitHub
2. Go to [Render Dashboard](https://dashboard.render.com) → **New → Blueprint**
3. Connect your repo — Render reads `render.yaml` automatically
4. Set `VITE_API_URL` on the frontend to your backend URL
5. Done! 🎉

---

## 📁 Project Structure

```
saas-billing-platform/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/saas/billing/
│   │   ├── config/             # Security config
│   │   ├── controller/         # REST endpoints
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── entity/             # JPA entities (21 tables)
│   │   ├── repository/         # Spring Data repositories
│   │   ├── security/           # JWT filter, UserDetails
│   │   ├── service/            # Service interfaces
│   │   └── serviceImpl/        # Business logic
│   └── src/main/resources/
│       ├── application.yml     # App configuration
│       └── schema.sql          # DB schema + seed data
├── frontend/                   # React + Vite app
│   └── src/
│       ├── components/         # Navbar, Sidebar
│       ├── context/            # Auth context (JWT)
│       ├── pages/              # All 11 pages
│       └── services/           # API client (axios)
├── docker-compose.yml          # Local Docker setup
├── render.yaml                 # Render cloud deployment
├── .env.example                # Environment template
├── start.ps1                   # Windows start script
├── stop.ps1                    # Windows stop script
├── TESTING_GUIDE.md            # Full testing walkthrough
└── README.md                   # This file
```

---

## 🧪 Testing

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for complete step-by-step instructions covering:
- Authentication flows
- Subscription management
- Invoice generation & download
- Usage tracking
- Admin analytics
- Swagger API testing
- Render deployment verification

---

## ⚙️ Configuration

Copy `.env.example` to `.env` and configure:

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection | `jdbc:postgresql://localhost:5432/saas_billing` |
| `JWT_SECRET` | Token signing key | Pre-set for dev |
| `STRIPE_API_KEY` | Stripe secret key | Mock key (no charges) |
| `SMTP_HOST` | Email server | `smtp.gmail.com` |
| `SMTP_USERNAME` | Email address | Optional |
| `SMTP_PASSWORD` | Email password | Optional |
| `VITE_API_URL` | Backend URL for frontend | Empty (proxy mode) |

---

## 📄 License

MIT
