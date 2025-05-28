import { useState } from 'react';
import type { ChangeEvent, FormEvent, FocusEvent } from 'react';
import gmail from '../assets/gmail.png';
import { Link } from 'react-router-dom';



interface RegisterForm {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export default function Register() {
  const [form, setForm] = useState<RegisterForm>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

  const [emailError, setEmailError] = useState<string | null>(null);
  const [passwordFocused, setPasswordFocused] = useState(false);
  const [passwordError, setPasswordError] = useState<string | null>(null);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });

    if (e.target.name === 'email') {
      validateEmail(e.target.value);
    }
    if (e.target.name === 'password') {
      if (passwordFocused) {
        validatePassword(e.target.value);
      }
    }
  };

  const validatePassword = (password: string) => {
  const pattern =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
  if (!pattern.test(password)) {
    setPasswordError('Password does not meet complexity requirements.');
  } else {
    setPasswordError(null);
  }
};


  const validateEmail = (email: string) => {
    // Strong email regex pattern
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!pattern.test(email)) {
      setEmailError('Please enter a valid email address.');
    } else {
      setEmailError(null);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    // Final validation before submission
    if (emailError) return alert('Fix email error before submitting.');

    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(form),
      });

      if (!response.ok) throw new Error('Registration failed');

      const data = await response.json();
      console.log('Success:', data);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white px-4 py-12">
      <form
        onSubmit={handleSubmit}
        className="bg-white rounded-xl shadow-xl p-8 w-full max-w-md text-gray-800 space-y-6 border border-gray-200"
      >
        <h2 className="text-3xl font-bold text-center bg-gradient-to-r from-blue-500 to-purple-500 bg-clip-text text-transparent">
          Create Account
        </h2>

        <input
          className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
          type="text"
          name="firstName"
          placeholder="First Name"
          value={form.firstName}
          onChange={handleChange}
          required
        />
        <input
          className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
          type="text"
          name="lastName"
          placeholder="Last Name"
          value={form.lastName}
          onChange={handleChange}
        />
        <input
          className={`w-full p-3 rounded-lg border ${
            emailError ? 'border-red-500' : 'border-gray-300'
          } focus:outline-none focus:ring-2 focus:ring-blue-500`}
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          required
        />
        {emailError && (
          <p className="text-sm text-red-500 mt-[-12px]">{emailError}</p>
        )}

        <input
  className={`w-full p-3 rounded-lg border ${
    passwordError ? 'border-red-500' : 'border-gray-300'
  } focus:outline-none focus:ring-2 focus:ring-blue-500`}
  type="password"
  name="password"
  placeholder="Password"
  value={form.password}
  onChange={handleChange}
  onFocus={() => setPasswordFocused(true)}
  onBlur={(e: FocusEvent<HTMLInputElement>) => {
    if (!e.target.value) setPasswordFocused(false);
  }}
  required
/>
{passwordFocused && (
  <div className="text-sm text-gray-500 text-left mt-1 space-y-1">
    <p>Password must contain:</p>
    <ul className="list-disc list-inside text-gray-600">
      <li>At least 8 characters</li>
      <li>At least one uppercase letter (A-Z)</li>
      <li>At least one lowercase letter (a-z)</li>
      <li>At least one number (0-9)</li>
      <li>At least one special character (!@#$%^&*)</li>
    </ul>
  </div>
)}
{passwordError && (
  <p className="text-sm text-red-500 mt-1">{passwordError}</p>
)}


        <button
          type="submit"
          className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white p-3 rounded-lg font-semibold shadow-md transition-all"
        >
          Register
        </button>

        {/* Social Buttons */}
        <div className="flex flex-col gap-3 pt-4">
          <button
            type="button"
            className="w-full flex items-center justify-center gap-2 border border-gray-300 p-3 rounded-lg hover:bg-gray-100 transition"
          >
            <img src={gmail} alt="Google" className="w-8 h-8" />
            Register with Google
          </button>
        </div>
        <p className="text-center text-sm text-gray-600 pt-4">
          Have an account?{' '}
          <Link
            to="/login"
            className="text-blue-600 font-medium hover:underline"
          >
            Login
          </Link>
        </p>
      </form>
    </div>
  );
}
