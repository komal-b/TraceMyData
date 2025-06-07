import { useEffect, useState } from 'react';
import type { ChangeEvent, FormEvent, FocusEvent } from 'react';
import {  Link, useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import { AiOutlineEye, AiOutlineEyeInvisible } from 'react-icons/ai';
import { jwtDecode } from 'jwt-decode';

interface RegisterForm {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState<RegisterForm>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });

  const [emailError, setEmailError] = useState<string | null>(null);
  const [passwordFocused, setPasswordFocused] = useState(false);
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [serverError, setServerError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);




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
    setServerError(null);
    // Final validation before submission
    if (emailError) return alert('Fix email error before submitting.');

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(form),
      });

      const message = await response.text();

      if (!response.ok) {
      setServerError(message); // show "Email already registered"
      return;
    }

       
      navigate('/check-email', { state: { fromRegister: true } });

    } catch (error) {
      console.error('Error:', error);
    }
  };

  useEffect(() => {
  const storedUser = localStorage.getItem('user');
  if (storedUser) {
    navigate('/dashboard');
  }
}, []);

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
        {serverError && (
            <p className="text-sm text-red-500 mt-1">{serverError}</p>
        )}
        <div className="relative">
        <input
            className={`w-full p-3 rounded-lg border ${
                passwordError ? 'border-red-500' : 'border-gray-300'
            } focus:outline-none focus:ring-2 focus:ring-blue-500`}
            type={showPassword ? 'text' : 'password'}
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
        <button
          type="button"
          onClick={() => setShowPassword((prev) => !prev)}
          className="absolute right-3 top-1/2 transform -translate-y-1/2 text-sm text-blue-600 hover:underline focus:outline-none"
         >
        
        {showPassword ? <AiOutlineEye /> : <AiOutlineEyeInvisible />}
        </button>
        </div>
        
        {/* Password Guidelines */}
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
        

        {/* Submit Button */}

        <button
          type="submit"
          className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white p-3 rounded-lg font-semibold shadow-md transition-all"
        >
          Register
        </button>

        

        {/* Social Buttons */}
         <GoogleLogin
                    text='continue_with'
                    onSuccess={async credentialResponse => {
                      const idToken = credentialResponse.credential;
                      if (!idToken) {
                        console.error('No ID token received from Google');
                        return;
                      }
                      
                      const res = await fetch('http://localhost:8080/api/auth/google', {
                        method: 'POST',
                        headers: {
                          'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({ idToken }),
                      });
        
                      
                      const decoded: any = jwtDecode(idToken);
                      const profilePic = decoded.picture || '';
                      const email = decoded.email;
                      
                      localStorage.setItem("user", JSON.stringify({ email, profilePic }));
                      navigate('/dashboard');
        
                      if (!res.ok) throw new Error('Google Login failed');
        
                      
                    }}
                    onError={() => {
                      console.log('Login Failed');
                    }}
                  
                />
       

      
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


