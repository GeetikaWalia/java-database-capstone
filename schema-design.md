
# Smart Clinic Management System — Schema Design

This document outlines the proposed **MySQL Database Design** for structured, relational data and the **MongoDB Collection Design** for flexible, document-oriented data.

---

## MySQL Database Design

> **Why MySQL?** Core operational entities (patients, doctors, appointments, payments) benefit from ACID guarantees, foreign keys, and well-defined relationships.

### Conventions
- Primary keys: `INT AUTO_INCREMENT` unless stated otherwise.
- Timestamps: `DATETIME` (UTC). Handle timezone in the application.
- Soft delete: prefer `is_active` flags over hard deletes for auditability.
- Indices: add on foreign keys and frequent search columns.

### Table: `patients`
- `id`: INT, **PK**, AUTO_INCREMENT
- `full_name`: VARCHAR(100), NOT NULL
- `email`: VARCHAR(255), NULL, UNIQUE
- `phone`: VARCHAR(20), NOT NULL
- `dob`: DATE, NULL
- `gender`: ENUM('Male','Female','Other'), NULL
- `address_line1`: VARCHAR(255), NULL
- `address_line2`: VARCHAR(255), NULL
- `city`: VARCHAR(100), NULL
- `state`: VARCHAR(100), NULL
- `postal_code`: VARCHAR(20), NULL
- `is_active`: TINYINT(1), NOT NULL DEFAULT 1
- `created_at`: DATETIME, NOT NULL
- `updated_at`: DATETIME, NOT NULL

**Indexes**: `(phone)`, `(email)`

---

### Table: `doctors`
- `id`: INT, **PK**, AUTO_INCREMENT
- `full_name`: VARCHAR(100), NOT NULL
- `email`: VARCHAR(255), NOT NULL, UNIQUE
- `phone`: VARCHAR(20), NOT NULL
- `specialty`: VARCHAR(100), NOT NULL
- `license_number`: VARCHAR(50), NOT NULL, UNIQUE
- `bio`: TEXT, NULL
- `is_active`: TINYINT(1), NOT NULL DEFAULT 1
- `created_at`: DATETIME, NOT NULL
- `updated_at`: DATETIME, NOT NULL

**Indexes**: `(specialty)`, `(license_number)`

---

### Table: `clinic_locations`
- `id`: INT, **PK**, AUTO_INCREMENT
- `name`: VARCHAR(100), NOT NULL
- `address_line1`: VARCHAR(255), NOT NULL
- `address_line2`: VARCHAR(255), NULL
- `city`: VARCHAR(100), NOT NULL
- `state`: VARCHAR(100), NOT NULL
- `postal_code`: VARCHAR(20), NOT NULL
- `phone`: VARCHAR(20), NULL
- `is_active`: TINYINT(1), NOT NULL DEFAULT 1

---

### Table: `doctor_availability`
- `id`: INT, **PK**, AUTO_INCREMENT
- `doctor_id`: INT, **FK** → `doctors(id)`, NOT NULL
- `location_id`: INT, **FK** → `clinic_locations(id)`, NULL
- `weekday`: ENUM('Mon','Tue','Wed','Thu','Fri','Sat','Sun'), NOT NULL
- `start_time`: TIME, NOT NULL
- `end_time`: TIME, NOT NULL
- `effective_from`: DATE, NOT NULL
- `effective_to`: DATE, NULL
- `is_active`: TINYINT(1), NOT NULL DEFAULT 1

**Indexes**: `(doctor_id, weekday)`, `(location_id)`

**Note**: Enforce no-overlap via application logic before insert/update.

---

### Table: `appointments`
- `id`: INT, **PK**, AUTO_INCREMENT
- `doctor_id`: INT, **FK** → `doctors(id)`, NOT NULL
- `patient_id`: INT, **FK** → `patients(id)`, NOT NULL
- `location_id`: INT, **FK** → `clinic_locations(id)`, NULL
- `appointment_time`: DATETIME, NOT NULL
- `duration_minutes`: INT, NOT NULL DEFAULT 30
- `status`: ENUM('Scheduled','Completed','Cancelled','NoShow'), NOT NULL DEFAULT 'Scheduled'
- `notes`: TEXT, NULL
- `created_at`: DATETIME, NOT NULL
- `updated_at`: DATETIME, NOT NULL

**Indexes**: `(doctor_id, appointment_time)`, `(patient_id, appointment_time)`

**Delete policy**: do not cascade delete when patient/doctor is removed; use `is_active` flags to preserve history.

---

### Table: `payments`
- `id`: INT, **PK**, AUTO_INCREMENT
- `appointment_id`: INT, **FK** → `appointments(id)`, NOT NULL
- `amount_cents`: INT, NOT NULL
- `currency`: CHAR(3), NOT NULL DEFAULT 'INR'
- `status`: ENUM('Pending','Paid','Refunded','Failed'), NOT NULL
- `method`: ENUM('Cash','Card','UPI','NetBanking'), NOT NULL
- `transaction_ref`: VARCHAR(100), NULL, UNIQUE
- `created_at`: DATETIME, NOT NULL
- `updated_at`: DATETIME, NOT NULL

**Indexes**: `(appointment_id)`, `(status)`

---

## MongoDB Collection Design

> **Why MongoDB?** Semi-structured and evolving records (prescriptions, notes, messages, audit logs) fit better as documents with nested objects and arrays.

### Collection: `prescriptions`
Example document:
```json
{
  "_id": { "$oid": "64abc1234567890abcdef123" },
  "appointmentId": 51,
  "patientId": 1001,
  "doctorId": 201,
  "medications": [
    { "name": "Paracetamol", "dosage": "500mg", "frequency": "6-hourly", "durationDays": 5 },
    { "name": "Omeprazole", "dosage": "20mg", "frequency": "OD", "durationDays": 10 }
  ],
  "doctorNotes": "Hydrate well; avoid NSAIDs if gastric discomfort continues.",
  "refillCount": 2,
  "pharmacy": { "name": "Apollo Pharmacy", "location": "DLF Phase 3" },
  "tags": ["fever", "gastric"],
  "createdAt": "2025-12-15T10:00:00Z"
}
```

### Collection: `doctor_notes`
Example document:
```json
{
  "_id": { "$oid": "64abc9876543210fedcba321" },
  "appointmentId": 51,
  "doctorId": 201,
  "patientId": 1001,
  "notes": [
    { "at": "2025-12-15T10:05:00Z", "text": "Patient reports headache; BP normal." },
    { "at": "2025-12-15T10:20:00Z", "text": "Prescribed Paracetamol; advised rest." }
  ],
  "attachments": [ { "type": "image", "url": "https://cdn.example.com/xray123.jpg" } ],
  "visibility": "internal",
  "createdAt": "2025-12-15T10:00:00Z"
}
```

### Collection: `messages`
Example document (patient–doctor chat):
```json
{
  "_id": { "$oid": "64abc5555aaee11bb22cc33d" },
  "channel": { "type": "appointment", "appointmentId": 51 },
  "participants": { "doctorId": 201, "patientId": 1001 },
  "messages": [
    { "at": "2025-12-15T09:45:00Z", "from": "patient", "text": "Running late by 10 mins." },
    { "at": "2025-12-15T09:46:00Z", "from": "doctor", "text": "Acknowledged. See you." }
  ],
  "status": "open",
  "createdAt": "2025-12-15T09:40:00Z"
}
```

### Collection: `audit_logs`
Example document:
```json
{
  "_id": { "$oid": "64abclog0001112223334445" },
  "actor": { "type": "admin", "adminId": 5 },
  "action": "UPDATE_DOCTOR_AVAILABILITY",
  "context": { "doctorId": 201, "availabilityId": 3005 },
  "ip": "203.0.113.7",
  "userAgent": "Mozilla/5.0",
  "at": "2025-12-15T06:20:00Z",
  "metadata": { "old": { "weekday": "Mon", "start": "10:00" }, "new": { "weekday": "Mon", "start": "11:00" } }
}
```

---

## Notes & Rationale
- **Relational vs Document**: Operational entities with relationships (appointments ↔ doctors/patients) sit in MySQL; flexible, additive content (prescriptions, notes, messages, logs) live in MongoDB.
- **Cross‑store links**: Keep numeric foreign keys in documents (e.g., `appointmentId`) and join at the application layer.
- **Data retention**: Avoid hard deletes for medical context; prefer `is_active` and audit trails.

