import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';
import { useGSAP } from '@gsap/react';
import api from '../api';

interface LoginProps {
  onLoginSuccess: () => void;
}

const Login: React.FC<LoginProps> = ({ onLoginSuccess }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isRegister, setIsRegister] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  const containerRef = useRef<HTMLDivElement>(null);

  useGSAP(() => {
    gsap.from(containerRef.current, {
      y: 50,
      opacity: 0,
      duration: 0.8,
      ease: "power3.out"
    });
  }, { scope: containerRef });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    try {
      if (isRegister) {
        await api.post('/auth/register', { email, password, role: 'USER' });
        alert('Registration successful. Please log in.');
        setIsRegister(false);
      } else {
        const res = await api.post('/auth/login', { email, password });
        localStorage.setItem('token', res.data.data.token);
        onLoginSuccess();
        navigate('/destinations');
      }
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.message ||
        'Authentication failed.';
      setErrorMessage(message);
    }
  };

  return (
    <div className="auth-container" ref={containerRef}>
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
        {errorMessage && <p className="auth-error">{errorMessage}</p>}
        <button type="submit" className="primary-btn">
          {isRegister ? 'Register' : 'Login'}
        </button>
      </form>
      <p
        onClick={() => {
          setIsRegister(!isRegister);
          setErrorMessage('');
        }}
        className="toggle-auth"
      >
        {isRegister ? 'Already have an account? Login' : "Don't have an account? Register"}
      </p>
    </div>
  );
};

export default Login;