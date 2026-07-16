import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import Login from './pages/Login';
import Onboarding from './pages/Onboarding';
import Destinations from './pages/Destinations';
import Planner from './pages/Planner';
import './index.css';

function App() {
  const token = localStorage.getItem('token');

  return (
    <Router>
      <div className="app-container">
        <nav className="navbar">
          <h1>TravelPlanner AI</h1>
          {token && (
            <div className="nav-links">
              <Link to="/onboarding">Onboarding</Link>
              <Link to="/destinations">Explore</Link>
              <Link to="/planner">Planner</Link>
              <button onClick={() => {
                localStorage.removeItem('token');
                window.location.href = '/login';
              }}>Logout</button>
            </div>
          )}
        </nav>
        
        <main className="main-content">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/onboarding" element={token ? <Onboarding /> : <Navigate to="/login" />} />
            <Route path="/destinations" element={token ? <Destinations /> : <Navigate to="/login" />} />
            <Route path="/planner" element={token ? <Planner /> : <Navigate to="/login" />} />
            <Route path="/" element={<Navigate to={token ? "/destinations" : "/login"} />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
