import { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import Login from './pages/Login';
import ContextCards from './pages/ContextCards';
import Results from './pages/Results';
import TripDetail from './pages/TripDetail';
import MyTrips from './pages/MyTrips';
import Profile from './pages/Profile';
import Dashboard from './pages/Dashboard';
import './index.css';

function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));

  const handleLogout = () => {
    localStorage.removeItem('token');
    setToken(null);
    window.location.href = '/login';
  };

  return (
    <Router>
      <div className="app-container">
        <nav className="navbar glass-panel">
          <h1>🌍 TravelPlanner AI</h1>
          {token && (
            <div className="nav-links">
              <Link to="/plan/new">Lên kế hoạch</Link>
              <Link to="/trips">Chuyến đi của tôi</Link>
              <Link to="/profile">Hồ sơ</Link>
              <button onClick={handleLogout}>Đăng xuất</button>
            </div>
          )}
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/login" element={<Login onLoginSuccess={() => setToken(localStorage.getItem('token'))} />} />
            <Route path="/plan/new" element={token ? <ContextCards /> : <Navigate to="/login" />} />
            <Route path="/plan/results" element={token ? <Results /> : <Navigate to="/login" />} />
            <Route path="/trip/:id" element={token ? <TripDetail /> : <Navigate to="/login" />} />
            <Route path="/trips" element={token ? <MyTrips /> : <Navigate to="/login" />} />
            <Route path="/profile" element={token ? <Profile /> : <Navigate to="/login" />} />
            <Route path="/" element={token ? <Dashboard /> : <Navigate to="/login" />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;