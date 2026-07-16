import React, { useState } from 'react';
import { DndContext, closestCenter, KeyboardSensor, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import { arrayMove, SortableContext, sortableKeyboardCoordinates, verticalListSortingStrategy, useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import api from '../api';

interface Waypoint {
  id: string; // Sortable needs unique id
  destinationId: string;
  name: string;
  latitude: number;
  longitude: number;
}

const initialWaypoints: Waypoint[] = [
  { id: '1', destinationId: 'd1', name: 'Hội An', latitude: 15.88, longitude: 108.33 },
  { id: '2', destinationId: 'd2', name: 'Đà Nẵng', latitude: 16.07, longitude: 108.22 },
  { id: '3', destinationId: 'd3', name: 'Huế', latitude: 16.46, longitude: 107.59 },
];

const SortableItem = ({ id, wp }: { id: string, wp: Waypoint }) => {
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
  const [items, setItems] = useState<Waypoint[]>(initialWaypoints);
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
    } catch (e) {
      alert('Failed to optimize route');
    }
    setOptimizing(false);
  };

  return (
    <div className="planner-container">
      <h2>Day 1 Itinerary</h2>
      <div className="planner-actions">
        <button onClick={handleOptimize} disabled={optimizing} className="primary-btn">
          {optimizing ? 'Optimizing via ORS...' : '✨ Optimize Route Order'}
        </button>
      </div>
      
      {stats.distance > 0 && (
        <div className="route-stats">
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
  );
};

export default Planner;
