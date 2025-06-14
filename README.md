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

This system implements a secure user registration process with email verification, commonly known as a "double opt-in" method. This ensures that only users with valid and accessible email addresses can complete their registration and create an account also it allows GoogleOAuth Login

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

***Login Form Components***

* **Input Fields:** Includes fields for `email`, `password`, `firstName` and `lastName`.
* **Password Visibility Toggle:** A toggle button allows users to show or hide their password input for convenience and verification.
* **Social Login Button:** Integrates a `GoogleLogin` component to enable authentication via Google OAuth.
* **Login Link:** A convenient link to the `login` page for users who do not yet have an account.
* **Error Display:** A designated area to display server-side error messages (e.g., "Email already registered").


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

#### 2.2 Login Functionality

This section details the user login process, covering both local (email/password) and social (Google OAuth) authentication methods.

##### 2.2.1 Purpose of Login

The primary purpose of login is to allow existing users to securely authenticate themselves and gain access to their personalized `dashboard` and other authenticated features within the TraceMyData application.

##### 2.2.2 Login Form Components

* **Input Fields:** Includes fields for `email` and `password`.
* **Password Visibility Toggle:** A toggle button allows users to show or hide their password input for convenience and verification.
* **Forgot Password Link:** A link to the `forgot-password` page is provided for users who need to reset their password.
* **Social Login Button:** Integrates a `GoogleLogin` component to enable authentication via Google OAuth.
* **Registration Link:** A convenient link to the `register` page for users who do not yet have an account.
* **Error Display:** A designated area to display server-side error messages (e.g., "Invalid credentials").


##### 2.2.3 Backend Login Logic (`POST /login`)

The backend handles local (email/password) login requests via the `/login` endpoint.

* **Endpoint:** `POST /login`
* **Request Body:** Expects a `LoginRequest` object containing `email` and `password`.
* **Process (`authService.login`):**
    1.  **User Lookup:** Attempts to find a user in the `User` database (`userRepo.findByEmail`) using the provided email. If not found, a "User not found" error is thrown.
    2.  **Authentication Provider Check:** Verifies that the found user's `authProvider` is explicitly "local". If the user account was created via an OAuth provider (e.g., "google"), an error "This account uses [OAuth provider] login" is returned, guiding the user to the correct login method.
    3.  **Password Verification:** The provided plain-text password is compared against the stored hashed password (`user.getPasswordHash()`) using `passwordEncoder.matches()`. If they do not match, an "Invalid credentials" error is thrown.
    4.  **JWT Token Generation:** Upon successful email and password verification, a JSON Web Token (JWT) is generated for the authenticated user using `jwtUtil.generateToken(user)`.
    5.  **Response Mapping:** The `AuthResponse` object is created and populated with the user's `firstName`, `lastName`, `email`, `authProvider`, and the newly generated `token`.

##### 2.2.4 Backend Response and Error Handling

* **Success Response:**
    * A `ResponseEntity.ok()` is returned with an `AuthResponse` object in the body.
    * The `AuthResponse` contains essential user details (`firstName`, `lastName`, `email`, `authProvider`) and the generated JWT `token`.
* **Error Responses:**
    * `ResponseEntity.badRequest().body()` is returned for various authentication failures:
        * "User not found": If the email does not exist in the database.
        * "This account uses [OAuth provider] login": If a local login attempt is made for an OAuth-registered account.
        * "Invalid credentials": If the password does not match the stored hash.
        * General `Exception` handling catches other unexpected errors, returning their messages.

#### 2.3 OAuth Login Functionality (Backend)

This section describes how the backend handles authentication requests from OAuth providers, specifically Google. This functionality is accessed in Register nad Login Page

##### 2.3.1 Google OAuth Login Endpoint

* **Endpoint:** `POST /google`
* **Purpose:** Receives the Google ID Token from the frontend and initiates the backend Google OAuth verification and login process.
* **Request Body:** Expects a JSON body with a single field:
    * `idToken`: String, the Google ID Token obtained from the frontend.
* **Validation:** Checks if the `idToken` is present and not empty.
* **Process:** Delegates the token verification and login logic to `authService.loginWithGoogle(token)`.
* **Response:** Returns a `ResponseEntity.ok()` with the `AuthResponse` object from `authService` on success, or `ResponseEntity.badRequest()` on failure.

##### 2.3.2 Backend Google Login Logic (`authService.loginWithGoogle`)

This method is responsible for verifying the Google ID Token and processing the user's login.

* **Token Verification:**
    * It calls `jwtUtil.verifyGoogleToken(idToken)` to validate the authenticity and integrity of the Google ID Token. This method is expected to return a `Map<String, Object>` containing the token's payload (e.g., email, given_name, family_name).
* **Common OAuth Handling:** The verified payload is then passed to the `handleOAuthLogin` private method for further processing, along with the provider name ("google").

##### 2.3.3 Common OAuth Login Handling (`handleOAuthLogin`)

This is a transactional method designed to handle the login process for any OAuth provider, based on a verified payload.

* **Payload Data:** Extracts the user's `email` from the verified `payload`.
* **User Existence Check:**
    * It queries `userRepo.findByEmail(email)` to check if a user with that email already exists in the main `User` database.
    * **Existing User:** If the user exists, the existing `User` entity is retrieved.
    * **New OAuth User:** If the user does *not* exist, the `registerNewOAuthUser` method is called to create a new `User` record for this OAuth login.
* **Token Generation:** A JWT token is generated for the authenticated or newly registered `User` using `jwtUtil.generateToken(user)`.
* **Response:** Returns an `AuthResponse` object containing the user's details and the generated JWT.
* **Error Handling:**
    * Catches `DataIntegrityViolationException` for potential database errors during user registration (e.g., unique constraint violation).
    * Catches generic `Exception` for other OAuth login failures, throwing a `RuntimeException` with a "OAuth login failed" message.

##### 2.3.4 New OAuth User Registration (`registerNewOAuthUser`)

This private method is specifically for creating a new `User` entity when a user logs in via OAuth for the first time.

* **New User Creation:** Initializes a new `User` object.
* **Populate Fields:** Sets the `email`, `firstName` (from `given_name` in payload), `lastName` (from `family_name` in payload) from the OAuth payload.
* **Authentication Provider:** Explicitly sets the `authProvider` to "google" to indicate the origin of this account.
* **Save User:** The new `User` entity is saved to the main `User` database via `userRepo.save(newUser)`.

#### 2.4 Forgot Password Functionality

This section details the two-step password reset process, allowing users to regain access to their accounts if they forget their password.

##### 2.4.1 Purpose of Forgot Password 

The primary purpose is to provide a secure mechanism for users to reset their password without knowing their old one, ensuring that only the legitimate email account holder can initiate and complete the process. This helps in maintaining account security and usability.

##### 2.4.2 Initiate Password Reset (Frontend - `ForgotPassword` Component)

* **User Interface:**
    * An input field for the user's `email` address.
    * A "Send Reset Link" button to submit the request.
    * Areas to display messages (green for success, red for errors, including email validation errors).
* **State Management:**
    * `email`: Stores the user's input email.
    * `emailError`: Stores client-side email validation errors.
    * `message`: Stores success messages (e.g., "If this email is registered...").
    * `error`: Stores general error messages (e.g., "Failed to send reset email.").
* **Client-Side Validation:** The `validateEmail` function uses a regex pattern to ensure the entered text is a valid email format before submission.
* **Form Submission (`handleSubmit`):**
    * Upon submission, a `POST` request is sent to `http://localhost:8080/api/auth/forgot-password` with the `email` in the JSON body.
    * **Success Response:** If the response is `ok`, a generic success message is displayed to the user ("If this email is registered, you will receive a password reset link shortly.") to prevent email enumeration attacks.
    * **Error Response:** If the response is not `ok`, the specific error message from the backend is displayed.
* **Route Protection:** An `useEffect` hook ensures that if a user is already logged in, they are redirected to the `/dashboard`. Additionally, `useLocation` checks if the user navigated from the `/login` page; if not, they are redirected back to `/login` to ensure a proper flow.

##### 2.4.3 Initiate Password Reset (Backend - `/forgot-password` Endpoint)

The backend handles the request to initiate a password reset.

* **Endpoint:** `POST /forgot-password`
* **Purpose:** Receives a user's email and starts the password reset process by generating a token and sending a reset email.
* **Request Body:** Expects a JSON body with a single field: `email` (String).
* **Forgot Password flow**
    1.  **User Lookup:** Finds the `User` by the provided `email`. Throws "Email not registered" if no user is found.
    2.  **OAuth Account Check:** Prevents password reset for accounts registered via OAuth (e.g., Google), throwing "Cannot reset password for OAuth accounts."
    3.  **Pending Request Check:** Checks if a password reset request is already pending for this email in `tempUserRepository`. Throws "A password reset request is already pending..." if so.
    4.  **Token Generation:** Generates a unique `token` (UUID).
    5.  **Temporary User Record (`TempUser`) Creation:** A new `TempUser` record is created to store the user's `firstName`, `lastName`, `email`, the generated `token`, the hashed password from the `User` table, a `30-minute expiration timestamp`, and the original `User`'s `id`. This temporary record links the reset request to the actual user and their current password hash.
    6.  **Save Temporary User:** The `TempUser` record is saved to `tempUserRepository`.
    7.  **Send Email:** Calls  to dispatch the reset email.
* **Error Handling:**
    * Returns `RuntimeException` for specific business logic errors (e.g., "Email not registered", "Cannot reset password for OAuth accounts", "A password reset request is already pending").
    * Catches `DataIntegrityViolationException` and generic `Exception` for database or other failures during the process.

##### 2.4.4 Complete Password Reset (Frontend - `ResetPassword` Component)

The `ResetPassword.tsx` component allows users to set a new password using the token received in their email.

* **User Interface:**
    * Input fields for "New Password" and "Confirm New Password".
    * A password visibility toggle (`AiOutlineEye`/`AiOutlineEyeInvisible`).
    * A "Submit" button to finalize the password change.
    * Areas to display error messages (red) or success messages (green, with a spinning icon for redirection).
* **Token Retrieval:** Uses `useSearchParams` from `react-router-dom` to extract the `token` from the URL query parameters.
* **Client-Side Validation:**
    * Checks if a `token` is present in the URL.
    * Verifies that "New Password" and "Confirm New Password" fields match.
* **Form Submission (`handleSubmit`):**
    * Upon submission, a `POST` request is sent to `http://localhost:8080/api/auth/reset-password?token=${token}`.
    * The request body contains the `token` and `newPassword` (the value of the `password` state), serialized as JSON.
    * **Success Response:** If the response is `ok`, `setSuccess(true)` is called, and after a 3-second delay, the user is redirected to the `/login` page.
    * **Error Response:** If the response is not `ok`, the specific error message from the backend is displayed.
* **Session Check:** Similar to other authentication components, an `useEffect` redirects already logged-in users to the `/dashboard`.

##### 2.4.5 Complete Password Reset (Backend - `/reset-password` Endpoint)

The backend handles the final step of setting the new password.

* **Endpoint:** `POST /reset-password`
* **Purpose:** Validates the reset token and updates the user's password.
* **Request Parameters:**
    * `token`: String, the temporary token from the URL.
* **Request Body:** Expects a JSON body with a single field: `newPassword` (String).
* **`authService.resetPassword(String token, String newPassword)`:**
    1.  **Temporary User Lookup:** Finds the `TempUser` record by the provided `token`. Throws "Invalid or Expired token..." if not found.
    2.  **Token Expiry Check:** Verifies if the `TempUser` token has expired. If expired, the `TempUser` record is deleted, and an error is returned (though the provided code deletes it *before* throwing the expiry error, which might need slight adjustment in actual implementation for consistent error flow).
    3.  **New vs. Old Password Check:** Compares the `newPassword` (after hashing) with the *original* hashed password stored in `TempUser` (which was copied from the `User` table). Throws "New password cannot be the same as the old password" if they match.
    4.  **Original User Retrieval:** Retrieves the actual `User` entity using `tempUser.getUser_id()`. Throws "User not found" if somehow the user is missing.
    5.  **Password Update:** The `User`'s `passwordHash` is updated with the newly `passwordEncoder.encode(newPassword)`.
    6.  **Save User:** The updated `User` record is saved to `userRepo`.
    7.  **Clean Up:** The `TempUser` record is deleted from `tempUserRepository`.
    8.  **Logging:** `loggers.info` statements track the process.
* **Error Handling:**
    * Throws `RuntimeException` for various validation failures (e.g., invalid/expired token, new password is same as old, user not found).
    * These exceptions are caught by the controller and returned as `ResponseEntity.badRequest()`.

### 2.5 Profile Management

This section details the functionalities that allow authenticated users to manage and update their profile information and account credentials. 

#### 2.5.1 Update Profile (Name & Email)

This functionality allows users to modify their first name, last name, and, for local accounts, initiate an email change.

##### 2.5.1.1 Purpose

To provide users with the ability to keep their personal information accurate and, if necessary, update their primary email address linked to their account.

##### 2.5.1.2 User Interface (Frontend - `ProfileUpdate`, `NavBar` Component)



The `Navbar.tsx` component handles the display of user information and provides the logout option.

* **Conditional Display:** The navigation bar displays content based on the user's authentication status:
    * **Not Logged In:** Shows a "Home" link.
    * **Logged In:** Displays a profile avatar which, when clicked, reveals a dropdown menu containing a "Logout" button.
* **State Management:** Manages the `user` object and `dropdownOpen` state for the profile menu.
* **Session Initialization:** An `useEffect` hook populates the `user` state from `localStorage` on component mount.
* **Dropdown Control:** Uses `useRef` and `useEffect` to close the dropdown when a click occurs outside of it.

The `ProfileUpdate.tsx` component is a modal interface for updating profile details.

* **Inputs:** Fields for `firstName`, `lastName`.
* **Email Display & Change:**
    * Displays the current email.
    * For `local` authentication providers, a "Change Email" button is present. Clicking this reveals a new input field (`newEmail`) for the desired new email address.
    * Email input is disabled for Google authenticated users as their email is managed by Google.
    * A message indicates that a verification email will be sent for email changes.
* **Save Changes Button:** Submits the updated profile data.
* **Toast Notifications:** Uses `react-toastify` to display success or error messages to the user.
* **Client-Side Validation:** Validates the `newEmail` format using a regex.

##### 2.5.1.3 Frontend Process

1.  **Form Input:** User modifies `firstName`, `lastName`, and optionally `newEmail`.
2.  **Email Change Initiation:** If "Change Email" is clicked, the `newEmail` input becomes visible and editable.
3.  **`handleSave` Function:**
    * Performs client-side validation for `newEmail` if applicable.
    * Constructs a payload including `firstName`, `lastName`, and `newEmail` (conditionally).
    * Sends a `POST` request to `http://localhost:8080/api/auth/update-profile` with `Content-Type: application/json` and `Authorization: Bearer <token>`.
    * **Success Handling:**
        * Parses the JSON response from the backend.
        * Constructs an `updatedUser` object by merging the existing `user` prop with the updated data from the response. This ensures all `user` properties are preserved.
        * Updates `localStorage` with the `updatedUser` object.
        * Calls the `onUserUpdate` prop function to update the parent component's user state, ensuring UI consistency.
        * Displays a success toast message and then closes the modal (`onClose`).
    * **Error Handling:** Displays an error toast message with the backend's error.

##### 2.5.1.4 Backend Endpoints & Logic

* **Endpoint:** `POST /update-profile`
* **Purpose:** Handles general profile updates (first name, last name) and initiates the email change process.
* **Authorization:** Requires a valid JWT in the `Authorization` header. The user's `username` (email) is extracted from the authenticated `SecurityContextHolder`.
* **Request Body:** A JSON object containing `firstName`, `lastName`, and optionally `newEmail`.
* **Logic Branching:**
    * **Case 1: Update Name Only (No `newEmail` provided)**
        * Calls `authService.updateProfile(profileData, username)`.
        * `authService.updateProfile`:
            * Retrieves the `User` entity by the authenticated `email`.
            * Updates the `firstName` and `lastName` fields.
            * Saves the updated `User` entity to the database.
            * Generates a **new JWT token** for the updated user.
            * Returns an `AuthResponse` containing the updated user details and the new token.
        * Response: Returns `ResponseEntity.ok()` with a success message and the `AuthResponse` object.
    * **Case 2: Initiate Email Change (`newEmail` provided)**
        * Calls `updateEmail(profileData, username)`.
        * `updateEmail`:
            * Retrieves the `User` entity by the `oldEmail` (authenticated user's email).
            * Checks if the `newEmail` is already registered in the `User` table or if a `TempUser` record exists for the `newEmail` (pending verification). Throws `RuntimeException` if either is true.
            * Creates a new `TempUser` record:
                * Populates with `firstName`, `lastName` from `profileData`.
                * Sets the `email` to `newEmail`.
                * Copies the **original `User`'s `passwordHash`**.
                * Links to the original `User`'s `id`.
                * Generates a unique `token` for email verification.
                * Sets an `expiresAt` timestamp (24 hours).
            * Saves the `TempUser` record.
            * Sends a verification email to the `newEmail` address using `emailService.sendVerificationEmail`. If user clicks on link sent on email then the user will be logged out and need to login again with new email
            * Returns a success message indicating that a verification link has been sent.
        * Response: Returns `ResponseEntity.ok()` with a success message.
* **Error Handling:** Catches various `Exception` types, including `RuntimeException` for business logic errors (e.g., "User not found", "Email already registered", "Email already pending") and `DataIntegrityViolationException` for database errors, returning appropriate `ResponseEntity.badRequest()`.

#### 2.5.2 Change Password

This functionality allows users with local accounts to change their password by providing their old password and setting a new one.

##### 2.5.2.1 Purpose

To allow authenticated users to update their account password securely.

##### 2.5.2.2 User Interface (Frontend - `ProfileUpdate` Component)

The "Change Password" section within the `ProfileUpdate.tsx` modal.

* **Visibility:** This section is only rendered if the `user.authProvider` is 'local'.
* **Toggle:** Initially, a disabled password input field is shown. A "Change Password" button toggles the visibility of the actual input fields for old and new passwords.
* **Inputs:** `oldPassword` and `newPassword` fields.
* **Password Visibility Toggles:** Individual toggles for `oldPassword` and `newPassword` fields (`passwordVisible`, `confirmVisible`).
* **Update Password Button:** Submits the password change request.
* **Toast Notifications:** Used for displaying success or error messages.
* **State Management:** `oldPassword`, `newPassword`, `showChangePassword`, `passwordVisible`, `confirmVisible` states manage the password input fields.

##### 2.5.2.3 Frontend Process

1.  **Form Input:** User enters their `oldPassword` and `newPassword`.
2.  **`handlePasswordChange` Function:**
    * Sends a `POST` request to `http://localhost:8080/api/auth/change-password` with `Content-Type: application/json` and `Authorization: Bearer <token>`.
    * The request body contains `oldPassword` and `newPassword`.
    * **Success Handling:**
        * Displays a success toast message.
        * On the toast's close, the user is explicitly `logout` from the client-side (`localStorage` cleared), and redirected to the `/login` page. An alert notifies the user they must log in with their new password.
    * **Error Handling:** Displays an error toast message with the backend's error.

##### 2.5.2.4 Backend Endpoints & Logic

* **Endpoint:** `POST /change-password`
* **Purpose:** Verifies the old password and updates the user's password to the new one.
* **Authorization:** Requires a valid JWT in the `Authorization` header. The user's `username` (email) is extracted from the authenticated `SecurityContextHolder`.
* **Request Body:** A JSON object containing `oldPassword` and `newPassword`.
* **Input Validation:** Checks if both `oldPassword` and `newPassword` are provided and not empty.
* **`authService.changePassword(String username, String oldPassword, String newPassword)`:**
    1.  **User Lookup:** Retrieves the `User` entity using the authenticated `username` (email).
    2.  **Old Password Verification:** Uses `passwordEncoder.matches(oldPassword, user.getPasswordHash())` to compare the provided `oldPassword` with the stored hashed password. Throws "Old password is incorrect" if they don't match.
    3.  **New vs. Old Password Check:** Checks if the `newPassword` is the same as the current password (after hashing). Throws "New password cannot be the same as the old password" if they are identical.
    4.  **Password Update:** Encodes the `newPassword` using `passwordEncoder.encode()` and updates the `user.passwordHash`.
    5.  **Save User:** Saves the updated `User` entity to the database.
* **Response:** Returns `ResponseEntity.ok()` with a success message ("Password changed successfully") on success.
* **Error Handling:** Catches `Exception` types, including `RuntimeException` for business logic errors, returning `ResponseEntity.badRequest()` with the error message.

### 2.6 Logout Functionality

This functionality allows authenticated users to securely log out of their session, terminating their access to protected resources.

#### 2.6.1 Purpose

To provide a secure way for users to end their session, revoke their authentication token, and clear their local login state, preventing unauthorized access to their account from the current device.

#### 2.6.2 User Interface (Frontend - `Navbar` Component)

The `Navbar.tsx` component handles the display of user information and provides the logout option.

* **Conditional Display:** The navigation bar displays content based on the user's authentication status:
    * **Not Logged In:** Shows a "Home" link.
    * **Logged In:** Displays a profile avatar which, when clicked, reveals a dropdown menu containing a "Logout" button.
* **State Management:** Manages the `user` object and `dropdownOpen` state for the profile menu.
* **Session Initialization:** An `useEffect` hook populates the `user` state from `localStorage` on component mount.
* **Dropdown Control:** Uses `useRef` and `useEffect` to close the dropdown when a click occurs outside of it.

#### 2.6.3 Frontend Process

1.  **Logout Initiation:** Clicking the "Logout" button in the profile dropdown calls the `logout(navigate)` utility function.
2.  **`logout` Utility Function:**
    * This function (likely in `../utils/Logout.ts`) coordinates the client-side logout.
    * It **clears `localStorage`** by removing the "user" item.
    * It makes an asynchronous `POST` request to the backend's logout endpoint (e.g., `http://localhost:8080/logout`).
    * Finally, it redirects the user to the application's home or login page (`Maps('/')`).

#### 2.6.4 Backend Endpoint & Logic

* **Endpoint:** `POST /logout`
* **Purpose:** To invalidate the user's session and delete the authentication JWT cookie from the client's browser.
* **Request Method:** Uses `POST` for security best practices, as logout is a state-changing operation.
* **Cookie Invalidation:**
    1.  A `ResponseCookie` object is constructed to instruct the client's browser to delete the JWT cookie.
    2.  It sets the cookie's name to "jwt" with an empty value, sets the `path` to root (`/`), sets `maxAge` to `0` (immediate expiration), and includes `httpOnly(true)` and `secure(true)` for enhanced security.
* **Logging:** A `loggers.info` statement confirms the successful clearing of the JWT cookie.
* **Response:** Returns `ResponseEntity.ok()` with a `HttpHeaders.SET_COOKIE` header containing the invalidation cookie and a success message ("Logged out successfully"). This response triggers the browser to delete the cookie.

---














