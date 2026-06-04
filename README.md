# SaaS Billing & Subscription Management Platform

A production-ready, multi-tenant SaaS Billing & Subscription Management System built using **Spring Boot 3, Hibernate, React 18, PostgreSQL, and Stripe Sandbox**.

---

## Architecture Diagram

The system operates under a standard layered MVC-Service architecture:

```
[React SPA Frontend] (Port 3000)
        │ (Axios HTTPS REST requests with JWT)
        ▼
[Nginx Reverse Proxy / Load Balancer] (Port 80 / 443)
        │ (Forwards to API route patterns)
        ▼
[Spring Boot 3 Backend Service] (Port 8080)
  ├── Security Filters (JWT validation, CORS rules, RBAC guards)
  ├── Controllers (Auth, Orgs, Plans, Subscriptions, Payments, Webhooks, Analytics)
  ├── Business Services (Upgrade/Downgrade proration, GST computations, Schedulers)
  └── Repository Layer (Spring Data JPA queries, tenant isolation limits checks)
        │
        ▼
[PostgreSQL Database Instance] (Port 5432)
```

---

## Key Features

1. **Multi-Tenant Isolation**: Row-level database boundaries checking by `organization_id` context mapping extracted from JWT authentication claims.
2. **Stripe Sandbox Integration**: Complete checkout session triggers, recurring cycles renewals, and de-duplicated Webhook event handlers.
3. **Proration & Upgrades Engine**: Direct support for plan migrations, trial expiration schedulers, and payment retry sweeps.
4. **GST Tax Engine**: Dynamic CGST, SGST, and IGST computations matching India and global settings configurations.
5. **Invoicing Engine**: Dynamic PDF receipt files generation using `OpenPDF` and SMTP dispatch queues.
6. **Analytics Engine**: Real-time SaaS MRR, ARR, Churn rates, success distributions, and CSV report export sheets.

---

## Directory Structure

```
saas-billing-platform/
├── backend/
│   ├── src/main/java/com/saas/billing/
│   │   ├── config/            # Spring beans & Security setups
│   │   ├── security/          # JWT filters & CustomUserDetails
│   │   ├── controller/        # REST controllers (Auth, Orgs, Subs)
│   │   ├── service/           # Business interfaces
│   │   ├── serviceImpl/       # Implementations & Stripe handlers
│   │   ├── repository/        # Data JPA layers
│   │   ├── entity/            # PostgreSQL mapping entities
│   │   ├── dto/               # Serialized request/response models
│   │   ├── mapper/            # Mapping utilities
│   │   ├── exception/         # Error handlers
│   │   └── scheduler/         # Cron automated renovators
│   ├── src/main/resources/    # application.yml configuration & schema.sql
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── context/           # AuthContext
│   │   ├── components/        # Navbar & Sidebar layouts
│   │   ├── pages/             # Dashboard, Plans, Support, Admin
│   │   ├── services/          # Axios HTTP clients
│   │   ├── App.jsx            # Router trees
│   │   └── index.css          # Design system & dark themes
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
└── docker-compose.yml
```

---

## Local Development Installation

### Prerequisites
- Java Development Kit (JDK) 17
- Node.js v18 or v20
- PostgreSQL database server running on `localhost:5432`

### Database Setup
1. Create a database named `saas_billing` in PostgreSQL:
   ```sql
   CREATE DATABASE saas_billing;
   ```
2. The Spring Boot backend will automatically execute `schema.sql` on startup to initialize the tables and seed default roles and plans.

### Running Backend Locally
1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```
2. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```
3. The API server will start on `http://localhost:8080`. Swagger documentation is available at `http://localhost:8080/swagger-ui.html`.

### Running Frontend Locally
1. Navigate to the `frontend/` directory:
   ```bash
   cd frontend
   ```
2. Install npm dependencies:
   ```bash
   npm install
   ```
3. Boot the development Vite server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to `http://localhost:3000`.

---

## Docker Deployment (Single-Command Run)

To run the database, backend services, and frontend dashboards in unified containers:

1. Execute docker-compose at the root folder:
   ```bash
   docker compose up --build
   ```
2. The application will be accessible at:
   - Frontend Dashboard: `http://localhost:3000`
   - Backend APIs: `http://localhost:8080`
   - PostgreSQL DB Port: `5432`

---

## AWS Deployment Guide

### 1. Database (RDS PostgreSQL)
- Provision an RDS instance running PostgreSQL 15+.
- Update `DATABASE_URL` environment variables inside the backend service container to point to the RDS endpoint.

### 2. File Storage (Amazon S3)
- Save generated invoice PDFs in an S3 Bucket.
- Use IAM Roles on the EC2 host to authenticate bucket file uploads.

### 3. Server (EC2 & Nginx)
- Launch an EC2 t3.medium instance running Ubuntu Linux.
- Copy Nginx reverse proxy configurations from `docker/nginx-prod.conf` to `/etc/nginx/sites-available/default`.
- Set up Let's Encrypt / Certbot to manage automatic SSL certificate renewals on port 443.
