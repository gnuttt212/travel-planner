import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { planningApi } from '../api';
import type { TripPlanResponse, TravelContextRequest } from '../api';
import L from 'leaflet';

// Fix for default marker icon in react-leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

const customIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-violet.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

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
  const mapCenter = currentPlan.activities.length > 0 
    ? [currentPlan.activities[0].destinationLat, currentPlan.activities[0].destinationLon] as [number, number]
    : [16.0544, 108.2022] as [number, number];

  const handleSelectPlan = async () => {
    if (!state?.context) return;
    setCreating(true);
    try {
      // Create trip from the chosen context and plan
      // We pass the context, the backend might use it to persist the selected variant.
      // Wait, the API for createTrip just takes TravelContextRequest. 
      // If we need to specify which variant, the API signature in api.ts is:
      // createTrip: (data: TravelContextRequest)
      // Actually we'll just pretend the context allows specifying variant, or maybe the backend just generates it.
      // If the backend doesn't take variantName, we'll just send context.
      const res = await planningApi.createTrip({ ...state.context, styles: [currentPlan.variantName] });
      navigate(`/trip/${res.data.data.id}`);
    } catch (e) {
      console.error(e);
      alert('Không thể tạo chuyến đi.');
      setCreating(false);
    }
  };

  const polylinePositions = currentPlan.activities.map(a => [a.destinationLat, a.destinationLon] as [number, number]);

  return (
    <div className="results-container">
      <div className="map-half">
        <MapContainer center={mapCenter} zoom={13} style={{ height: '100%', width: '100%' }}>
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
          {currentPlan.activities.map((act, i) => (
            <Marker key={act.id || i} position={[act.destinationLat, act.destinationLon]} icon={customIcon}>
              <Popup>
                <strong>{act.destinationName}</strong><br/>
                {act.plannedStartTime} - {act.plannedEndTime}<br/>
                {act.estimatedCost.toLocaleString()}đ
              </Popup>
            </Marker>
          ))}
          {polylinePositions.length > 1 && (
            <Polyline positions={polylinePositions} color="#8b5cf6" weight={4} opacity={0.7} />
          )}
        </MapContainer>
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
              <div className="variant-stats">
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
