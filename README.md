# 💳 SaaS Billing & Subscription Management Platform

A full-stack SaaS Billing & Subscription Management System built with **Spring Boot**, **React**, and **PostgreSQL**. The platform enables organizations to manage subscription plans, billing, invoices, payments, and usage tracking through a secure REST API.

---

# ✨ Features

## Authentication & Authorization
- JWT Authentication
- Role-Based Access Control (Admin, Organization, User)
- User Registration & Login
- Password Reset

## Subscription Management
- Create and manage subscription plans
- Subscribe to plans
- Upgrade/Downgrade subscriptions
- Pause & Resume subscriptions
- Cancel subscriptions
- Subscription history

## Billing & Invoicing
- Automatic invoice generation
- PDF invoice download
- Email invoice notifications
- GST/Tax calculation

## Payment Management
- Stripe integration (Test/Mock Mode)
- Payment history
- Refund support
- Payment retry handling

## Usage Tracking
- API usage monitoring
- Storage usage tracking
- User limit enforcement

## Organization Management
- Organization profile
- Billing settings
- Currency & Country configuration
- GST registration

## Admin Dashboard
- Monthly Revenue Analytics
- Annual Revenue Analytics
- Plan Distribution
- User Management
- Support Ticket Management

## API Documentation
- Swagger/OpenAPI Integration

---

# 🏗️ Tech Stack

| Layer | Technology |
|--------|------------|
| Frontend | React 18, Vite, Tailwind CSS |
| Backend | Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | PostgreSQL |
| Authentication | JWT |
| Documentation | Swagger (SpringDoc) |
| Payment Gateway | Stripe Java SDK |
| PDF Generation | OpenPDF |
| Email | Spring Mail |

---

# 🏛️ Architecture

```
React Frontend
      │
      ▼
Spring Boot REST API
      │
      ▼
 PostgreSQL Database
      │
      ▼
Stripe (Test Mode)
```

---

# 📁 Project Structure

```
billing-platform
│
├── backend
│   ├── config
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── exception
│   ├── mapper
│   ├── repository
│   ├── security
│   ├── service
│   ├── serviceImpl
│   └── resources
│
├── frontend
│
└── README.md
```

---

# 🚀 Getting Started

## Prerequisites

- Java 17
- Maven
- PostgreSQL
- Node.js (Frontend)

---

## Backend Setup

Clone the repository.

```bash
git clone <repository-url>
```

Navigate to backend.

```bash
cd backend
```

Create PostgreSQL database.

```
saas_billing
```

Update `application.yml` or environment variables.

Run the application.

```bash
mvn spring-boot:run
```

Backend runs at:

```
http://localhost:8080
```

---

## Frontend Setup

Navigate to frontend.

```bash
cd frontend
```

Install dependencies.

```bash
npm install
```

Run the frontend.

```bash
npm run dev
```

Frontend runs at:

```
http://localhost:5173
```

---

# 📚 API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui.html
```

OpenAPI

```
http://localhost:8080/api-docs
```

---

# 🔧 Configuration

Configure the following environment variables.

| Variable | Description |
|----------|-------------|
| DATABASE_URL | PostgreSQL URL |
| DATABASE_USERNAME | Database Username |
| DATABASE_PASSWORD | Database Password |
| JWT_SECRET | JWT Secret |
| STRIPE_API_KEY | Stripe API Key |
| STRIPE_WEBHOOK_SECRET | Stripe Webhook Secret |
| SMTP_HOST | Mail Host |
| SMTP_USERNAME | Mail Username |
| SMTP_PASSWORD | Mail Password |

---

# 🧪 Testing

The backend can be tested using:

- Swagger UI
- Postman
- REST Client
- JUnit Tests

---

# 🔐 Security

- JWT Authentication
- BCrypt Password Encryption
- Role-Based Authorization
- Global Exception Handling
- Input Validation

---

# 📈 Future Improvements

- Docker Deployment
- Flyway Database Migration
- Refresh Token Rotation
- Email Verification
- CI/CD Pipeline
- Automated Integration Tests

---

# 👨‍💻 Author

Developed as an Internship Project demonstrating enterprise-level backend development using Spring Boot.

---

# 📄 License

This project is licensed under the MIT License.