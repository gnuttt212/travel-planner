import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';
import { useGSAP } from '@gsap/react';
import { planningApi } from '../api';
import type { TravelContextRequest } from '../api';

const PURPOSES = [
  { id: 'RELAX', label: 'Nghỉ dưỡng', icon: '🏖️' },
  { id: 'FOOD', label: 'Ăn uống khám phá', icon: '🍜' },
  { id: 'GROUP_FUN', label: 'Vui chơi nhóm', icon: '🎉' },
  { id: 'NATURE', label: 'Thiên nhiên', icon: '🌿' },
  { id: 'CHECKIN', label: 'Check-in sống ảo', icon: '📸' },
  { id: 'DATING', label: 'Hẹn hò', icon: '💖' },
];

export default function ContextCards() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(false);

  const [formData, setFormData] = useState<Partial<TravelContextRequest>>({
    purpose: '',
    tripDate: new Date().toISOString().split('T')[0],
    duration: 'FULL_DAY',
    groupType: 'FRIENDS',
    groupSize: 2,
    budgetPerPerson: 500000,
    styles: [],
    startLat: 16.0544,
    startLon: 108.2022,
    transportation: 'MOTORBIKE',
    city: 'Đà Nẵng'
  });

  const updateForm = (key: keyof TravelContextRequest, value: any) => {
    setFormData(prev => ({ ...prev, [key]: value }));
  };

  const { contextSafe } = useGSAP({ scope: containerRef });

  const nextStep = contextSafe(() => {
    if (step < 4) {
      gsap.to('.step-content', {
        x: '-20px',
        opacity: 0,
        duration: 0.2,
        onComplete: () => {
          setStep(prev => prev + 1);
          gsap.fromTo('.step-content', 
            { x: '20px', opacity: 0 }, 
            { x: '0px', opacity: 1, duration: 0.3 }
          );
        }
      });
    } else {
      handleSubmit();
    }
  });

  const prevStep = contextSafe(() => {
    if (step > 0) {
      gsap.to('.step-content', {
        x: '20px',
        opacity: 0,
        duration: 0.2,
        onComplete: () => {
          setStep(prev => prev - 1);
          gsap.fromTo('.step-content', 
            { x: '-20px', opacity: 0 }, 
            { x: '0px', opacity: 1, duration: 0.3 }
          );
        }
      });
    }
  });

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const res = await planningApi.recommend(formData as TravelContextRequest);
      navigate('/plan/results', { state: { plans: res.data.data, context: formData } });
    } catch (e) {
      console.error(e);
      alert('Có lỗi xảy ra khi tạo kế hoạch');
      setLoading(false);
    }
  };

  const renderStep = () => {
    switch (step) {
      case 0:
        return (
          <div className="step-content">
            <h2>Mục đích chuyến đi của bạn?</h2>
            <div className="purpose-grid">
              {PURPOSES.map(p => (
                <div 
                  key={p.id} 
                  className={`purpose-card ${formData.purpose === p.id ? 'active' : ''}`}
                  onClick={() => updateForm('purpose', p.id)}
                >
                  <div className="purpose-icon">{p.icon}</div>
                  <div>{p.label}</div>
                </div>
              ))}
            </div>
          </div>
        );
      case 1:
        return (
          <div className="step-content">
            <h2>Thời gian chuyến đi</h2>
            <div className="input-group">
              <label>Ngày đi</label>
              <input 
                type="date" 
                value={formData.tripDate} 
                onChange={(e) => updateForm('tripDate', e.target.value)} 
                className="glass-input"
              />
            </div>
            <div className="duration-buttons">
              {['HALF_DAY', 'FULL_DAY', 'MULTI_DAY'].map(d => (
                <button 
                  key={d}
                  className={`glass-btn ${formData.duration === d ? 'active' : ''}`}
                  onClick={() => updateForm('duration', d)}
                >
                  {d === 'HALF_DAY' ? 'Nửa ngày' : d === 'FULL_DAY' ? 'Một ngày' : 'Nhiều ngày'}
                </button>
              ))}
            </div>
          </div>
        );
      case 2:
        return (
          <div className="step-content">
            <h2>Bạn đi cùng ai?</h2>
            <div className="group-grid">
              {['SOLO', 'COUPLE', 'FAMILY', 'FRIENDS'].map(g => (
                <button 
                  key={g}
                  className={`glass-btn ${formData.groupType === g ? 'active' : ''}`}
                  onClick={() => updateForm('groupType', g)}
                >
                  {g === 'SOLO' ? '👤 Mình tôi' : g === 'COUPLE' ? '👩‍❤️‍👨 Cặp đôi' : g === 'FAMILY' ? '👨‍👩‍👧‍👦 Gia đình' : '👯 Bạn bè'}
                </button>
              ))}
            </div>
            {formData.groupType !== 'SOLO' && (
              <div className="input-group size-group">
                <label>Số lượng người</label>
                <div className="counter">
                  <button onClick={() => updateForm('groupSize', Math.max(1, (formData.groupSize || 1) - 1))}>-</button>
                  <span>{formData.groupSize}</span>
                  <button onClick={() => updateForm('groupSize', (formData.groupSize || 1) + 1)}>+</button>
                </div>
              </div>
            )}
          </div>
        );
      case 3:
        return (
          <div className="step-content">
            <h2>Ngân sách dự kiến (VNĐ)</h2>
            <div className="budget-buttons">
              {[
                { label: 'Tiết kiệm ~200K', val: 200000 },
                { label: 'Thoải mái ~500K', val: 500000 },
                { label: 'Không giới hạn', val: 2000000 }
              ].map(b => (
                <button 
                  key={b.val}
                  className={`glass-btn ${formData.budgetPerPerson === b.val ? 'active' : ''}`}
                  onClick={() => updateForm('budgetPerPerson', b.val)}
                >
                  {b.label}
                </button>
              ))}
            </div>
            <div className="input-group mt-4">
              <label>Hoặc nhập số tiền (VNĐ/người)</label>
              <input 
                type="number" 
                value={formData.budgetPerPerson} 
                onChange={(e) => updateForm('budgetPerPerson', parseInt(e.target.value))} 
                className="glass-input"
              />
            </div>
          </div>
        );
      case 4:
        return (
          <div className="step-content">
            <h2>Địa điểm & Di chuyển</h2>
            <div className="input-group">
              <label>Thành phố</label>
              <div className="loc-input">
                <input 
                  type="text" 
                  value={formData.city} 
                  onChange={(e) => updateForm('city', e.target.value)} 
                  className="glass-input"
                  placeholder="Ví dụ: Đà Nẵng, Hà Nội..."
                />
                <button 
                  className="glass-btn icon-btn" 
                  onClick={() => {
                    navigator.geolocation.getCurrentPosition(pos => {
                      updateForm('startLat', pos.coords.latitude);
                      updateForm('startLon', pos.coords.longitude);
                      alert('Đã lấy vị trí hiện tại');
                    });
                  }}
                  title="Lấy vị trí hiện tại"
                >📍</button>
              </div>
            </div>
            <label className="mt-4 block">Phương tiện</label>
            <div className="transport-buttons">
              {[
                { id: 'MOTORBIKE', label: '🏍️ Xe máy' },
                { id: 'CAR', label: '🚗 Ô tô' },
                { id: 'PUBLIC', label: '🚌 Phương tiện công cộng' }
              ].map(t => (
                <button 
                  key={t.id}
                  className={`glass-btn ${formData.transportation === t.id ? 'active' : ''}`}
                  onClick={() => updateForm('transportation', t.id)}
                >
                  {t.label}
                </button>
              ))}
            </div>
          </div>
        );
    }
  };

  return (
    <div className="context-cards-wrapper" ref={containerRef}>
      <div className="glass-panel main-card">
        {renderStep()}
        
        <div className="card-footer">
          <div className="progress-dots">
            {[0, 1, 2, 3, 4].map(i => (
              <div key={i} className={`dot ${step === i ? 'active' : ''} ${step > i ? 'completed' : ''}`}></div>
            ))}
          </div>
          
          <div className="actions">
            {step > 0 && (
              <button className="glass-btn" onClick={prevStep} disabled={loading}>Quay lại</button>
            )}
            <button className="primary-btn pulse-btn" onClick={nextStep} disabled={loading}>
              {loading ? 'Đang tải...' : step === 4 ? 'Bắt đầu lên lịch 🚀' : 'Tiếp tục'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
