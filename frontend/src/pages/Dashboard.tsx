import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { planningApi } from '../api';
import type { TripResponse } from '../api';

export default function Dashboard() {
  const [trips, setTrips] = useState<TripResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    planningApi.getMyTrips()
      .then(res => setTrips(res.data.data || []))
      .catch(() => setTrips([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="dashboard-container">
      <h2>Xin chào</h2>
      <div className="dashboard-grid">
        <div className="dashboard-card glass-panel">
          <h3>Tạo kế hoạch nhanh</h3>
          <p>Bắt đầu tạo lịch trình mới với vài thao tác đơn giản.</p>
          <Link to="/plan/new" className="primary-btn mt-4">Lên kế hoạch</Link>
        </div>

        <div className="dashboard-card glass-panel">
          <h3>Chuyến đi của bạn</h3>
          {loading ? (
            <div>Đang tải...</div>
          ) : trips.length === 0 ? (
            <div>
              <p>Bạn chưa có chuyến đi nào.</p>
              <Link to="/plan/new" className="primary-btn mt-2">Tạo chuyến đi đầu tiên</Link>
            </div>
          ) : (
            <div className="trips-list">
              {trips.slice(0,6).map(t => (
                <Link key={t.id} to={`/trip/${t.id}`} className="trip-mini">
                  <div>{t.title}</div>
                  <div className="muted">📅 {t.tripDate} • {t.groupSize} người</div>
                </Link>
              ))}
              {trips.length > 6 && <Link to="/trips" className="muted">Xem tất cả</Link>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
