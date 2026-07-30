# LocationTransport Component

This document describes the `LocationTransport` React component introduced in the frontend to replace the older city input and stacked transport buttons.

It is implemented at: [frontend/src/components/LocationTransport.tsx](frontend/src/components/LocationTransport.tsx#L1-L999)

## Purpose

- Provide an accessible, responsive, and compact UI for selecting a city (combobox) and transportation mode (segmented radio-cards).
- Use a modern dark navy theme with soft rounded corners and clear active/hover states.

## Features

- Combobox search with filtered dropdown (keyboard support: ArrowUp/ArrowDown/Enter/Escape).
- Small geolocation icon button inside the input to populate the current location (asks user confirmation before reading geolocation).
- Transportation control implemented as a 3-way segmented grid of radio cards with active and hover styles.
- Tailwind CSS utility classes used for styling for quick customization.
- Accessibility: ARIA roles for combobox/listbox/options and radio roles for transport buttons.

## Component API (props)

- `cities: string[]` — list of available city names to search/filter.
- `city?: string` — current selected city (controlled initial value).
- `onCityChange?: (c: string) => void` — callback when user selects a city.
- `transportation?: 'MOTORBIKE' | 'CAR' | 'PUBLIC'` — currently selected transport.
- `onTransportationChange?: (t) => void` — callback when transport changes.
- `onUseCurrentLocation?: (lat: number, lon: number) => void` — called when user allows geolocation.

## Integration

The new component is integrated into the planning flow in:
- [frontend/src/pages/ContextCards.tsx](frontend/src/pages/ContextCards.tsx#L1-L999)

When integrating, wire the following callbacks to your planning form state:

```tsx
<LocationTransport
  cities={cities}
  city={formData.city}
  onCityChange={(c) => updateForm('city', c)}
  transportation={formData.transportation}
  onTransportationChange={(t) => updateForm('transportation', t)}
  onUseCurrentLocation={(lat, lon) => { updateForm('startLat', lat); updateForm('startLon', lon); }}
/>
```

## Styling and Tailwind

- The component uses Tailwind utility classes. The frontend now includes `tailwind.config.js` and `postcss.config.js`, so utility classes are processed during build.
- To change the accent color, update the `bg-indigo-600` / `focus:ring-indigo-500` classes or extend the Tailwind theme.

## Accessibility notes

- The combobox uses `role="combobox"` and `role="listbox"` for the dropdown. Options are marked with `role="option"` and `aria-selected`.
- Transport choices use `role="radio"` and `aria-checked`.
- Keyboard support includes Arrow Up/Down to navigate options, Enter to select, and Escape to close.

## Testing / Smoke checks

1. Start the frontend dev server and open the planning page:

```bash
cd frontend
npm run dev
```

2. On the planning step "Địa điểm & Di chuyển":
- Type at least one character to see filtered suggestions.
- Use Arrow keys to navigate and Enter to select.
- Click the 📍 icon and accept the browser geolocation prompt to populate `startLat`/`startLon` and attempt to choose the nearest city.
- Click a transport card and confirm the active visual state updates.

## Troubleshooting

- If the dropdown is not showing, check that `cities` prop is populated by the planning API: `planningApi.getCities()`.
- If Tailwind classes appear not to apply, ensure the frontend build includes Tailwind processing and the dev server restarted after changes.

---
Document generated: 2026-07-29
