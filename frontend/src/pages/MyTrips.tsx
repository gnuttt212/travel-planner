import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { planningApi } from '../api';
import type { TripResponse } from '../api';

export default function MyTrips() {
  const [trips, setTrips] = useState<TripResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    planningApi.getMyTrips()
      .then(res => {
        setTrips(res.data.data);
        setLoading(false);
      })
      .catch(e => {
        console.error(e);
        setLoading(false);
      });
  }, []);

  const handleDelete = async (id: string, e: React.MouseEvent) => {
    e.preventDefault();
    if (confirm('Bạn có chắc chắn muốn xóa chuyến đi này?')) {
      try {
        await planningApi.deleteTrip(id);
        setTrips(trips.filter(t => t.id !== id));
      } catch (e) {
        alert('Lỗi xóa chuyến đi');
      }
    }
  };

  if (loading) return <div className="loading-screen">Đang tải...</div>;

  return (
    <div className="my-trips-container">
      <h2>Chuyến đi của tôi</h2>
      {trips.length === 0 ? (
        <div className="empty-state">
          <p>Bạn chưa có chuyến đi nào.</p>
          <Link to="/plan/new" className="primary-btn mt-4 inline-block">Lên kế hoạch ngay</Link>
        </div>
      ) : (
        <div className="trips-grid">
          {trips.map(trip => (
            <Link to={`/trip/${trip.id}`} key={trip.id} className="trip-card glass-panel">
              <h3>{trip.title}</h3>
              <div className="trip-info">
                <span>📅 {trip.tripDate}</span>
                <span>🎯 {trip.purpose}</span>
                <span>👥 {trip.groupSize}</span>
              </div>
              <button className="delete-btn" onClick={(e) => handleDelete(trip.id, e)}>🗑️</button>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
