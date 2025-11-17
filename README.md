# Medico - Integrated Hospital & Pharmacy Management System

A comprehensive, enterprise-grade Spring Boot application for managing hospital operations and pharmacy services seamlessly.

## 🏥 Overview

Medico is a full-featured Hospital Management System with integrated Pharmacy Management capabilities, designed for small to mid-sized hospitals (50-300 beds). It provides a unified platform for managing patients, doctors, appointments, medical records, and pharmacy inventory.

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

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.1
- **Java Version**: 17 (LTS)
- **Database**: H2 (Development), PostgreSQL (Production-ready)
- **Security**: Spring Security with role-based access control
- **API Documentation**: Swagger/OpenAPI 3.0
- **ORM**: Spring Data JPA with Hibernate
- **Caching**: Spring Cache
- **Build Tool**: Maven 3.9+

### Key Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Spring Boot Starter Cache
- Lombok (reducing boilerplate code)
- ModelMapper (DTO conversions)
- SpringDoc OpenAPI (API documentation)
- JWT for authentication (ready for implementation)

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.9+
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd medico
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR file:
   ```bash
   java -jar target/medico-1.0-SNAPSHOT.jar
   ```

4. **Access the application**
   - Application: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:medicodb`
     - Username: `sa`
     - Password: (leave empty)

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

The application uses Spring Security with role-based access control:

### User Roles
- **ADMIN**: Full system access
- **DOCTOR**: Access to patients, appointments, prescriptions
- **NURSE**: Access to patients, appointments, medical records
- **PHARMACIST**: Access to medications, prescriptions
- **RECEPTIONIST**: Access to patients, appointments, doctors
- **LAB_TECHNICIAN**: Access to lab orders and results
- **PATIENT**: Limited access to own records

### Current Security Configuration
For development, the following endpoints are publicly accessible:
- `/api-docs/**` - API documentation
- `/swagger-ui/**` - Swagger UI
- `/h2-console/**` - H2 database console
- `/api/auth/**` - Authentication endpoints (to be implemented)

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

### Competitive Advantages
- **Integrated Solution**: Single system for hospital and pharmacy (no separate systems)
- **Cost-Effective**: Suitable for 50-300 bed hospitals
- **Modern Technology**: Built with latest Spring Boot and Java 17
- **Scalable Architecture**: Microservice-ready design
- **Comprehensive API**: RESTful APIs for integration with other systems

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
- [ ] JWT-based authentication implementation
- [ ] Doctor appointment scheduling UI
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

Run tests with:
```bash
mvn test
```

Run with coverage:
```bash
mvn clean test jacoco:report
```

## 📝 Configuration

### Database Configuration
Edit `src/main/resources/application.properties`:

```properties
# For PostgreSQL Production
spring.datasource.url=jdbc:postgresql://localhost:5432/medicodb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Security Configuration
JWT secret and expiration can be configured in `application.properties`:
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
```

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

---

**Version**: 1.0.0
**Last Updated**: November 2025
**Maintained By**: Kaddy Development Team
