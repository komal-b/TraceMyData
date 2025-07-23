# 📡 TraceMyData

**TraceMyData** is a full-stack privacy awareness platform that empowers users to analyze websites for privacy practices. It extracts metadata, detects third-party trackers using automated crawling, and computes a privacy risk score using a machine learning model.
<p align="center">
  <img src="https://img.shields.io/badge/Backend-Java%20%7C%20Spring%20Boot-blue" />
  <img src="https://img.shields.io/badge/Frontend-React%20%7C%20TypeScript-green" />
  <img src="https://img.shields.io/badge/ML-Python%20Microservice-yellow" />
  <img src="https://img.shields.io/badge/Auth-JWT%20%7C%20OAuth-red" />
  <img src="https://img.shields.io/badge/Container-Docker-blueviolet" />
</p>

---

## 🔑 Features

- **🔐 User Authentication**
  - Register and log in securely using email/password (hashed with `PasswordEncoder`)
  - Email verification (double opt-in)
  - Social login via **Google OAuth**
  - JWT-based session management

- **🌐 Website Analysis**
  - Submit a website URL to scan
  - Extract metadata like page title, description
  - Detect third-party trackers and cookies using automated browser crawling

- **📊 Privacy Risk Scoring**
  - ML-based privacy score generated from extracted data
  - Note: Model accuracy is limited due to dataset constraints

- **📁 Report Generation**
  - Download privacy analysis as a CSV report

- **📚 Privacy Education**
  - In-app guidance and tips on staying private online

---

## 🛠️ Tech Stack

| Layer         | Technology                                 |
|---------------|---------------------------------------------|
| **Frontend**  | React (TypeScript), Tailwind CSS            |
| **Backend**   | Java, Spring Boot, Spring MVC, Spring JPA   |
| **ML Service**| Python (Flask or FastAPI)                   |
| **Database**  | PostgreSQL                                  |
| **Auth**      | JWT, Google OAuth                           |
| **DevOps**    | Docker, Docker Compose, Kubernetes                      |

---


## 💻 Usage

1. **Login/Register** via **email** or **Google OAuth**
2. **Submit a website URL** on your dashboard
3. The app will automatically:
   - 🕷️ Crawl the website
   - 🏷️ Extract metadata (title, description, etc.)
   - 🧠 Detect trackers and third-party scripts
   - 📊 Score the website for privacy risk using an ML model
4. **View results** on your dashboard
5. **Download** a detailed report in **CSV** format
6. **Read privacy tips** and educational content tailored to your scan


## 🧠 Architecture Overview

- **Frontend** *(React + Tailwind CSS)*  
  - Handles user interaction, OAuth login UI  
  - Input form for website URLs  
  - Dashboard to view scan results  
  - CSV report viewer

- **Backend** *(Spring Boot)*  
  - REST API for frontend communication  
  - User registration, login, JWT authentication  
  - Tracker detection and metadata processing

- **ML Microservice** *(Python)*  
  - Receives website data from backend  
  - Computes and returns a privacy risk score using an ML model

- **Containerized with Docker Compose**  
  - All components (frontend, backend, ML service, PostgreSQL) run as containers  
  - Single command to build and launch the full system
 
- **Kubernetes Deployement**
  - kubectl apply -f k8s/
 
## ⚠️ Limitations

- 🔍 The ML model has **limited accuracy** due to a **small training dataset**
- 🕵️ Tracker detection is based on **heuristic patterns** and a list of **known scripts**, which may not catch all trackers
