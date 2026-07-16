import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isRegister, setIsRegister] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (isRegister) {
        await api.post('/auth/register', { email, password, role: 'USER' });
        alert('Registration successful. Please log in.');
        setIsRegister(false);
      } else {
        const res = await api.post('/auth/login', { email, password });
        localStorage.setItem('token', res.data.data.token);
        // check if user has onboarding, for simplicity just redirect to destinations
        navigate('/destinations');
      }
    } catch (err) {
      alert('Authentication failed.');
    }
  };

  return (
    <div className="auth-container">
      <h2>{isRegister ? 'Create Account' : 'Welcome Back'}</h2>
      <form onSubmit={handleSubmit} className="auth-form">
        <input 
          type="email" 
          placeholder="Email" 
          value={email} 
          onChange={(e) => setEmail(e.target.value)} 
          required 
        />
        <input 
          type="password" 
          placeholder="Password" 
          value={password} 
          onChange={(e) => setPassword(e.target.value)} 
          required 
        />
        <button type="submit" className="primary-btn">
          {isRegister ? 'Register' : 'Login'}
        </button>
      </form>
      <p onClick={() => setIsRegister(!isRegister)} className="toggle-auth">
        {isRegister ? 'Already have an account? Login' : "Don't have an account? Register"}
      </p>
    </div>
  );
};

export default Login;
