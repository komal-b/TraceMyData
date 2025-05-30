import { Link } from 'react-router-dom';

export default function CheckEmail() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-white px-4 py-12">
      <div className="max-w-md text-center">
        <h2 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-500 to-purple-500 mb-4">
          Verify Your Email
        </h2>
        <p className="text-gray-700 mb-6">
          We've sent a verification link to your email address which will expire in 24 hours. Please check your inbox and click the link to activate your account.
        </p>
        <p className="text-sm text-gray-500 mb-6">
          Didn’t receive the email? Please check your spam folder or try registering again.
        </p>
        <Link
          to="/login"
          className="text-blue-600 font-semibold hover:underline"
        >
          Back to Login
        </Link>
      </div>
    </div>
  );
}
