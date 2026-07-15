import psycopg2
from sentence_transformers import SentenceTransformer
import json

# DB connection parameters based on docker-compose.yml
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "travel_planner"
DB_USER = "postgres"
DB_PASS = "postgres"

def main():
    print("Loading SentenceTransformer model (all-MiniLM-L6-v2)...")
    model = SentenceTransformer('all-MiniLM-L6-v2')
    
    print("Connecting to database...")
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        cur = conn.cursor()
        
        # Select destinations that don't have an embedding yet
        cur.execute("SELECT id, name, description, tags FROM destinations WHERE embedding IS NULL")
        rows = cur.fetchall()
        
        if not rows:
            print("No destinations need embeddings. All up to date.")
            return

        print(f"Found {len(rows)} destinations to process.")
        
        for row in rows:
            dest_id, name, description, tags = row
            
            # Create a rich text representation
            # Tags might be stored as a string representation of an array depending on JPA mapping, 
            # let's assume it's a comma separated or JSON string if read raw, or postgres array.
            tags_str = ", ".join(tags) if isinstance(tags, list) else str(tags)
            text_to_embed = f"Destination: {name}. Description: {description}. Features: {tags_str}."
            
            # Generate embedding
            embedding = model.encode(text_to_embed)
            
            # Convert numpy array to list for psycopg2 to format it as a vector
            embedding_list = embedding.tolist()
            
            # Update database
            cur.execute(
                "UPDATE destinations SET embedding = %s WHERE id = %s",
                (embedding_list, dest_id)
            )
            print(f"Updated embedding for: {name}")
            
        conn.commit()
        print("All embeddings generated and saved successfully!")
        
    except Exception as e:
        print(f"An error occurred: {e}")
    finally:
        if 'cur' in locals():
            cur.close()
        if 'conn' in locals():
            conn.close()

if __name__ == "__main__":
    main()
