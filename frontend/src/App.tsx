// src/App.tsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Register from './pages/Register';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Login from './pages/Login';
import CheckEmail from './pages/CheckEmail';
import Verify from './pages/Verify';
import PrivateRoute from './components/PrivateRoute';
import Dashboard from './pages/Dasboard';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import WebsiteMetadata from './pages/WebsiteMetadata';
import TrackerDetection from './pages/TrackerDetection';
import PrivacyEducation from './pages/PrivayEducation';
import DownloadReports from './pages/DownloadReports';
import PrivacyRiskScore from './pages/PrivacyRiskScore';

function App() {
  return (
    <Router>
      <div className="flex flex-col min-h-screen bg-gray-900 text-white">
        <Navbar />

        {/* Main Content */}
        <main className="flex-grow">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/check-email" element={<CheckEmail />} />
            <Route path="/verify" element={<Verify />} />
            <Route
              path="/dashboard"
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              }
            />
            <Route path="/metadata" element={<WebsiteMetadata />} />
            <Route path="/tracker-detection" element={<TrackerDetection />} />
            <Route path="/privacy-risk-score" element={<PrivacyRiskScore />} />
            <Route path="/privacy-education" element={<PrivacyEducation />} />
            <Route path="/download-reports" element={<DownloadReports />} />
          </Routes>

        </main>

        <Footer />
      </div>
    </Router>
  );
}

export default App;
