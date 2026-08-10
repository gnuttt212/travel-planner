import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { planningApi } from '../api';
import type { TripPlanResponse, TravelContextRequest } from '../api';
import TripMap from '../components/TripMap';
export default function Results() {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as { plans?: TripPlanResponse[], context?: TravelContextRequest };
  const [plans] = useState<TripPlanResponse[]>(state?.plans || []);
  const [selectedIdx, setSelectedIdx] = useState(0);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    if (!state?.plans || state.plans.length === 0) {
      navigate('/plan/new');
    }
  }, [state, navigate]);

  if (!plans.length) return null;

  const currentPlan = plans[selectedIdx];
  const places = currentPlan.activities.map(act => ({
    name: act.destinationName,
    latitude: act.destinationLat,
    longitude: act.destinationLon,
    plannedStartTime: act.plannedStartTime,
    plannedEndTime: act.plannedEndTime,
    estimatedCost: act.estimatedCost
  }));

  const handleSelectPlan = async () => {
    if (!state?.context) {
      alert('Dữ liệu chuyến đi không hợp lệ. Vui lòng tạo lại kế hoạch.');
      return;
    }
    setCreating(true);
    try {
      const payload = {
        ...state.context,
        variantName: currentPlan.variantName,
      };
      const res = await planningApi.createTrip(payload);
      navigate(`/trip/${res.data.data.id}`);
    } catch (e: any) {
      console.error(e);
      const message = e.response?.data?.message || e.message || 'Không thể tạo chuyến đi.';
      alert(message);
      setCreating(false);
    }
  };

  return (
    <div className="results-container">
      <div className="map-half">
        <TripMap 
          places={places} 
          showPolyline={places.length > 1}
          popupRender={(place) => (
            <>
              <strong>{place.name}</strong><br/>
              {place.plannedStartTime} - {place.plannedEndTime}<br/>
              {place.estimatedCost.toLocaleString()}đ
            </>
          )}
        />
      </div>
      
      <div className="variants-half">
        <div className="variants-header">
          <h2>Đề xuất lịch trình</h2>
          <p>Chọn một trong các phương án được cá nhân hóa cho bạn</p>
        </div>
        
        <div className="variants-scroll">
          {plans.map((plan, idx) => (
            <div 
              key={idx} 
              className={`variant-card glass-panel ${idx === selectedIdx ? 'selected' : ''}`}
              onClick={() => setSelectedIdx(idx)}
            >
              <h3>{plan.variantName}</h3>
              <p className="variant-desc">{plan.variantDescription}</p>
              {plan.aiNarrative && (
                <div className="ai-narrative" style={{ marginTop: '10px', fontSize: '0.9rem', color: '#e0e7ff', borderLeft: '3px solid #8b5cf6', paddingLeft: '10px', fontStyle: 'italic' }}>
                  ✨ {plan.aiNarrative}
                </div>
              )}
              <div className="variant-stats" style={{ marginTop: '15px' }}>
                <div className="stat"><span>💰</span> {plan.totalCost.toLocaleString()}đ</div>
                <div className="stat"><span>📍</span> {plan.activities.length} điểm</div>
                <div className="stat"><span>🚗</span> {plan.totalDistanceKm.toFixed(1)} km</div>
              </div>
            </div>
          ))}
        </div>
        
        <div className="variants-footer">
          <button className="primary-btn full-width" onClick={handleSelectPlan} disabled={creating}>
            {creating ? 'Đang tạo chuyến đi...' : '✨ Chọn lịch trình này'}
          </button>
        </div>
      </div>
    </div>
  );
}
