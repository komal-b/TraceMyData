import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ProfileUpdate from '../components/ProfileUpdate';
import type { User } from '../type/User';

export default function Dashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('metadata');
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [user, setUser] = useState<User | null>(null);
 
  

  // Auto-collapse on small screens
  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (!stored) {
      navigate('/login');
    } else {
      setUser(JSON.parse(stored).user);
      
    }

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
            <h3 className="text-xl font-bold mb-2">Website Metadata Analyzer</h3>
            <ul className="list-disc ml-6 text-gray-700 space-y-1">
              <li>URL Input Field</li>
              <li>Metadata Extraction (title, description, OG tags)</li>
              <li>Site Preview Card</li>
            </ul>
          </div>
        );
      case 'trackers':
        return (
          <div>
            <h3 className="text-xl font-bold mb-2">Tracker Detection</h3>
            <ul className="list-disc ml-6 text-gray-700 space-y-1">
              <li>Detect Embedded Trackers (e.g., Google Analytics)</li>
              <li>Display 3rd-Party Requests (Playwright/Headless Browser)</li>
              <li>Show Tracker Risk Level</li>
            </ul>
          </div>
        );
      case 'risk':
        return (
          <div>
            <h3 className="text-xl font-bold mb-2">Privacy Risk Score</h3>
            <ul className="list-disc ml-6 text-gray-700 space-y-1">
              <li>NLP on Privacy/Cookie Policy</li>
              <li>Risk Scoring (0–100)</li>
              <li>Risk Reason Tags</li>
              <li>Color-coded Output</li>
            </ul>
          </div>
        );
      case 'education':
        return (
          <div>
            <h3 className="text-xl font-bold mb-2">Privacy Education</h3>
            <ul className="list-disc ml-6 text-gray-700 space-y-1">
              <li>Show Guides (e.g., "What is fingerprinting?")</li>
              <li>Link to Tools/Extensions (VPNs, Ad Blockers)</li>
              <li>Learn More Page (EFF, Mozilla articles)</li>
            </ul>
          </div>
        );
      case 'history':
        return (
          <div>
            <h3 className="text-xl font-bold mb-2">Scan History Dashboard</h3>
            <ul className="list-disc ml-6 text-gray-700 space-y-1">
              <li>Timeline of Scanned Sites</li>
              <li>Export Results (CSV/PDF)</li>
              <li>Bookmark/Favorite Sites</li>
            </ul>
          </div>
        );
      default:
        return <p className="text-gray-700">Welcome to your dashboard.</p>;
    }
  };

  return (
  
    <div className="min-h-screen flex bg-gray-100">
      {showModal && user && (
        <ProfileUpdate
                  open={showModal}
                  onClose={() => setShowModal(false)}
                  user={user}  token={user.token ?? ''}      />
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
            ['history', 'Scan History Dashboard'],
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
