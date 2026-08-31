import json
import random
import math
import requests
import time
from rapidfuzz import fuzz
from collections import defaultdict
import unittest

def haversine(lat1, lon1, lat2, lon2):
    R = 6371000  # Radius of earth in meters
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)
    a = math.sin(delta_phi / 2.0) ** 2 + \
        math.cos(phi1) * math.cos(phi2) * \
        math.sin(delta_lambda / 2.0) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

# 1. Dedup địa điểm trùng lặp
def dedup_places(places):
    """
    Deduplicates places based on distance (<50m) and name similarity (>=85%).
    Keeps the place with the most source_tags.
    Returns (deduped_places, deduplication_logs).
    """
    logs = []
    # Grid based grouping to avoid O(N^2). Grid size ~ 50m.
    # 1 degree lat ~ 111km, so 50m ~ 0.00045 degrees.
    GRID_SIZE = 0.00045
    grid = defaultdict(list)
    
    for place in places:
        lat = place.get('lat')
        lon = place.get('lon')
        if lat is None or lon is None:
            continue
        grid_x = int(lat / GRID_SIZE)
        grid_y = int(lon / GRID_SIZE)
        grid[(grid_x, grid_y)].append(place)
        
    deduped_places = []
    
    # Process each grid cell
    # Note: To be perfectly accurate, we should check adjacent cells too, 
    # but for this script's simplicity and speed, checking within the same cell or slightly expanded is fine.
    # A simple O(N^2) within small grids is fast enough.
    
    processed_ids = set()
    
    for cell, cell_places in grid.items():
        # Check adjacent cells to ensure we don't miss cross-border <50m
        x, y = cell
        adjacent_places = []
        for dx in [-1, 0, 1]:
            for dy in [-1, 0, 1]:
                adjacent_places.extend(grid.get((x + dx, y + dy), []))
                
        for i, place1 in enumerate(cell_places):
            id1 = place1.get('id')
            if id1 in processed_ids:
                continue
            
            duplicates = [place1]
            for place2 in adjacent_places:
                id2 = place2.get('id')
                if id1 == id2 or id2 in processed_ids:
                    continue
                
                name1 = place1.get('name', '')
                name2 = place2.get('name', '')
                if not name1 or not name2:
                    continue
                
                # Check distance
                dist = haversine(place1['lat'], place1['lon'], place2['lat'], place2['lon'])
                if dist < 50:
                    # Check name similarity
                    sim = fuzz.ratio(name1.lower(), name2.lower())
                    if sim >= 85:
                        duplicates.append(place2)
            
            if len(duplicates) > 1:
                # Keep the one with most tags
                duplicates.sort(key=lambda x: len(x.get('source_tags', {})), reverse=True)
                kept = duplicates[0]
                removed = duplicates[1:]
                deduped_places.append(kept)
                processed_ids.add(kept['id'])
                for r in removed:
                    processed_ids.add(r['id'])
                    logs.append(f"Removed '{r.get('name')}' (id:{r.get('id')}) - duplicate of '{kept.get('name')}' (id:{kept.get('id')}), similarity: {fuzz.ratio(r.get('name','').lower(), kept.get('name','').lower())}%, dist: {haversine(kept['lat'], kept['lon'], r['lat'], r['lon']):.1f}m")
            else:
                deduped_places.append(place1)
                processed_ids.add(id1)
                
    return deduped_places, logs

# 2. Rating suy luận có cơ sở
def infer_rating(tags: dict) -> tuple[float, int]:
    """
    Infers rating and review count from OSM tags.
    Base rating: 3.2 - 4.0 based on meaningful tags count.
    +0.3 to +0.5 if wikidata or wikipedia tag exists.
    Review count: Based on tags + wikidata presence (max 500).
    """
    meaningful_tags = ['website', 'phone', 'opening_hours', 'cuisine', 'contact:website', 'contact:phone', 'contact:facebook', 'internet_access', 'wheelchair', 'outdoor_seating', 'capacity']
    
    count_meaningful = sum(1 for tag in meaningful_tags if tag in tags)
    
    # Base rating mapping
    if count_meaningful >= 4:
        base_rating = 4.0
    elif count_meaningful == 3:
        base_rating = 3.8
    elif count_meaningful == 2:
        base_rating = 3.5
    elif count_meaningful == 1:
        base_rating = 3.3
    else:
        base_rating = 3.2
        
    has_wiki = 'wikidata' in tags or 'wikipedia' in tags
    if has_wiki:
        base_rating += 0.5
        
    rating = min(5.0, base_rating)
    rating = round(rating, 1)
    
    # Infer review count
    base_reviews = count_meaningful * 15
    if has_wiki:
        base_reviews += 300
    
    # Add some pseudo-randomness based on tags length so it's not identical for all
    pseudo_rand = (len(str(tags)) % 50)
    review_count = min(500, base_reviews + pseudo_rand + 10)
    
    return rating, review_count

# 3. Sinh mô tả (description) chi tiết hơn từ tag OSM
def generate_description(tags: dict, category: str, city: str) -> str:
    """
    Generates a natural language description based on OSM tags.
    tags considered: cuisine, outdoor_seating, wheelchair, internet_access, smoking, diet:vegetarian, opening_hours
    """
    clauses = []
    
    cuisine = tags.get('cuisine')
    if cuisine:
        # cuisine in OSM can be comma separated, just take the first one or replace underscore
        c = cuisine.split(';')[0].replace('_', ' ')
        clauses.append(f"phong cách {c}")
        
    outdoor = tags.get('outdoor_seating')
    if outdoor == 'yes':
        clauses.append("có không gian ngoài trời thoáng đãng")
    elif outdoor == 'no':
        clauses.append("với không gian trong nhà ấm cúng")
        
    desc = f"Một địa điểm {category.lower()} thú vị tại {city}"
    if clauses:
        desc += ", " + ", ".join(clauses)
    desc += "."
    
    amenities = []
    if tags.get('internet_access') in ['yes', 'wlan', 'wifi']:
        amenities.append("có wifi miễn phí")
    if tags.get('wheelchair') == 'yes':
        amenities.append("hỗ trợ lối đi cho xe lăn")
    if tags.get('diet:vegetarian') == 'yes':
        amenities.append("phù hợp cho người ăn chay")
    if tags.get('smoking') == 'yes':
        amenities.append("có khu vực hút thuốc")
        
    if amenities:
        desc += " Nơi đây " + ", ".join(amenities) + "."
        
    return desc

# 4. Ảnh — ưu tiên Wikidata trước khi fallback Unsplash
def fetch_wikidata_image(wikidata_id: str) -> str:
    """
    Fetches image URL from Wikidata property P18.
    """
    if not wikidata_id:
        return None
    try:
        url = f"https://www.wikidata.org/wiki/Special:EntityData/{wikidata_id}.json"
        resp = requests.get(url, timeout=5)
        if resp.status_code == 200:
            data = resp.json()
            entity = data.get('entities', {}).get(wikidata_id, {})
            claims = entity.get('claims', {})
            p18 = claims.get('P18', [])
            if p18:
                filename = p18[0].get('mainsnak', {}).get('datavalue', {}).get('value')
                if filename:
                    # Replace spaces with underscores for the URL
                    filename = filename.replace(' ', '_')
                    return f"https://commons.wikimedia.org/wiki/Special:FilePath/{filename}"
    except Exception as e:
        print(f"Error fetching wikidata for {wikidata_id}: {e}")
    return None

def get_images(tags: dict, category: str, city: str) -> tuple[list, str, bool]:
    """
    Returns (images_list, thumbnailUrl, imageIsGeneric).
    Priority: OSM image -> Wikidata P18 -> Unsplash fallback.
    """
    images = []
    image_is_generic = False
    
    # 1. OSM Tag
    osm_img = tags.get('image') or tags.get('wikimedia_commons')
    if osm_img and osm_img.startswith('http'):
        images.append(osm_img)
    elif osm_img and osm_img.startswith('File:'):
        filename = osm_img[5:].replace(' ', '_')
        images.append(f"https://commons.wikimedia.org/wiki/Special:FilePath/{filename}")
        
    # 2. Wikidata
    if not images and 'wikidata' in tags:
        wiki_img = fetch_wikidata_image(tags['wikidata'])
        if wiki_img:
            images.append(wiki_img)
            
    # 3. Unsplash Fallback
    if not images:
        images.append(f"https://source.unsplash.com/800x600/?{category.lower()},{city.replace(' ', '')}")
        image_is_generic = True
        
    thumbnail_url = images[0] if images else ""
    return images, thumbnail_url, image_is_generic


# --- UNIT TESTS ---
class TestPlaceProcessing(unittest.TestCase):
    
    def test_dedup_places(self):
        places = [
            {"id": "1", "name": "Highlands Coffee", "lat": 10.7715, "lon": 106.7043, "source_tags": {"website": "yes"}},
            {"id": "2", "name": "Highlands Coffee", "lat": 10.77151, "lon": 106.70431, "source_tags": {"website": "yes", "phone": "123"}}, # duplicate, more tags
            {"id": "3", "name": "The Coffee House", "lat": 10.7715, "lon": 106.7043, "source_tags": {}}, # diff name
            {"id": "4", "name": "Highlands Coffee", "lat": 11.7715, "lon": 106.7043, "source_tags": {}} # diff location
        ]
        deduped, logs = dedup_places(places)
        self.assertEqual(len(deduped), 3)
        ids = [p['id'] for p in deduped]
        self.assertIn("2", ids)
        self.assertNotIn("1", ids)
        self.assertEqual(len(logs), 1)

    def test_infer_rating(self):
        # Only 1 tag -> 3.3
        self.assertEqual(infer_rating({'phone': '123'})[0], 3.3)
        # 4+ tags -> 4.0
        self.assertEqual(infer_rating({'phone': '1', 'website': '2', 'cuisine': '3', 'opening_hours': '4'})[0], 4.0)
        # Wiki tag -> +0.5 (max 5.0)
        self.assertEqual(infer_rating({'phone': '1', 'website': '2', 'cuisine': '3', 'opening_hours': '4', 'wikidata': 'Q1'})[0], 4.5)
        
    def test_generate_description(self):
        tags = {
            'cuisine': 'vietnamese;asian',
            'outdoor_seating': 'yes',
            'internet_access': 'wifi',
            'wheelchair': 'yes'
        }
        desc = generate_description(tags, "Nhà hàng", "Đà Nẵng")
        self.assertTrue("phong cách vietnamese" in desc)
        self.assertTrue("không gian ngoài trời" in desc)
        self.assertTrue("wifi miễn phí" in desc)
        self.assertTrue("xe lăn" in desc)

if __name__ == '__main__':
    unittest.main()
