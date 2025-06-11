import { useState, useEffect, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { logout } from '../utils/Logout';
import type { User } from '../type/User';

export default function Navbar() {
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const location = useLocation();
  const dropdownRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      console.log('Stored User:', storedUser);
      setUser(JSON.parse(storedUser).user);
     
    } else {
      setUser(null);
    }
    setDropdownOpen(false);
  }, [location]);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setDropdownOpen(false);
      }
    }

    if (dropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [dropdownOpen]);

  const initials = user?.email?.charAt(0).toUpperCase();
  const profilePic = user?.profilePic || '';

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 py-4">
        <div className="flex justify-between items-center">
          {/* Logo */}
          <Link
            to={user ? '/dashboard' : '/'}
            className="text-4xl font-bold bg-gradient-to-r from-blue-500 to-purple-500 bg-clip-text text-transparent"
          >
            TraceMyData
          </Link>

          {/* Right Side */}
          <div className="space-x-4">
            {!user ? (
              <Link
                to="/"
                className="text-gray-600 hover:text-blue-500 transition-colors"
              >
                Home
              </Link>
            ) : (
              <div className="relative inline-block text-left" ref={dropdownRef}>
                <button
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                  className="flex items-center space-x-2 bg-white focus:outline-none"
                >
                  <img
                    src={
                      profilePic ||
                      `https://ui-avatars.com/api/?name=${initials}&background=random`
                    }
                    alt="Avatar"
                    className="w-9 h-9 rounded-full border"
                  />
                </button>

                {dropdownOpen && (
                  <div className="absolute right-0 mt-2 w-64 bg-gray-100 text-sm rounded shadow-lg z-50 border border-gray-200">
                    <div className="text-gray-500 font-sm px-4 py-3 border-b border-gray-200">
                      {user.email}
                    </div>

                    <button
                      onClick={() => window.dispatchEvent(new Event('open-profile-update'))}
                      className="w-full text-left text-gray-800 px-4 py-3 bg-gray-100 hover:bg-gray-200 transition"
                    >
                      Profile Update
                    </button>

                    <button
                      onClick={() => logout(navigate)}
                      className="w-full text-left text-gray-800 px-4 py-3 bg-gray-100 hover:bg-gray-200 transition rounded-b"
                    >
                      Logout
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}