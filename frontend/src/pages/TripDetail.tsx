import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { planningApi, messagingApi } from '../api';
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
  const [comments, setComments] = useState<any[]>([]);
  const [newComment, setNewComment] = useState('');
  const [reactions, setReactions] = useState<Record<string, any[]>>({});
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      planningApi.getTrip(id)
        .then(res => {
          setTrip(res.data.data);
          setLoading(false);
          // load comments
          messagingApi.listComments(id).then(cres => setComments(cres.data.data || [])).catch(() => {});
        })
        .catch(() => {
          setError('Không tìm thấy chuyến đi hoặc bạn không có quyền xem.');
          setLoading(false);
        });
    }
  }, [id, navigate]);

  const postComment = async () => {
    if (!id || !newComment.trim()) return;
    try {
      const res = await messagingApi.addComment(id, newComment.trim());
      setComments((s) => [...s, res.data.data]);
      setNewComment('');
    } catch {
      alert('Không thể thêm bình luận');
    }
  };

  const loadReactions = async (commentId: string) => {
    try {
      const res = await messagingApi.listReactions('COMMENT', commentId);
      setReactions(r => ({...r, [commentId]: res.data.data || []}));
    } catch {
      // Ignore reaction loading failures.
    }
  };

  const reactToComment = async (commentId: string, type: string) => {
    try {
      await messagingApi.react('COMMENT', commentId, type);
      await loadReactions(commentId);
    } catch {
      // Ignore reaction submission failures.
    }
  };

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
    } catch {
      alert('Lỗi xuất PDF');
    }
  };
  
  if (loading) return <div className="loading-screen">Đang tải lịch trình...</div>;
  if (error) return (
    <div className="error-screen">
      <div className="glass-panel" style={{padding:20}}>
        <h3>Lỗi</h3>
        <p>{error}</p>
        <div style={{marginTop:12}}>
          <button className="glass-btn" onClick={() => navigate('/trips')}>Quay lại Chuyến đi của tôi</button>
        </div>
      </div>
    </div>
  );
  if (!trip) return (
    <div className="empty-screen glass-panel">
      <p>Không tìm thấy chuyến đi này.</p>
      <button className="glass-btn" onClick={() => navigate('/trips')}>Quay lại</button>
    </div>
  );

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
              <div className="trip-summary glass-panel mt-4">
                <h3>Bình luận</h3>
                <div>
                  <textarea className="glass-input" rows={3} value={newComment} onChange={e => setNewComment(e.target.value)} />
                  <div style={{marginTop:8}}>
                    <button className="primary-btn" onClick={postComment}>Gửi bình luận</button>
                  </div>
                </div>
                <div style={{marginTop:12}}>
                  {comments.map(c => (
                    <div key={c.id} className="user-item" style={{marginBottom:8}}>
                      <div>
                        <div style={{fontWeight:600}}>{c.authorEmail}</div>
                        <div style={{color:'#cbd5e1'}}>{new Date(c.createdAt).toLocaleString()}</div>
                        <div style={{marginTop:6}}>{c.content}</div>
                        <div style={{marginTop:8, display:'flex', gap:8}}>
                          <button className="glass-btn" onClick={() => reactToComment(c.id, 'LIKE')}>👍</button>
                          <button className="glass-btn" onClick={() => reactToComment(c.id, 'LOVE')}>❤️</button>
                          <button className="glass-btn" onClick={() => reactToComment(c.id, 'LAUGH')}>😂</button>
                          <button className="glass-btn" onClick={() => loadReactions(c.id)}>Xem phản ứng</button>
                        </div>
                        <div style={{marginTop:6}}>
                          {(reactions[c.id] || []).map(r => (
                            <span key={r.id} style={{marginRight:8, fontSize:12}}>{r.authorEmail}:{r.type}</span>
                          ))}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
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
