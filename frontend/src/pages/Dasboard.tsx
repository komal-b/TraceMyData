import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { logout } from '../utils/Logout';

interface User {
  email: string;
  name?: string;
  profilePic?: string;
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (!stored) {
      navigate('/login'); // redirect if no user found
    } else {
      setUser(JSON.parse(stored));
    }
  }, [navigate]);


  const handleAccountSettings = () => {
    navigate('/account');
  };

  if (!user) return null; // Loading fallback

  const initials = user.email?.charAt(0).toUpperCase();
  const profilePic = user.profilePic || null;

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Top Bar */}
      <div className="bg-white shadow p-4 flex justify-between items-center">
        <h1 className="text-xl font-bold text-gray-800">📊 Dashboard</h1>

        {/* User Avatar and Email */}
        <div className="relative">
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center gap-2 focus:outline-none"
          >
            {profilePic ? (
              <img
                src={profilePic}
                alt="Profile"
                className="w-9 h-9 rounded-full border"
              />
            ) : (
              <div className="w-9 h-9 rounded-full bg-blue-600 text-white flex items-center justify-center font-semibold">
                {initials}
              </div>
            )}
            <span className="text-sm text-gray-600 hidden sm:block">{user.email}</span>
          </button>

          {/* Dropdown */}
          {dropdownOpen && (
            <div className="absolute right-0 mt-2 w-48 bg-white rounded shadow-md z-10 border">
              <button
                onClick={handleAccountSettings}
                className="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
              >
                Account Settings
              </button>
              <button
                onClick={() => logout(navigate)}
                className="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-100"
              >
                Logout
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Content */}
      <div className="p-6">
        <p className="text-lg text-gray-700">Welcome to your dashboard!</p>
      </div>
    </div>
  );
}
