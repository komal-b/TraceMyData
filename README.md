# TraceMyData

A full-stack privacy awareness platform to help users analyze websites for metadata, trackers, and privacy risk scores using AI and scraping tools.

## Tech Stack

 **Language:** Java
* **Framework:** Spring Boot
* **Web Framework:** Spring Web MVC (for REST controllers)
* **ORM/Data Access:** Spring Data JPA
* **Security:** `PasswordEncoder` (for password hashing)
* **Database:** PostgreSQL
* **DevOps:** Docker
* **Frontend:** React, TailwindCSS, TypeScript

## Folder Structure
- `frontend/` - React frontend
- `backend/` - Java Spring Boot backend
- `ml_service/` - Python-based ML model API

## Functionalities Added in Project
### 1 Home Page

The Home Page serves as the primary landing page for the TraceMyData application, providing an inviting introduction to its purpose and core features. It is designed to onboard new users, offer a clear path for existing users, and showcase the value proposition of the service.

#### 1.1 Key Functionalities

* **User Redirection (Session Management):**
    * Utilizes a `useEffect` hook to check for a stored user session (`localStorage.getItem('user')`).
    * If a user is already logged in (a `user` item exists in `localStorage`), they are automatically redirected to the `/dashboard` page, preventing them from needing to navigate past the landing page unnecessarily.
* **Call-to-Action (CTA) for New Users:**
    * If no user session is detected in `localStorage`, two prominent call-to-action buttons are displayed:
        * "Get Started - It's Free": A primary button that directs new users to the `/register` page to begin their sign-up journey.
        * "Existing User? Login": A secondary button that provides a direct link to the `/login` page for returning users.
    * These buttons are conditionally rendered, ensuring they are only shown when relevant (i.e., when a user is not already logged in).


### 2. Authentication and User Management

#### 2.1 Registration Functionality

This system implements a secure user registration process with email verification, commonly known as a "double opt-in" method. This ensures that only users with valid and accessible email addresses can complete their registration and create an account.

##### 2.1.1 Purpose of Registration

The primary purpose of registration is to allow new users to create an account within the application. This account creation is subject to email verification, which serves to:

* Confirm the user's email address belongs to them.
* Prevent fraudulent or spam registrations.
* Enable further authenticated interactions with the application's features once verified.

##### 2.1.2 User Input Requirements

For initial registration, the system expects a `RegisterRequest` object, which at minimum would contain:

* `email`: The user's email address.
* `password`: The user's chosen password.
* `firstName`: The user's first name.
* `lastName`: The user's last name.

**Validation Rules:**

* **Unique Email:** The system checks if the provided email is already present in the main user database (`User` table) or if there's a pending registration request for it in the temporary user database (`temp_user` table). If either is true, an error is returned.
* **Password Handling:** The provided password is not stored in plain text. Instead, it is immediately encoded (hashed) using `passwordEncoder.encode()` before being saved to the `temp_user` record. This is a critical security measure.

##### 2.1.3 Process Flow (Step-by-Step)

The registration process is divided into two main stages: **Initiation** and **Verification**.

**Initiate Registration (`POST /register` endpoint):**

1.  A user sends a `POST` request to the `/register` endpoint with their registration details (first name, last name, email, password).
2.  The system first checks if the email is already registered in the `User` database or if there's a pending registration in the `temp_user` database.
3.  If the email is new and not pending, a unique `token` (UUID) is generated.
4.  A `temp_user` record is created, storing the user's details (with the password encoded), the generated `token`, and an `expiresAt` timestamp (24 hours from creation).
5.  An email containing a verification link (which includes the `token`) is sent to the user's provided email address via `emailService.sendVerificationEmail`.
6.  The `temp_user` record is saved to the `temp_user` table.
7.  A success message, "Verification email sent," is returned to the client.

**Verify Registration (`GET /verify` endpoint):**

1.  The user receives the verification email and clicks on the embedded link, which sends a `GET` request to the `/verify` endpoint, including the `token` as a request parameter.
2.  The system attempts to find the `temp_user` record associated with the provided token.
3.  It checks if the token is valid and has not expired (within the 24-hour window).
4.  If valid and not expired, a new `User` entity is created.
5.  The user's first name, last name, email, and the encoded password from `temp_user` are copied to the new `User` entity. The `authProvider` is set to "local".
6.  The new `User` record is saved to the main `User` database.
7.  The corresponding `temp_user` record is then deleted from the `temp_user` table to clean up temporary data.
8.  A success message, "Email verified! Redirecting to login...", is returned.

##### 2.1.4 Backend Logic

* **Technology Stack:** The backend is built using **Java with Spring Boot**. `@PostMapping` and `@GetMapping` annotations indicate RESTful API endpoints.
* **Database Tables:**
    * `User`: Stores persistent data of registered users after email verification is complete.
    * `temp_user`: Stores temporary data of registered users who are awaiting email verification. Records are held here until verification is successful or the token expires.
* **Password Security:** Passwords are never stored in plain text. `passwordEncoder.encode()` is used to hash the password before saving it, ensuring secure storage.
* **Token Generation:** `UUID.randomUUID().toString()` is used to generate a unique, unguessable token for email verification.
* **Email Service:** An `emailService` component is responsible for sending the actual verification emails.
* **Transaction Management:** The `@Transactional` annotation on the `register` method ensures that the operations within it (fetching `temp_user`, saving `User`, deleting `temp_user`) are part of a single database transaction. This guarantees atomicity: either all operations succeed, or none do, preventing data inconsistency.

##### 2.1.5 Error Handling

The system provides robust error handling for various scenarios:

* **Initial Registration Errors:**
    * "Email already registered": Returned if the email exists in the main `User` database.
    * "A registration request is already pending for this email": Returned if the email exists in the `temp_user` database.
    * General `Exception` during initiation: Caught and returned as a `ResponseEntity.badRequest()` with the exception message.
* **Verification Errors:**
    * "Invalid or Expired token. Try registering again.": Returned if the provided token doesn't match an existing `temp_user` record or if the token has expired (beyond 24 hours).
    * General `Exception` during verification: Caught and returned as a `ResponseEntity.badRequest()` with the exception message.



