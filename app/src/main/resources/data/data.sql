
-- Smart Clinic Reporting Procedures and Sample Data
-- File: smart_clinic_reports.sql
-- Database: smart_clinic

-- 1) Schema (DDL) -------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS smart_clinic;
USE smart_clinic;

-- Doctors table
CREATE TABLE IF NOT EXISTS doctors (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  specialty VARCHAR(100) NOT NULL
);

-- Patients table
CREATE TABLE IF NOT EXISTS patients (
  id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE,
  phone VARCHAR(20)
);

-- Appointments table
CREATE TABLE IF NOT EXISTS appointments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  doctor_id INT NOT NULL,
  patient_id INT NOT NULL,
  appointment_time DATETIME NOT NULL,
  status ENUM('Scheduled','Completed','Cancelled','NoShow') NOT NULL DEFAULT 'Scheduled',
  CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
  CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
  INDEX idx_appt_time (appointment_time),
  INDEX idx_appt_doctor_time (doctor_id, appointment_time),
  INDEX idx_appt_patient_time (patient_id, appointment_time)
);

-- 2) Sample Data --------------------------------------------------------------
USE smart_clinic;

-- Clear previous sample data (optional for clean reruns)
DELETE FROM appointments;
DELETE FROM doctors;
DELETE FROM patients;

-- Doctors
INSERT INTO doctors (full_name, specialty) VALUES
('Dr. Asha Mehra', 'General Physician'),
('Dr. Rohan Kapoor', 'Cardiology'),
('Dr. Neha Singh', 'Dermatology');

-- Patients
INSERT INTO patients (full_name, email, phone) VALUES
('Anita Sharma', 'anita@example.com', '9876543210'),
('Vikram Gupta', 'vikram@example.com', '9123456780'),
('Rahul Verma', 'rahul@example.com', '9988776655'),
('Meera Nair',  'meera@example.com',  '9090909090');

-- Appointments
-- Adjust dates as needed for your tests
INSERT INTO appointments (doctor_id, patient_id, appointment_time, status) VALUES
-- Day 1 (e.g., 2025-12-14)
(1, 1, '2025-12-14 09:00:00', 'Completed'),
(1, 2, '2025-12-14 10:00:00', 'Completed'),
(2, 3, '2025-12-14 11:00:00', 'Completed'),
(2, 4, '2025-12-14 12:00:00', 'Completed'),

-- Day 2 (e.g., 2025-12-15)
(1, 3, '2025-12-15 09:30:00', 'Scheduled'),
(1, 4, '2025-12-15 10:15:00', 'Scheduled'),
(2, 1, '2025-12-15 11:00:00', 'Scheduled'),
(3, 2, '2025-12-15 11:30:00', 'Scheduled'),

-- Same month different days
(3, 1, '2025-12-01 09:00:00', 'Completed'),
(3, 3, '2025-12-02 10:00:00', 'Completed'),

-- Previous month
(2, 2, '2025-11-20 09:00:00', 'Completed'),
(2, 3, '2025-11-21 10:00:00', 'Completed'),

-- Previous year
(1, 1, '2024-12-10 09:00:00', 'Completed'),
(1, 2, '2024-12-11 10:00:00', 'Completed');

-- 3) Stored Procedures --------------------------------------------------------
DELIMITER $$

-- A) Daily Appointments Report, grouped by doctor
DROP PROCEDURE IF EXISTS GetDailyAppointmentReportByDoctor $$
CREATE PROCEDURE GetDailyAppointmentReportByDoctor(IN p_date DATE)
BEGIN
  /* Report: Appointments on p_date, grouped by doctor. */
  SELECT
      d.id AS doctor_id,
      d.full_name AS doctor_name,
      COUNT(*) AS appointments_count
  FROM appointments a
  JOIN doctors d ON d.id = a.doctor_id
  WHERE DATE(a.appointment_time) = p_date
  GROUP BY d.id, d.full_name
  ORDER BY appointments_count DESC, doctor_name ASC;
END $$

-- B) Doctor with most distinct patients in a specific month
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByMonth $$
CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(IN p_year INT, IN p_month INT)
BEGIN
  SELECT
      d.id AS doctor_id,
      d.full_name AS doctor_name,
      COUNT(DISTINCT a.patient_id) AS distinct_patients
  FROM appointments a
  JOIN doctors d ON d.id = a.doctor_id
  WHERE YEAR(a.appointment_time) = p_year
    AND MONTH(a.appointment_time) = p_month
  GROUP BY d.id, d.full_name
  ORDER BY distinct_patients DESC, doctor_name ASC
  LIMIT 1;
END $$

---- C) Doctor with most distinct patients in a given year
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByYear $$
CREATE PROCEDURE GetDoctorWithMostPatientsByYear(IN p_year INT)
BEGIN
  SELECT
      d.id AS doctor_id,
      d.full_name AS doctor_name,
      COUNT(DISTINCT a.patient_id) AS distinct_patients
  FROM appointments a
  JOIN doctors d ON d.id = a.doctor_id
  WHERE YEAR(a.appointment_time) = p_year
  GROUP BY d.id, d.full_name
  ORDER BY distinct_patients DESC, doctor_name ASC
  LIMIT 1;
END $$

DELIMITER ;

-- 4) Test Calls (Deliverables) -----------------------------------------------
-- Run the daily report
CALL GetDailyAppointmentReportByDoctor('2025-12-15');

-- Run the monthly top-doctor report
CALL GetDoctorWithMostPatientsByMonth(2025, 12);

-- Run the yearly top-doctor report
