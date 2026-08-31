import json
import random
import os

PLACES_FILE = r"D:\travel-planner\places.json"
SEED_FILE = r"D:\travel-planner\src\main\resources\seed_destinations.json"

CATEGORY_MAP = {
    ("food_drink", "cafe"): "CAFE",
    ("food_drink", "restaurant"): "RESTAURANT",
    ("leisure", "park"): "PARK",
    ("attraction", "museum"): "MUSEUM",
    ("food_drink", "bar"): "BAR",
    ("shopping", "marketplace"): "MARKET",
    ("attraction", "beach"): "BEACH",
    ("attraction", "temple"): "TEMPLE",
    ("shopping", "mall"): "SHOPPING",
    ("attraction", "theme_park"): "ENTERTAINMENT",
    ("food_drink", "food_court"): "STREET_FOOD",
    ("leisure", "garden"): "GARDEN",
    ("attraction", "viewpoint"): "VIEWPOINT"
}

def load_existing_seeds():
    if os.path.exists(SEED_FILE):
        with open(SEED_FILE, 'r', encoding='utf-8') as f:
            try:
                return json.load(f)
            except json.JSONDecodeError:
                return []
    return []

def extract_city(place):
    # Try to get city from address or tags
    tags = place.get("source_tags", {})
    if "addr:city" in tags:
        return tags["addr:city"]
    if "addr:province" in tags:
        return tags["addr:province"]
    return "Việt Nam"

def generate_random_rating():
    # Generate rating between 3.5 and 5.0
    return round(random.uniform(3.5, 5.0), 1)

def generate_random_cost(category):
    # Generate reasonable cost based on category
    base_cost = {
        "CAFE": 50000,
        "RESTAURANT": 150000,
        "PARK": 0,
        "MUSEUM": 40000,
        "BAR": 200000,
        "MARKET": 100000,
        "BEACH": 0,
        "TEMPLE": 0,
        "SHOPPING": 500000,
        "ENTERTAINMENT": 300000,
        "STREET_FOOD": 30000,
        "GARDEN": 30000,
        "VIEWPOINT": 0
    }
    cost = base_cost.get(category, 50000)
    if cost == 0:
        return 0
    variation = random.uniform(0.8, 1.5)
    # Round to nearest 10k
    return int((cost * variation) / 10000) * 10000

def generate_random_hours():
    return json.dumps({
        "mon": "08:00-22:00",
        "tue": "08:00-22:00",
        "wed": "08:00-22:00",
        "thu": "08:00-22:00",
        "fri": "08:00-22:00",
        "sat": "08:00-23:00",
        "sun": "08:00-23:00"
    })

def process_places():
    existing_seeds = load_existing_seeds()
    print(f"Loaded {len(existing_seeds)} existing seeds.")
    
    # We want to add at most this many places
    MAX_NEW_PLACES = 2000
    new_places = []
    
    with open(PLACES_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)
        
    random.shuffle(data) # Shuffle to get a random distribution
    
    for place in data:
        if len(new_places) >= MAX_NEW_PLACES:
            break
            
        name = place.get("name")
        if not name:
            continue
            
        cat = place.get("category")
        subcat = place.get("subcategory")
        
        target_category = CATEGORY_MAP.get((cat, subcat))
        
        # If direct mapping fails, try some broad mappings
        if not target_category:
            if cat == "food_drink" and subcat in ["fast_food", "cafe", "restaurant", "bar"]:
                if subcat == "fast_food": target_category = "STREET_FOOD"
            elif cat == "attraction" and subcat in ["museum", "artwork", "gallery"]:
                target_category = "MUSEUM"
            elif cat == "attraction" and subcat == "theme_park":
                target_category = "ENTERTAINMENT"
            elif cat == "leisure" and subcat in ["park", "garden"]:
                target_category = "PARK" if subcat == "park" else "GARDEN"
                
        if not target_category:
            continue
            
        city = extract_city(place)
        
        # Build the destination object
        dest = {
            "name": name,
            "description": place.get("name_i18n", {}).get("en", f"Một địa điểm thú vị tại {city} ({target_category})"),
            "city": city,
            "address": place.get("address") or f"{name}, {city}",
            "category": target_category,
            "latitude": place.get("lat", 0.0),
            "longitude": place.get("lon", 0.0),
            "avgRating": generate_random_rating(),
            "reviewCount": random.randint(10, 500),
            "avgCostPerPerson": generate_random_cost(target_category),
            "tags": json.dumps([target_category.lower(), "travel", city.lower().replace(" ", "")]),
            "openingHours": generate_random_hours(),
            "indoor": target_category in ["MUSEUM", "CAFE", "RESTAURANT", "BAR", "SHOPPING"],
            "imageUrl": f"https://source.unsplash.com/800x600/?{target_category.lower()},{city.replace(' ', '')}",
            "bestMonths": [1, 2, 3, 4, 10, 11, 12]
        }
        
        new_places.append(dest)
        
    print(f"Processed {len(new_places)} new places.")
    
    # Merge existing and new
    all_places = existing_seeds + new_places
    
    with open(SEED_FILE, 'w', encoding='utf-8') as f:
        json.dump(all_places, f, ensure_ascii=False, indent=2)
        
    print(f"Successfully wrote {len(all_places)} places to {SEED_FILE}")

if __name__ == "__main__":
    process_places()
