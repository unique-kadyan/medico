# Medico - Integrated Hospital & Pharmacy Management System

A comprehensive, full-stack hospital management platform built with **Spring Boot** and **React**, designed for managing hospital operations and pharmacy services seamlessly.

## 🏥 Overview

Medico is a full-featured Hospital Management System with integrated Pharmacy Management capabilities, designed for small to mid-sized hospitals (50-300 beds). It provides a unified platform for managing patients, doctors, appointments, medical records, pharmacy inventory, and payment processing.

## ✨ Key Features

### Hospital Management
- **Patient Management**
  - Complete patient registration and demographics
  - Medical history tracking
  - Blood group and allergy management
  - Emergency contact information

- **Doctor Management**
  - Doctor profiles with specializations
  - License tracking
  - Department assignment
  - Availability management

- **Staff Management**
  - Nurse, pharmacist, lab technician, and receptionist profiles
  - Staff registration approval workflow
  - Doctor/Nurse patient assignment tracking

- **Appointment System**
  - Schedule patient appointments
  - Doctor availability checking
  - Appointment status tracking
  - Time slot management

- **Emergency Room Management**
  - Real-time emergency room status tracking
  - Multi-floor emergency department support
  - Equipment and capacity management
  - Room occupancy monitoring
  - Specialized rooms (Trauma, Pediatric, Cardiac, Isolation, etc.)
  - Room cleaning and maintenance status
  - Emergency patient admission and triage

- **Medical Records**
  - Electronic Medical Records (EMR)
  - Vital signs tracking
  - Diagnosis and treatment history
  - Lab and imaging results

- **Operation Theater Management**
  - OT request creation and scheduling
  - Surgery request tracking

### Pharmacy Management
- **Medication Inventory**
  - Comprehensive medication database
  - Stock level monitoring
  - Automatic reorder alerts
  - Expiry date tracking
  - Batch number management

- **Smart Inventory Features**
  - Low stock alerts
  - Expired medication tracking
  - Medications expiring soon (3-month window)
  - Category-based organization

- **Prescription Management**
  - Electronic prescriptions
  - Prescription dispensing
  - Drug interaction checking capability
  - Dosage and frequency tracking

### Medicine Orders & Payments
- **E-Pharmacy Orders**
  - Medicine order creation and fulfillment tracking
  - Order status management
  - Checkout workflow

- **Payment Gateway Integration**
  - Razorpay integration (UPI, Cards, Net Banking)
  - Stripe integration (Cards, International payments)
  - Payment status tracking and verification
  - Signature verification for secure transactions

### Analytics & Reporting
- **Dashboard** with real-time statistics
- **Reports** module for analytics and insights

## 🛠️ Technology Stack

### Backend
| Technology | Details |
|---|---|
| **Framework** | Spring Boot 3.5.7 |
| **Language** | Java 25 |
| **Database** | H2 (Development), PostgreSQL (Production-ready) |
| **Security** | Spring Security with JWT authentication |
| **API Docs** | Swagger/OpenAPI 3.0 |
| **ORM** | Spring Data JPA with Hibernate |
| **Caching** | Spring Cache |
| **Build Tool** | Maven 3.9+ |

#### Backend Dependencies
- Spring Boot Starter Web, Data JPA, Security, Validation, Cache
- Lombok (reducing boilerplate code)
- ModelMapper (DTO conversions)
- SpringDoc OpenAPI (API documentation)
- JWT for token-based authentication

### Frontend
| Technology | Details |
|---|---|
| **Framework** | React 18.3.1 |
| **Build Tool** | Vite 6.0.5 |
| **Routing** | React Router DOM 7.1.1 |
| **State Management** | Redux Toolkit 2.4.0 + React Query 5.62.7 |
| **UI Library** | Material-UI (MUI) 6.2.0 |
| **CSS** | Tailwind CSS 3.4.17 + Emotion CSS-in-JS |
| **Forms** | React Hook Form 7.54.0 + Yup validation |
| **HTTP Client** | Axios 1.7.9 |
| **Charts** | Recharts 2.15.0 |
| **Payments** | Stripe (react-stripe-js) + Razorpay |
| **Date Utils** | date-fns 3.6.0, Day.js 1.11.13 |
| **Notifications** | Sonner 1.7.1 (toast notifications) |
| **Linting** | ESLint 9.17.0 + Prettier 3.6.2 |

## 📁 Project Structure

```
medico/
├── src/                          # Spring Boot backend
│   └── main/
│       ├── java/                 # Java source code
│       │   └── com/medico/
│       │       ├── controller/   # REST API controllers
│       │       ├── service/      # Business logic
│       │       ├── repository/   # Data access layer
│       │       ├── model/        # JPA entities
│       │       ├── dto/          # Data transfer objects
│       │       ├── config/       # Security, CORS, cache config
│       │       └── exception/    # Custom exceptions
│       └── resources/
│           ├── application.properties
│           └── static/           # Frontend build output
├── frontend/                     # React frontend
│   ├── src/
│   │   ├── App.jsx              # Root component with routing
│   │   ├── main.jsx             # Entry point (Redux, Theme, QueryClient)
│   │   ├── components/
│   │   │   ├── auth/            # PermissionGuard component
│   │   │   ├── layout/          # Layout, Header, Sidebar, MobileNav
│   │   │   └── payments/        # PaymentDialog, Razorpay, Stripe
│   │   ├── pages/               # Feature-based pages
│   │   │   ├── auth/            # Login, Register, Hospital Registration
│   │   │   ├── dashboard/       # Main dashboard
│   │   │   ├── patients/        # Patient CRUD
│   │   │   ├── doctors/         # Doctor management
│   │   │   ├── nurses/          # Nurse management
│   │   │   ├── pharmacists/     # Pharmacist management
│   │   │   ├── lab-technicians/ # Lab technician management
│   │   │   ├── receptionists/   # Receptionist management
│   │   │   ├── appointments/    # Appointment scheduling
│   │   │   ├── medications/     # Medication inventory
│   │   │   ├── medicationRequests/ # Medication request workflow
│   │   │   ├── medicineOrders/  # Medicine orders & checkout
│   │   │   ├── labTests/        # Lab test requests
│   │   │   ├── otRequests/      # Operation theater requests
│   │   │   ├── emergency/       # Emergency room management
│   │   │   ├── assignments/     # Doctor/Nurse assignments
│   │   │   ├── approvals/       # Staff registration approvals
│   │   │   └── reports/         # Analytics & reporting
│   │   ├── services/            # API integration layer (22 services)
│   │   │   ├── api.js           # Axios instance with interceptors
│   │   │   ├── authService.js
│   │   │   ├── patientService.js
│   │   │   ├── doctorService.js
│   │   │   ├── medicineOrderService.js
│   │   │   ├── razorpayService.js
│   │   │   ├── stripeService.js
│   │   │   └── ...              # 15+ more domain services
│   │   ├── store/
│   │   │   ├── store.js         # Redux store configuration
│   │   │   └── slices/
│   │   │       ├── authSlice.js # Authentication state
│   │   │       └── uiSlice.js   # UI state
│   │   ├── hooks/
│   │   │   └── usePermissions.js # Permission checking hook
│   │   ├── utils/
│   │   │   └── permissions.js   # Role & permission definitions
│   │   └── styles/
│   │       └── index.css        # Global styles
│   ├── package.json
│   └── vite.config.js
└── pom.xml                       # Maven build configuration
```

## 🚀 Getting Started

### Prerequisites
- Java 25 or higher
- Maven 3.9+
- Node.js 18+ and npm
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd medico
   ```

2. **Setup the frontend**
   ```bash
   cd frontend
   npm install
   ```

3. **Run in development mode** (two terminals):

   **Terminal 1 - Backend:**
   ```bash
   mvn spring-boot:run
   ```

   **Terminal 2 - Frontend:**
   ```bash
   cd frontend
   npm run dev
   ```

4. **Build for production**
   ```bash
   # Build frontend (outputs to src/main/resources/static)
   cd frontend
   npm run build

   # Build the full application
   cd ..
   mvn clean install

   # Run the JAR (serves both backend API and frontend)
   java -jar target/medico-1.0-SNAPSHOT.jar
   ```

5. **Access the application**
   - Frontend (dev): http://localhost:5173
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:medicodb`
     - Username: `sa`
     - Password: (leave empty)

### Frontend Scripts
```bash
npm run dev          # Start development server
npm run build        # Production build
npm run preview      # Preview production build
npm run lint         # Run ESLint
npm run lint:fix     # Auto-fix lint issues
npm run format       # Format code with Prettier
npm run format:check # Check code formatting
```

## 🎨 Frontend Architecture

### State Management
- **Redux Toolkit** - Global state for authentication and UI
- **React Query** - Server-side state with 5-minute cache, auto-retry, and background refetching
- **Local State** - Component-level state for forms, dialogs, and UI interactions

### Authentication Flow
1. User logs in via `/login` page
2. Backend returns JWT token + user data
3. Token stored in `localStorage` and added to all API requests via Axios interceptors
4. Protected routes check auth state from Redux store
5. 401 responses trigger automatic logout and redirect

### Role-Based Access Control (Frontend)
The frontend enforces RBAC using:
- **PermissionGuard** component - Conditionally renders UI based on roles/permissions
- **usePermissions** hook - Programmatic permission checks in components
- **Route protection** - `ProtectedRoute` wrapper redirects unauthenticated users

**10 Roles**: ADMIN, DOCTOR, DOCTOR_SUPERVISOR, NURSE, NURSE_MANAGER, NURSE_SUPERVISOR, LAB_TECHNICIAN, PHARMACIST, RECEPTIONIST, PATIENT

**60+ Permissions** covering all CRUD operations across modules

### API Integration
- Centralized Axios instance with base URL `/api`
- Request interceptors for JWT token and API key injection
- Response interceptors for error handling (401, 403, 404, 500)
- Toast notifications via Sonner for user feedback
- 22 dedicated service modules for clean separation of concerns

### UI/UX
- **Material-UI** with custom theme (Blue primary, Pink secondary)
- **Responsive design** with collapsible sidebar and mobile bottom navigation
- **Tailwind CSS** for utility-based styling alongside MUI components
- **MUI Date Pickers** for date/time selection
- **Recharts** for dashboard data visualizations

## 📚 API Documentation

Once the application is running, visit the Swagger UI for interactive API documentation:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Main API Endpoints

#### Patient Management
- `GET /api/patients` - Get all patients
- `GET /api/patients/{id}` - Get patient by ID
- `POST /api/patients` - Create new patient
- `PUT /api/patients/{id}` - Update patient
- `DELETE /api/patients/{id}` - Deactivate patient
- `GET /api/patients/search?name={name}` - Search patients by name

#### Medication Management
- `GET /api/medications` - Get all medications
- `GET /api/medications/{id}` - Get medication by ID
- `POST /api/medications` - Add new medication
- `PUT /api/medications/{id}` - Update medication
- `PATCH /api/medications/{id}/stock?quantity={qty}` - Update stock
- `GET /api/medications/low-stock` - Get low stock medications
- `GET /api/medications/expired` - Get expired medications
- `GET /api/medications/expiring-soon` - Get medications expiring within 3 months

#### Emergency Room Management
- `GET /api/emergency-rooms` - Get all emergency rooms
- `GET /api/emergency-rooms/{id}` - Get emergency room by ID
- `GET /api/emergency-rooms/available` - Get available emergency rooms
- `GET /api/emergency-rooms/status/{status}` - Get rooms by status
- `POST /api/emergency-rooms` - Create new emergency room
- `PUT /api/emergency-rooms/{id}` - Update emergency room
- `PATCH /api/emergency-rooms/{id}/status` - Update room status

#### Emergency Patient Management
- `GET /api/emergency-patients` - Get all emergency patients
- `GET /api/emergency-patients/{id}` - Get emergency patient by ID
- `POST /api/emergency-patients` - Admit emergency patient
- `PUT /api/emergency-patients/{id}` - Update emergency patient
- `PATCH /api/emergency-patients/{id}/discharge` - Discharge patient

## 🔐 Security

The application uses Spring Security with JWT-based authentication and role-based access control:

### User Roles
- **ADMIN**: Full system access
- **DOCTOR / DOCTOR_SUPERVISOR**: Access to patients, appointments, prescriptions
- **NURSE / NURSE_MANAGER / NURSE_SUPERVISOR**: Access to patients, appointments, medical records
- **PHARMACIST**: Access to medications, prescriptions
- **RECEPTIONIST**: Access to patients, appointments, doctors
- **LAB_TECHNICIAN**: Access to lab orders and results
- **PATIENT**: Limited access to own records

### Security Configuration
For development, the following endpoints are publicly accessible:
- `/api-docs/**` - API documentation
- `/swagger-ui/**` - Swagger UI
- `/h2-console/**` - H2 database console
- `/api/auth/**` - Authentication endpoints

## 📦 Database Schema

### Core Entities
1. **Patient**: Patient demographic and medical information
2. **Doctor**: Doctor profiles and credentials
3. **User**: System users with role-based access
4. **Appointment**: Scheduled appointments
5. **MedicalRecord**: Patient medical history
6. **Medication**: Pharmacy inventory
7. **Prescription**: Patient prescriptions
8. **PrescriptionItem**: Individual medication items in prescriptions
9. **EmergencyRoom**: Emergency department room management
10. **EmergencyPatient**: Emergency patient admissions and triage
11. **NursePatientAssignment**: Nurse-to-patient assignments
12. **OTRequest**: Operating theater/surgery requests

### Key Features
- Automatic timestamps (created_at, updated_at)
- Soft delete functionality (active flag)
- Relationship mapping between entities
- Audit trail support

## 🎯 Use Cases

### For Small to Medium Hospitals
- **Patient Registration**: Quick patient onboarding with complete demographic capture
- **Appointment Scheduling**: Efficient doctor-patient appointment management
- **Medical Records**: Centralized EMR system
- **Inventory Management**: Real-time medication stock tracking
- **Prescription Processing**: Seamless prescription workflow from doctor to pharmacy
- **Online Payments**: Integrated payment processing for medicine orders

### Competitive Advantages
- **Full-Stack Solution**: Modern React frontend + robust Spring Boot backend
- **Integrated Solution**: Single system for hospital and pharmacy (no separate systems)
- **Cost-Effective**: Suitable for 50-300 bed hospitals
- **Modern Technology**: Built with Spring Boot, React 18, and Material-UI
- **Scalable Architecture**: Microservice-ready design
- **Comprehensive API**: RESTful APIs for integration with other systems
- **Payment Ready**: Razorpay and Stripe integration out of the box

## 💰 Monetization Strategy

### Licensing Models
1. **Perpetual License**: $50,000 - $250,000 (based on hospital size)
2. **Subscription Model**: $3,000 - $15,000/month
3. **Module-Based Pricing**: Pay for what you need

### Revenue Streams
- Software licensing
- Implementation services ($20,000 - $100,000)
- Annual maintenance (18-22% of license fee)
- Training services ($5,000 - $20,000)
- Custom development ($150 - $200/hour)

### Value-Added Services
- Data migration from legacy systems
- Interface development for medical equipment
- Regulatory compliance packages
- Business intelligence and analytics solutions

## 🔄 Future Enhancements

### Phase 2 (Completed/In Progress)
- [x] Emergency room management system
- [x] Emergency patient admission and triage
- [x] Nurse-patient assignment tracking
- [x] Operating theater (OT) request management
- [x] Dashboard with real-time statistics
- [x] React frontend with Material-UI
- [x] JWT-based authentication
- [x] Role-based access control (frontend + backend)
- [x] Payment gateway integration (Razorpay + Stripe)
- [x] Medicine order management
- [x] Staff registration approval workflow
- [x] Analytics & reporting module
- [ ] Real-time notifications
- [ ] Billing and insurance management
- [ ] Lab management module enhancement
- [ ] Ward/Bed management

### Phase 3 (Advanced)
- [ ] HL7/FHIR compliance for healthcare interoperability
- [ ] Telemedicine integration
- [ ] Mobile app for patients and doctors
- [ ] AI-powered diagnosis assistance
- [ ] Drug interaction checking with external APIs
- [ ] Inventory predictive analytics

## 🧪 Testing

### Backend Tests
```bash
mvn test
```

Run with coverage:
```bash
mvn clean test jacoco:report
```

### Frontend Linting
```bash
cd frontend
npm run lint
npm run format:check
```

## 📝 Configuration

### Backend Configuration
Edit `src/main/resources/application.properties`:

```properties
# For PostgreSQL Production
spring.datasource.url=jdbc:postgresql://localhost:5432/medicodb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### JWT Configuration
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Frontend Environment
The frontend uses Vite's proxy to forward `/api` requests to the backend at `http://localhost:8080` during development. For production, the frontend build is served as static files from the Spring Boot application.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

Proprietary - All rights reserved. Contact for licensing information.

## 📧 Contact

For inquiries about licensing, implementation, or support:
- Email: support@medico.com
- Website: https://www.medico.com

## 🙏 Acknowledgments

Built with:
- Spring Boot Framework
- Spring Security
- Hibernate ORM
- Swagger/OpenAPI
- Lombok
- React 18
- Material-UI (MUI)
- Redux Toolkit
- TanStack React Query
- Vite
- Tailwind CSS
- Razorpay & Stripe

---

**Version**: 1.0.0
**Last Updated**: February 2026
**Maintained By**: Kaddy Development Team
