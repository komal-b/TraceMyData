import { Link } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav className="bg-white">
      <div className="max-w-7xl mx-auto px-4 py-4">
        <div className="flex justify-between items-center">
          {/* Gradient Text for Logo */}
          <Link
            to="/"
            className="text-4xl font-bold bg-gradient-to-r from-blue-500 to-purple-500 bg-clip-text text-transparent"
          >
            TraceMyData
          </Link>

          {/* Light-colored Home link */}
          <div className="space-x-4">
            <Link
              to="/"
              className="text-gray-600 hover:text-blue-500 transition-colors"
            >
              Home
            </Link>
          </div>
        </div>
      </div>
    </nav>
  );
}
