import React, { useEffect, useRef, useState } from 'react';

type Transport = 'MOTORBIKE' | 'CAR' | 'PUBLIC';

type Props = {
  cities: string[];
  city?: string;
  onCityChange?: (c: string) => void;
  transportation?: Transport;
  onTransportationChange?: (t: Transport) => void;
  onUseCurrentLocation?: (lat: number, lon: number) => void;
};

/**
 * LocationTransport
 * Modern, minimal dark-themed combobox + segmented transport control.
 * Uses Tailwind CSS classes for styling and is keyboard & screenreader friendly.
 */
export default function LocationTransport({
  cities,
  city,
  onCityChange,
  transportation,
  onTransportationChange,
  onUseCurrentLocation,
}: Props) {
  const [query, setQuery] = useState(city || '');
  const [open, setOpen] = useState(false);
  const [filtered, setFiltered] = useState<string[]>([]);
  const [activeIndex, setActiveIndex] = useState(-1);

  const inputRef = useRef<HTMLInputElement | null>(null);
  const listRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    setQuery(city || '');
  }, [city]);

  useEffect(() => {
    const q = query.trim().toLowerCase();
    if (q.length < 1) {
      setFiltered([]);
      return;
    }
    setFiltered(cities.filter(c => c.toLowerCase().includes(q)).slice(0, 8));
    setActiveIndex(0);
  }, [query, cities]);

  const choose = (c: string) => {
    setQuery(c);
    setOpen(false);
    onCityChange?.(c);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex(i => Math.min(filtered.length - 1, i + 1));
      setOpen(true);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex(i => Math.max(0, i - 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (filtered[activeIndex]) choose(filtered[activeIndex]);
    } else if (e.key === 'Escape') {
      setOpen(false);
    }
  };

  const useLocation = () => {
    if (!navigator.geolocation) return alert('Trình duyệt không hỗ trợ lấy vị trí.');
    const ok = confirm('Bạn có muốn chia sẻ vị trí hiện tại để tính thời gian di chuyển không?');
    if (!ok) return;
    navigator.geolocation.getCurrentPosition((pos) => {
      const lat = pos.coords.latitude;
      const lon = pos.coords.longitude;
      onUseCurrentLocation?.(lat, lon);
    }, (err) => {
      alert('Không thể lấy vị trí: ' + err.message);
    });
  };

  return (
    <div className="w-full max-w-2xl mx-auto">
      <div className="bg-gradient-to-b from-[#071028] to-[#0b1530] border border-gray-800 rounded-xl p-5 shadow-lg">
        <h3 className="text-lg font-semibold text-slate-200 mb-4">Địa điểm & Di chuyển</h3>

        {/* Combobox */}
        <div className="mb-4">
          <label className="block text-sm text-slate-300 mb-2">Thành phố</label>
          <div className="relative">
            <input
              ref={inputRef}
              role="combobox"
              aria-expanded={open}
              aria-controls="city-listbox"
              aria-autocomplete="list"
              className="w-full bg-[#061226] border border-gray-700 text-slate-100 placeholder-slate-400 rounded-lg py-3 pl-4 pr-10 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
              placeholder={'🔍 Gõ tên tỉnh/thành để tìm kiếm...'}
              value={query}
              onChange={(e) => { setQuery(e.target.value); setOpen(true); }}
              onFocus={() => setOpen(true)}
              onKeyDown={handleKeyDown}
            />

            {/* geo icon button inside input (right) */}
            <button
              aria-label="Dùng vị trí hiện tại"
              onClick={useLocation}
              className="absolute right-2 top-1/2 -translate-y-1/2 inline-flex items-center justify-center h-8 w-8 rounded-md bg-indigo-600 hover:bg-indigo-500 text-white shadow-sm"
              title="Dùng vị trí hiện tại"
            >
              📍
            </button>

            {/* dropdown list */}
            {open && filtered.length > 0 && (
              <div
                id="city-listbox"
                role="listbox"
                ref={listRef}
                className="mt-2 max-h-60 overflow-auto bg-[#061226] border border-gray-700 rounded-lg shadow-lg z-10"
              >
                {filtered.map((c, idx) => (
                  <div
                    key={c}
                    role="option"
                    aria-selected={idx === activeIndex}
                    className={`px-4 py-3 cursor-pointer text-slate-100 hover:bg-indigo-600/20 ${idx === activeIndex ? 'bg-indigo-600/25' : ''}`}
                    onMouseDown={(e) => { e.preventDefault(); choose(c); }}
                    onMouseEnter={() => setActiveIndex(idx)}
                  >
                    {c}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Transportation segmented control */}
        <div>
          <label className="block text-sm text-slate-300 mb-2">Phương tiện</label>
          <div className="grid grid-cols-3 gap-3 sm:grid-cols-3">
            {[
              { id: 'MOTORBIKE', label: '🏍️ Xe máy' },
              { id: 'CAR', label: '🚗 Ô tô' },
              { id: 'PUBLIC', label: '🚌 Công cộng' },
            ].map((t) => {
              const tid = t.id as Transport;
              const active = transportation === tid;
              return (
                <button
                  key={t.id}
                  role="radio"
                  aria-checked={active}
                  onClick={() => onTransportationChange?.(tid)}
                  className={`flex items-center justify-center gap-2 text-sm font-medium py-3 px-3 rounded-lg border ${active ? 'bg-indigo-600 text-white border-indigo-500 shadow' : 'bg-[#071226] text-slate-200 border-gray-700 hover:bg-indigo-600/10'} transition`}
                >
                  <span className="text-lg">{t.label}</span>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}

// Usage example (place in a page):
// <LocationTransport cities={["Hà Nội","Hồ Chí Minh","Đà Nẵng","Đà Lạt"]} onCityChange={c => setCity(c)} transportation={transport} onTransportationChange={setTransport} onUseCurrentLocation={(lat,lon)=>{/* ... */}} />
