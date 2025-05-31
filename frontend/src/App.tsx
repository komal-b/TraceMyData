// src/App.tsx
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Register from './pages/Register';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Login from './pages/Login';
import CheckEmail from './pages/CheckEmail';
import Verify from './pages/Verify';
import CompleteRegistration from './pages/CompleteRegistration';

function App() {
  return (
    <Router>
      <div className="flex flex-col min-h-screen bg-gray-900 text-white">
        {/* Navigation */}
        <Navbar />

        {/* Main Content */}
        <main className="flex-grow">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/register" element={<Register />} />
            <Route path="/login" element={<Login />} />
            <Route path="/check-email" element={<CheckEmail />} />
            <Route path="/verify" element={<Verify />} />
            <Route path="/complete-registration" element={<CompleteRegistration />} />
            {/* Add more routes as needed */}
          </Routes>
        </main>

        {/* Footer */}
        <Footer />
      </div>
    </Router>
  );
}

export default App;