import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ProfileUpdate from '../components/ProfileUpdate';
import type { User } from '../type/User';
import WebsiteMetadata from './WebsiteMetadata';
import { logout } from '../utils/Logout';
import TrackerDetection from './TrackerDetection';
import PrivacyEducation from './PrivayEducation';
import DownloadReports from './DownloadReports';
import PrivacyRiskScore from './PrivacyRiskScore';

export default function Dashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('metadata');
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [user, setUser] = useState<User | null>(null);
  
  const isTokenExpired = (token: string) => {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch (e) {
      console.error('Invalid token', e);
      return true; // Consider invalid tokens as expired
    }
  };
  

  // Auto-collapse on small screens
  useEffect(() => {
    const stored = localStorage.getItem('user');

    if (!stored) {
      navigate('/login');
      return;
    }
    const parsedUser = JSON.parse(stored);
    const token = parsedUser.user?.token;
    if (!token || isTokenExpired(token)) {
      logout(navigate);
      return;
    }

     setUser(parsedUser.user);

    const handleResize = () => {
      setIsCollapsed(window.innerWidth < 768);
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [navigate]);

  useEffect(() => {
    const handleOpen = () => setShowModal(true);
    window.addEventListener('open-profile-update', handleOpen);
    return () => window.removeEventListener('open-profile-update', handleOpen);
  }, []);

  const renderContent = () => {
    switch (activeTab) {
      case 'metadata':
        return (
          <div>
             <WebsiteMetadata />; 
          </div>
        );
      case 'trackers':
        return (
          <div>
            <TrackerDetection />; 
          </div>
        );
      case 'risk':
        return (
          <div>
            <PrivacyRiskScore />
          </div>
        );
      case 'education':
        return (
          <div>
            <PrivacyEducation />
          </div>
        );
      case 'reports':
        return (
          <div>
            <DownloadReports />
          </div>
        );
      default:
        return <p className="text-gray-700">Welcome to your dashboard.</p>;
    }
  };

  function handleUserUpdate(updatedUser: User): void {
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify({ user: updatedUser }));
  }

  return (
  
    <div className="min-h-screen flex bg-gray-100">
      {showModal && user && (
        <ProfileUpdate
                  open={showModal}
                  onClose={() => setShowModal(false)}
                  user={user}  token={user.token ?? ''}  
                  onUserUpdate={handleUserUpdate}    />
      )}

      <aside
        className={`$${
          isCollapsed ? 'w-16' : 'w-64'
        } transition-all duration-300 bg-gray-50 shadow-md border-r border-gray-200 p-4 space-y-4`}
      >
        <div className="flex justify-between items-center mb-6">
          {!isCollapsed && (
            <h2 className="text-xl font-bold text-gray-800">TraceMyData</h2>
          )}
          <button
            onClick={() => setIsCollapsed(prev => !prev)}
            className="text-gray-600 hover:text-blue-600 focus:outline-none"
          >
            {isCollapsed ? '➤' : '←'}
          </button>
        </div>

        <nav className="space-y-2">
          {[
            ['metadata', 'Website Metadata Analyzer'],
            ['trackers', 'Tracker Detection'],
            ['risk', 'Privacy Risk Score'],
            ['education', 'Privacy Education'],
            ['reports', 'Download Reports'],
          ].map(([key, label]) => (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              className={`flex items-center text-left w-full px-3 py-2 rounded-md transition ${
                activeTab === key
                  ? isCollapsed
                    ? 'text-blue-700'
                    : 'bg-blue-100 text-blue-700 font-medium'
                  : 'text-gray-700 hover:bg-gray-100'
              }`}
            >
              <span className={`${isCollapsed ? 'hidden' : 'inline'} ml-1`}>
                {label}
              </span>
            </button>
          ))}
        </nav>
      </aside>

      <main className="flex-1 p-8">
        <h2 className="text-2xl font-semibold text-gray-800 mb-4 capitalize">
          {activeTab.replace(/^\w/, c => c.toUpperCase())}
        </h2>
        <div className="bg-white shadow-md rounded-lg p-6">{renderContent()}</div>
      </main>
    </div>
  );
}
