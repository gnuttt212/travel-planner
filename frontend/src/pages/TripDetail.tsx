import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { planningApi } from '../api';
import type { TripResponse } from '../api';

const getCategoryIcon = (cat: string) => {
  const lower = cat.toLowerCase();
  if (lower.includes('cafe')) return '☕';
  if (lower.includes('restaurant') || lower.includes('food')) return '🍜';
  if (lower.includes('park') || lower.includes('nature')) return '🌿';
  if (lower.includes('museum') || lower.includes('culture')) return '🏛️';
  if (lower.includes('hotel') || lower.includes('stay')) return '🏨';
  return '📍';
};

export default function TripDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [trip, setTrip] = useState<TripResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      planningApi.getTrip(id)
        .then(res => {
          setTrip(res.data.data);
          setLoading(false);
        })
        .catch(e => {
          console.error(e);
          alert('Không tìm thấy chuyến đi');
          navigate('/');
        });
    }
  }, [id, navigate]);

  const handleExport = async () => {
    if (!id) return;
    try {
      const res = await planningApi.exportPdf(id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `trip-${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (e) {
      alert('Lỗi xuất PDF');
    }
  };

  if (loading) return <div className="loading-screen">Đang tải lịch trình...</div>;
  if (!trip) return null;

  const totalCost = trip.activities.reduce((sum, a) => sum + a.estimatedCost, 0);
  const totalDistance = trip.activities.reduce((sum, a) => sum + a.travelDistanceKm, 0);
  const totalTime = trip.activities.reduce((sum, a) => sum + a.estimatedDurationMinutes, 0);

  return (
    <div className="trip-detail-container">
      <div className="trip-header glass-panel">
        <button className="back-btn glass-btn" onClick={() => navigate('/trips')}>← Chuyến đi của tôi</button>
        <h1>{trip.title}</h1>
        <div className="trip-meta">
          <span>📅 {trip.tripDate}</span>
          <span>👥 {trip.groupSize} người</span>
          <span>🎯 {trip.purpose}</span>
        </div>
        <button className="export-btn primary-btn" onClick={handleExport}>📥 Xuất PDF</button>
      </div>

      <div className="timeline-container">
        <div className="timeline-line"></div>
        {trip.activities.map((act, idx) => (
          <div key={act.id} className="timeline-item">
            <div className="timeline-icon">{getCategoryIcon(act.destinationCategory)}</div>
            <div className="timeline-content glass-panel">
              <div className="time-badge">{act.plannedStartTime} - {act.plannedEndTime}</div>
              <h3>{act.destinationName}</h3>
              <div className="act-details">
                <span className="rating">⭐ {act.destinationRating}</span>
                <span>⏱️ {act.estimatedDurationMinutes} phút</span>
                <span>💰 {act.estimatedCost.toLocaleString()}đ</span>
              </div>
              {idx < trip.activities.length - 1 && trip.activities[idx+1].travelTimeMinutes > 0 && (
                <div className="travel-info">
                  ↓ Di chuyển {trip.activities[idx+1].travelTimeMinutes} phút ({trip.activities[idx+1].travelDistanceKm.toFixed(1)}km)
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      <div className="trip-summary glass-panel">
        <h3>Tổng kết chuyến đi</h3>
        <div className="summary-stats">
          <div className="stat">
            <div className="stat-label">Tổng chi phí</div>
            <div className="stat-val">{totalCost.toLocaleString()}đ</div>
          </div>
          <div className="stat">
            <div className="stat-label">Quãng đường</div>
            <div className="stat-val">{totalDistance.toFixed(1)} km</div>
          </div>
          <div className="stat">
            <div className="stat-label">Thời gian HĐ</div>
            <div className="stat-val">{Math.round(totalTime / 60)} giờ {totalTime % 60} phút</div>
          </div>
        </div>
      </div>
    </div>
  );
}
