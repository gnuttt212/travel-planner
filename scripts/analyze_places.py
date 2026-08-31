import json
from collections import Counter

file_path = r"D:\travel-planner\places.json"

categories = Counter()

with open(file_path, 'r', encoding='utf-8') as f:
    try:
        data = json.load(f)
        for place in data:
            cat = place.get('category', 'None')
            subcat = place.get('subcategory', 'None')
            categories[(cat, subcat)] += 1
    except Exception as e:
        print("Error reading json:", e)

for k, v in categories.most_common():
    print(f"{k[0]} -> {k[1]}: {v}")
