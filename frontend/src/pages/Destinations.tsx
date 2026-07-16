import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
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

interface Destination {
  id: string;
  name: string;
  description: string;
  country: string;
  latitude: number;
  longitude: number;
  popularityScore: number;
}

const Destinations: React.FC = () => {
  const [destinations, setDestinations] = useState<Destination[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDestinations();
  }, []);

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
    <div className="destinations-container">
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
        <MapContainer 
          center={[16.0, 106.0]} 
          zoom={5} 
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          />
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
