import React, { useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

// Fix for default marker icon in react-leaflet
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

export const defaultMarkerIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-violet.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

export type TripMapPlace = {
  name: string;
  latitude: number;
  longitude: number;
  // Allows extending with any other properties (like plannedStartTime) without breaking the generic map
  [key: string]: any;
};

type TripMapProps = {
  places: TripMapPlace[];
  showPolyline?: boolean;
  popupRender?: (place: TripMapPlace, index: number) => React.ReactNode;
  className?: string;
};

// Component to automatically fit bounds to places
const AutoFitBound = ({ places }: { places: TripMapPlace[] }) => {
  const map = useMap();

  useEffect(() => {
    if (!places || places.length === 0) return;

    if (places.length === 1) {
      map.setView([places[0].latitude, places[0].longitude], 13);
      return;
    }

    const bounds = L.latLngBounds(places.map(p => [p.latitude, p.longitude]));
    map.fitBounds(bounds, { padding: [50, 50] });
  }, [places, map]);

  return null;
};

export default function TripMap({ places, showPolyline = false, popupRender, className = "h-full w-full" }: TripMapProps) {
  const mapCenter = places.length > 0 
    ? [places[0].latitude, places[0].longitude] as [number, number]
    : [16.0544, 108.2022] as [number, number];

  const polylinePositions = places.map(p => [p.latitude, p.longitude] as [number, number]);

  return (
    <div className={className} style={{ minHeight: '300px' }}>
      <MapContainer center={mapCenter} zoom={13} style={{ height: '100%', width: '100%', borderRadius: 'inherit' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        
        {places.map((place, idx) => (
          <Marker 
            key={`${place.latitude}-${place.longitude}-${idx}`} 
            position={[place.latitude, place.longitude]} 
            icon={defaultMarkerIcon}
          >
            <Popup>
              {popupRender ? popupRender(place, idx) : <strong>{place.name}</strong>}
            </Popup>
          </Marker>
        ))}

        {showPolyline && polylinePositions.length > 1 && (
          <Polyline positions={polylinePositions} color="#8b5cf6" weight={4} opacity={0.7} />
        )}

        <AutoFitBound places={places} />
      </MapContainer>
    </div>
  );
}
