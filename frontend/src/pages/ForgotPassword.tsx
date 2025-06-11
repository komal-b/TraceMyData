import { useState, type FormEvent, type ChangeEvent, useEffect } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
const [emailError, setEmailError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
    if (e.target.value === 'email') {
      validateEmail(e.target.value);
    }
  };

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      navigate('/dashboard');
    }
  }, [navigate]);
  
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
    setMessage(null);
    setError(null);

    try {
      const response = await fetch('http://localhost:8080/api/auth/forgot-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email }),
      });

      const message = await response.text();

      if (!response.ok) {
        setMessage(message); // show "Email already registered"
        return;
      }

      setMessage('If this email is registered, you will receive a password reset link shortly.');
    } catch (err) {
      setError('Failed to send reset email. Please try again.');
    }
  };

  const location = useLocation();
  if (!location.state?.fromLogin) {
    return <Navigate to="/login" replace />;
  }
  return (
    <div className="min-h-screen flex items-center justify-center bg-white px-4 py-12">
      <form
        onSubmit={handleSubmit}
        className="bg-white rounded-xl shadow-xl p-8 w-full max-w-md text-gray-800 space-y-6 border border-gray-200"
      >
        <h2 className="text-3xl font-bold text-center text-gray-800">
          Forgot Password
        </h2>

        <p className="text-center text-gray-600">
          Enter the email address registered with TraceMyData.
        </p>

        {message && (
          <div className="text-green-600 text-center text-sm">
            {message}
          </div>
        )}

        {error && (
          <div className="text-red-600 text-center text-sm">
            {error}
          </div>
        )}
        {
            emailError && (
                <div className="text-red-600 text-center text-sm">
                {emailError}
                </div>
            )
        }

        <input
            className={`w-full p-3 rounded-lg border ${
                emailError ? 'border-red-500' : 'border-gray-300'
            } focus:outline-none focus:ring-2 focus:ring-blue-500`}
            type="email"
            placeholder="Email"
            value={email}
            onChange={handleChange}
            required
            />
            
        <button
          type="submit"
          className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white p-3 rounded-lg font-semibold shadow-md transition-all"
        >
          Send Reset Link
        </button>
      </form>
    </div>
  );
}


