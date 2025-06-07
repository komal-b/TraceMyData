import {  useEffect, useState } from 'react';
import type { ChangeEvent, FormEvent } from 'react';
import { GoogleLogin } from '@react-oauth/google';
import  { Link, useNavigate } from 'react-router-dom';
import { AiOutlineEye, AiOutlineEyeInvisible } from 'react-icons/ai';
import { jwtDecode } from "jwt-decode";
import type { User } from '../type/User';


interface LoginForm {
  email: string;
  password: string;
}

export default function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState<LoginForm>({
    email: '',
    password: '',
  });
  const [serverError, setServerError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  
  

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(form),
    });

    
   
    if (!response.ok) {
      const message = await response.text();
      setServerError(message); // show "Error logging in"
      return;
    }
    const userData = await response.json();
    console.log('Login Response:', userData);
    const user: User = {
        email: userData.email,
        firstName: userData.firstName,
        lastName: userData.lastName,
        profilePic: userData.profilePic || '',
        authProvider: userData.authProvider ,
        token: userData.token,
    };
      localStorage.setItem("user", JSON.stringify({ user }));
    navigate('/dashboard');
    // Navigate or show success
    } catch (error) {
      console.error('Error:', error);
      // Show error message to user
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
          Login
        </h2>

        {serverError && (
          <div className="text-red-500 text-sm text-center">
            {serverError}
          </div>
        )}

        {/* Input Fields */}
        

        <input
          className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          required
        />
       
        <div className="relative">
          <input
            className="w-full p-3 rounded-lg border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500"
            type={showPassword ? 'text' : 'password'}
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
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
  
          {/* Submit Button */}
        <button
          type="submit"
          className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white p-3 rounded-lg font-semibold shadow-md transition-all"
        >
          Login
        </button>

        {/* Social Buttons */}
        <div className="flex flex-col gap-3 pt-4">
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

              console.log('Google Login Response:', res);
              
              const decoded: any = jwtDecode(idToken);
              
              
              const userVal = await res.json();
              if (!userVal || !userVal.email) {
                console.error('Invalid user data received from Google');
                return;
              }
              const user: User = {
              email: userVal.email,
              firstName: userVal.firstName,
              lastName: userVal.lastName,
              profilePic: decoded.profilePic || '',
              authProvider: userVal.authProvider ,
              token: userVal.token,
          };
            localStorage.setItem("user", JSON.stringify({ user }));
            
              
              navigate('/dashboard');

              if (!res.ok) throw new Error('Google Login failed');

              
            }}
            onError={() => {
              console.log('Login Failed');
            }}
          
        />
            <p className="text-center text-sm text-gray-600 pt-6">
            Don’t have an account?{' '}
            <Link to="/register" className="text-blue-600 font-medium hover:underline">
                Register
            </Link>
            </p>

        </div>
      </form>
    </div>
  );
}

