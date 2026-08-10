import { useState } from 'react';
import { DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { arrayMove, SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import api from '../api';
import TripMap from '../components/TripMap';

interface Waypoint {
  id: string; // Sortable needs unique id
  destinationId: string;
  name: string;
  latitude: number;
  longitude: number;
}

const loadInitialWaypoints = (): Waypoint[] => {
  try {
    const selected = JSON.parse(localStorage.getItem('selectedDestinations') || '[]');
    if (selected && selected.length > 0) {
      return selected.map((d: any, index: number) => ({
        id: `wp-${index}-${d.id}`,
        destinationId: d.id,
        name: d.name,
        latitude: d.latitude,
        longitude: d.longitude
      }));
    }
  } catch {
    // Ignore invalid JSON stored in localStorage and fall back to defaults.
  }

  return [
    { id: '1', destinationId: '1', name: 'Bãi biển Mỹ Khê', latitude: 16.0610, longitude: 108.2470 },
    { id: '2', destinationId: '2', name: 'Cầu Rồng', latitude: 16.0613, longitude: 108.2263 },
    { id: '3', destinationId: '3', name: 'Chợ Hàn Đà Nẵng', latitude: 16.0680, longitude: 108.2230 },
  ];
};

const SortableItem = ({ id, wp }: { id: string; wp: Waypoint }) => {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id });
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners} className="sortable-item">
      📍 {wp.name}
    </div>
  );
};

const Planner: React.FC = () => {
  const [items, setItems] = useState<Waypoint[]>(loadInitialWaypoints());
  const [optimizing, setOptimizing] = useState(false);
  const [stats, setStats] = useState({ distance: 0, duration: 0 });

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
  );

  const handleDragEnd = (event: any) => {
    const { active, over } = event;
    if (active.id !== over.id) {
      setItems((items) => {
        const oldIndex = items.findIndex(i => i.id === active.id);
        const newIndex = items.findIndex(i => i.id === over.id);
        return arrayMove(items, oldIndex, newIndex);
      });
    }
  };

  const handleOptimize = async () => {
    setOptimizing(true);
    try {
      const payload = items.map((wp, idx) => ({
        destinationId: wp.destinationId,
        name: wp.name,
        latitude: wp.latitude,
        longitude: wp.longitude,
        originalOrder: idx
      }));

      const res = await api.post('/routes/optimize', payload);
      const optimizedOrder = res.data.data.optimizedOrder;
      
      // Update the local state based on the new optimized order
      const newItems = optimizedOrder.map((o: any) => ({
        id: items.find(i => i.destinationId === o.destinationId)?.id || o.destinationId,
        destinationId: o.destinationId,
        name: o.name,
        latitude: o.latitude,
        longitude: o.longitude
      }));
      
      setItems(newItems);
      setStats({
        distance: res.data.data.totalDistanceKm,
        duration: res.data.data.totalDurationMinutes
      });
    } catch {
      alert('Failed to optimize route');
    }
    setOptimizing(false);
  };

  return (
    <div className="flex flex-col lg:flex-row h-[calc(100vh-74px)] w-full">
      {/* MAP SECTION */}
      <div className="w-full h-[40vh] lg:h-full lg:flex-1 relative z-[1]">
        <TripMap 
          places={items.map(i => ({ 
            name: i.name, 
            latitude: i.latitude, 
            longitude: i.longitude 
          }))}
          showPolyline={items.length > 1}
        />
      </div>

      {/* PLANNER SECTION */}
      <div className="w-full lg:w-[450px] bg-[var(--color-bg)] border-t lg:border-t-0 lg:border-l border-[var(--border-light)] flex flex-col z-[2] overflow-hidden">
        <div className="p-6 overflow-y-auto h-full">
          <h2>Day 1 Itinerary</h2>
          <div className="planner-actions mb-4">
            <button onClick={handleOptimize} disabled={optimizing} className="primary-btn w-full">
              {optimizing ? 'Optimizing via ORS...' : '✨ Optimize Route Order'}
            </button>
          </div>
          
          {stats.distance > 0 && (
            <div className="route-stats mb-4">
              Estimated Distance: {stats.distance.toFixed(1)} km | Duration: {Math.round(stats.duration)} mins
            </div>
          )}

          <div className="dnd-container">
            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
              <SortableContext items={items.map(i => i.id)} strategy={verticalListSortingStrategy}>
                {items.map(wp => (
                  <SortableItem key={wp.id} id={wp.id} wp={wp} />
                ))}
              </SortableContext>
            </DndContext>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Planner;
