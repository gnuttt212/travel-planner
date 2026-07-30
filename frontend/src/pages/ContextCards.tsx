import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import gsap from 'gsap';
import { useGSAP } from '@gsap/react';
import { planningApi } from '../api';
import type { TravelContextRequest } from '../api';
import LocationTransport from '../components/LocationTransport';

const PURPOSES = [
  { id: 'RELAXATION', label: 'Nghỉ dưỡng', icon: '🏖️' },
  { id: 'FOOD_EXPLORE', label: 'Ăn uống khám phá', icon: '🍜' },
  { id: 'GROUP_FUN', label: 'Vui chơi nhóm', icon: '🎉' },
  { id: 'NATURE', label: 'Thiên nhiên', icon: '🌿' },
  { id: 'PHOTOGRAPHY', label: 'Check-in sống ảo', icon: '📸' },
  { id: 'DATE', label: 'Hẹn hò', icon: '💖' },
];

export default function ContextCards() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(false);

  const [cities, setCities] = useState<string[]>([]);

  const [formData, setFormData] = useState<Partial<TravelContextRequest>>({
    purpose: undefined,
    tripDate: new Date().toISOString().split('T')[0],
    duration: 'FULL_DAY',
    groupType: 'FRIENDS',
    groupSize: 2,
    budgetPerPerson: 500000,
    styles: [],
    startLat: 16.0544,
    startLon: 108.2022,
    transportation: 'MOTORBIKE',
    city: undefined
  });

  useEffect(() => {
    planningApi.getCities()
      .then(res => {
        setCities(res.data.data);
      })
      .catch(e => {
        console.error('Không lấy được danh sách thành phố:', e);
        setCities([]);
      });
  }, []);

  // Small city center list for nearest-city mapping (lat, lon)
  const CITY_COORDS: { name: string; lat: number; lon: number }[] = [
    { name: 'Hồ Chí Minh', lat: 10.7769, lon: 106.7009 },
    { name: 'Hà Nội', lat: 21.0278, lon: 105.8342 },
    { name: 'Đà Nẵng', lat: 16.0544, lon: 108.2022 },
    { name: 'Huế', lat: 16.4637, lon: 107.5909 },
    { name: 'Nha Trang', lat: 12.2388, lon: 109.1967 },
    { name: 'Đà Lạt', lat: 11.9404, lon: 108.4583 },
    { name: 'Cần Thơ', lat: 10.0452, lon: 105.7469 },
  ];

  const toRad = (deg: number) => deg * Math.PI / 180;
  const haversineKm = (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const R = 6371; // km
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  };

  const findNearestCity = (lat: number, lon: number) => {
    let best = { name: '', d: Infinity };
    CITY_COORDS.forEach(c => {
      const d = haversineKm(lat, lon, c.lat, c.lon);
      if (d < best.d) best = { name: c.name, d };
    });
    // Only accept if within 120 km
    return best.d <= 120 ? best.name : null;
  };

  const updateForm = (key: keyof TravelContextRequest, value: any) => {
    setFormData(prev => ({ ...prev, [key]: value }));
  };

  const { contextSafe } = useGSAP({ scope: containerRef });

  const validateStep = (currentStep: number): string | null => {
    switch (currentStep) {
      case 0:
        if (!formData.purpose) return 'Vui lòng chọn mục đích chuyến đi';
        return null;
      case 1:
        if (!formData.tripDate) return 'Vui lòng chọn ngày đi';
        if (!formData.duration) return 'Vui lòng chọn thời lượng chuyến đi';
        return null;
      case 2:
        if (!formData.groupType) return 'Vui lòng chọn nhóm đi cùng';
        if (formData.groupType !== 'SOLO' && (!formData.groupSize || formData.groupSize < 1)) {
          return 'Số lượng người phải lớn hơn 0';
        }
        return null;
      case 3:
        if (!formData.budgetPerPerson || formData.budgetPerPerson <= 0) {
          return 'Vui lòng nhập ngân sách hợp lệ';
        }
        return null;
      case 4:
        if (!formData.city) return 'Vui lòng chọn thành phố';
        if (!formData.transportation) return 'Vui lòng chọn phương tiện di chuyển';
        return null;
      default:
        return null;
    }
  };

  const nextStep = contextSafe(() => {
    const error = validateStep(step);
    if (error) {
      alert(error);
      return;
    }

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
    for (let s = 0; s <= 4; s++) {
      const error = validateStep(s);
      if (error) {
        alert(error);
        return;
      }
    }

    setLoading(true);
    try {
      const res = await planningApi.recommend(formData as TravelContextRequest);
      navigate('/plan/results', { state: { plans: res.data.data, context: formData } });
    } catch (e: any) {
      console.error(e);
      const msg =
        e.response?.data?.message ||
        e.message ||
        'Có lỗi xảy ra khi tạo kế hoạch';
      alert(msg);
    } finally {
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
                onChange={(e) => updateForm('budgetPerPerson', parseInt(e.target.value) || 0)} 
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
              <LocationTransport
                cities={cities}
                city={formData.city as string | undefined}
                onCityChange={(c) => updateForm('city', c)}
                transportation={formData.transportation as any}
                onTransportationChange={(t) => updateForm('transportation', t)}
                onUseCurrentLocation={(lat, lon) => {
                  updateForm('startLat', lat);
                  updateForm('startLon', lon);
                  const nearest = findNearestCity(lat, lon);
                  if (nearest) updateForm('city', nearest);
                }}
              />
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
