import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import gsap from 'gsap';
import { useGSAP } from '@gsap/react';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import api from '../api';

// Fix for default marker icon in react-leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

// Icon riêng cho vị trí hiện tại (màu khác để phân biệt)
const UserLocationIcon = L.divIcon({
  className: 'user-location-marker',
  html: '<div style="background:#4285F4;width:16px;height:16px;border-radius:50%;border:3px solid white;box-shadow:0 0 4px rgba(0,0,0,0.4);"></div>',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
});

interface Destination {
  id: string;
  name: string;
  description: string;
  country: string;
  latitude: number;
  longitude: number;
  popularityScore: number;
}

// Component phụ để re-center map khi có toạ độ mới
const RecenterMap: React.FC<{ position: [number, number] }> = ({ position }) => {
  const map = useMap();
  useEffect(() => {
    map.setView(position, 12);
  }, [position, map]);
  return null;
};

const Destinations: React.FC = () => {
  const [destinations, setDestinations] = useState<Destination[]>([]);
  const [loading, setLoading] = useState(true);
  const [userPosition, setUserPosition] = useState<[number, number] | null>(null);
  const [locationError, setLocationError] = useState('');
  const [locating, setLocating] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useGSAP(() => {
    if (destinations.length > 0) {
      gsap.fromTo(".destination-card",
        { y: 30, opacity: 0 },
        { y: 0, opacity: 1, duration: 0.5, stagger: 0.1, ease: "power2.out" }
      );
    }
  }, { scope: containerRef, dependencies: [destinations] });

  useEffect(() => {
    fetchDestinations();
  }, []);

  const handleGetCurrentLocation = () => {
    if (!navigator.geolocation) {
      setLocationError('Trình duyệt của bạn không hỗ trợ định vị.');
      return;
    }

    setLocating(true);
    setLocationError('');

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        setUserPosition([latitude, longitude]);
        setLocating(false);
      },
      (err) => {
        setLocating(false);
        switch (err.code) {
          case err.PERMISSION_DENIED:
            setLocationError('Bạn đã từ chối quyền truy cập vị trí.');
            break;
          case err.POSITION_UNAVAILABLE:
            setLocationError('Không thể xác định vị trí hiện tại.');
            break;
          case err.TIMEOUT:
            setLocationError('Yêu cầu định vị đã hết thời gian.');
            break;
          default:
            setLocationError('Có lỗi xảy ra khi lấy vị trí.');
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0,
      }
    );
  };

  const fetchDestinations = async () => {
    try {
      const maxBudget = localStorage.getItem('maxBudget') || '2000000';
      const travelMonth = localStorage.getItem('travelMonth') || '6';

      // 1. Get standard recommendations
      const res = await api.get('/destinations/recommend', {
        params: { maxBudgetPerDay: maxBudget, travelMonth }
      });
      
      // 2. Mix with collaborative recommendations
      const collabRes = await api.get('/destinations/collaborative-recommend').catch(() => ({ data: { data: [] } }));
      
      const combined = [...res.data.data];
      
      // Add collaborative items if not already present
      collabRes.data.data.forEach((d: Destination) => {
        if (!combined.find(x => x.id === d.id)) {
          combined.push(d);
        }
      });
      
      setDestinations(combined);
      setLoading(false);
    } catch (e) {
      console.error(e);
      setLoading(false);
    }
  };

  const handleInteraction = async (dest: Destination, type: string) => {
    try {
      await api.post('/interactions', { destinationId: dest.id, interactionType: type });
      if (type === 'SELECT') {
          const selected = JSON.parse(localStorage.getItem('selectedDestinations') || '[]');
          if (!selected.find((x: any) => x.id === dest.id)) {
              selected.push(dest);
              localStorage.setItem('selectedDestinations', JSON.stringify(selected));
          }
      }
      alert(`${type} recorded! Popularity score will update soon.`);
    } catch (e) {
      console.error(e);
    }
  };

  if (loading) return <div className="loader">Loading recommendations...</div>;

  return (
    <div className="destinations-container" ref={containerRef}>
      <div className="list-panel">
        <h2>Recommended for you</h2>
        <div className="destination-list">
          {destinations.map(d => (
            <div key={d.id} className="destination-card">
              <h3>{d.name}</h3>
              <p>{d.description}</p>
              <div className="card-actions">
                <button onClick={() => handleInteraction(d, 'SAVE')}>Save</button>
                <button onClick={() => handleInteraction(d, 'SELECT')} className="primary-btn">Select for Trip</button>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="map-panel" style={{ position: 'relative' }}>
        <button
          onClick={handleGetCurrentLocation}
          disabled={locating}
          style={{
            position: 'absolute',
            top: 10,
            right: 10,
            zIndex: 1000,
            padding: '8px 12px',
            background: '#fff',
            border: '1px solid #ccc',
            borderRadius: 6,
            cursor: locating ? 'not-allowed' : 'pointer',
            boxShadow: '0 1px 4px rgba(0,0,0,0.2)',
          }}
        >
          {locating ? 'Đang định vị...' : '📍 Vị trí của tôi'}
        </button>

        {locationError && (
          <div
            style={{
              position: 'absolute',
              top: 50,
              right: 10,
              zIndex: 1000,
              background: '#fff3f3',
              color: '#d32f2f',
              padding: '6px 10px',
              borderRadius: 6,
              fontSize: 13,
              maxWidth: 220,
            }}
          >
            {locationError}
          </div>
        )}

        <MapContainer 
          center={[16.0, 106.0]} 
          zoom={5} 
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />

          {userPosition && (
            <>
              <RecenterMap position={userPosition} />
              <Marker position={userPosition} icon={UserLocationIcon}>
                <Popup>Vị trí hiện tại của bạn</Popup>
              </Marker>
            </>
          )}

          {destinations.map(dest => (
            dest.latitude && dest.longitude ? (
              <Marker key={dest.id} position={[dest.latitude, dest.longitude]}>
                <Popup>
                  <h3>{dest.name}</h3>
                  <p>Score: {dest.popularityScore.toFixed(1)}</p>
                </Popup>
              </Marker>
            ) : null
          ))}
        </MapContainer>
      </div>
    </div>
  );
};

export default Destinations;