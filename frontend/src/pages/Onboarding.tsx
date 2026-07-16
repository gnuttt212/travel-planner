import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api';

const Onboarding: React.FC = () => {
  const [step, setStep] = useState(1);
  const [tags, setTags] = useState<string[]>([]);
  const [minBudget, setMinBudget] = useState(500000);
  const [maxBudget, setMaxBudget] = useState(2000000);
  const [travelStyle, setTravelStyle] = useState('Relaxing');
  const [groupType, setGroupType] = useState('Solo');
  const navigate = useNavigate();

  const handleTagToggle = (tag: string) => {
    setTags(prev => prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag]);
  };

  const handleSubmit = async () => {
    try {
      await api.post('/onboarding', {
        preferredTags: tags,
        minBudget,
        maxBudget,
        travelStyle,
        groupType
      });
      localStorage.setItem('maxBudget', maxBudget.toString());
      localStorage.setItem('travelMonth', (new Date().getMonth() + 1).toString());
      navigate('/destinations');
    } catch (e) {
      alert('Failed to save preferences');
    }
  };

  return (
    <div className="onboarding-container">
      <div className="step-indicator">Step {step} of 4</div>
      
      {step === 1 && (
        <div className="onboarding-step">
          <h2>What kind of experiences do you like?</h2>
          <div className="tag-grid">
            {['biển', 'núi', 'thiên-nhiên', 'văn-hóa', 'lịch-sử', 'ẩm-thực', 'giải-trí', 'thư-giãn'].map(tag => (
              <button 
                key={tag} 
                className={`tag-btn ${tags.includes(tag) ? 'selected' : ''}`}
                onClick={() => handleTagToggle(tag)}
              >
                {tag}
              </button>
            ))}
          </div>
          <button onClick={() => setStep(2)} className="next-btn">Next</button>
        </div>
      )}

      {step === 2 && (
        <div className="onboarding-step">
          <h2>What is your daily budget? (VND)</h2>
          <div className="budget-inputs">
            <input type="number" value={minBudget} onChange={e => setMinBudget(Number(e.target.value))} placeholder="Min" />
            <span> - </span>
            <input type="number" value={maxBudget} onChange={e => setMaxBudget(Number(e.target.value))} placeholder="Max" />
          </div>
          <button onClick={() => setStep(3)} className="next-btn">Next</button>
        </div>
      )}

      {step === 3 && (
        <div className="onboarding-step">
          <h2>Travel Style</h2>
          <select value={travelStyle} onChange={e => setTravelStyle(e.target.value)} className="select-input">
            <option value="Relaxing">Relaxing</option>
            <option value="Adventure">Adventure</option>
            <option value="Cultural">Cultural</option>
            <option value="Party">Party</option>
          </select>
          <button onClick={() => setStep(4)} className="next-btn">Next</button>
        </div>
      )}

      {step === 4 && (
        <div className="onboarding-step">
          <h2>Who are you traveling with?</h2>
          <select value={groupType} onChange={e => setGroupType(e.target.value)} className="select-input">
            <option value="Solo">Solo</option>
            <option value="Couple">Couple</option>
            <option value="Family">Family</option>
            <option value="Friends">Friends</option>
          </select>
          <button onClick={handleSubmit} className="next-btn submit-btn">Finish</button>
        </div>
      )}
    </div>
  );
};

export default Onboarding;
