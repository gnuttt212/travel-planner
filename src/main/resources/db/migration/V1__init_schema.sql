CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    avatar_url VARCHAR(1024),
    bio TEXT,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    tag_weights JSONB,
    min_budget NUMERIC,
    max_budget NUMERIC,
    travel_style VARCHAR(255),
    group_type VARCHAR(255),
    preference_vector TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID,
    title VARCHAR(255),
    status VARCHAR(50),
    trip_date DATE,
    duration VARCHAR(50),
    purpose VARCHAR(50),
    group_size INTEGER,
    start_lat DOUBLE PRECISION,
    start_lon DOUBLE PRECISION,
    transportation VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE trip_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    destination_id UUID,
    order_index INTEGER,
    planned_start_time TIME,
    planned_end_time TIME,
    estimated_duration_minutes INTEGER,
    estimated_cost NUMERIC,
    status VARCHAR(50),
    destination_name VARCHAR(255),
    destination_category VARCHAR(255),
    destination_lat DOUBLE PRECISION,
    destination_lon DOUBLE PRECISION,
    destination_rating DOUBLE PRECISION,
    destination_image_url VARCHAR(1024),
    travel_distance_km DOUBLE PRECISION,
    travel_time_minutes INTEGER,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE TABLE destinations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255),
    description TEXT,
    city VARCHAR(255),
    address VARCHAR(1024),
    category VARCHAR(50),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    avg_rating DOUBLE PRECISION,
    review_count INTEGER,
    avg_cost_per_person NUMERIC,
    tags TEXT,
    opening_hours TEXT,
    is_indoor BOOLEAN,
    image_url VARCHAR(1024),
    best_months TEXT
);

CREATE TABLE friend_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_email VARCHAR(255) NOT NULL,
    receiver_email VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (sender_email, receiver_email)
);

CREATE TABLE friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    friend_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE (user_id, friend_id)
);

CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    itinerary_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    total_amount NUMERIC,
    currency VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    budget_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    amount NUMERIC NOT NULL,
    category VARCHAR(255),
    expense_date DATE,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE messages (
    id VARCHAR(255) PRIMARY KEY,
    sender_email VARCHAR(255) NOT NULL,
    receiver_email VARCHAR(255) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE comments (
    id VARCHAR(255) PRIMARY KEY,
    trip_id UUID NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE reactions (
    id VARCHAR(255) PRIMARY KEY,
    author_email VARCHAR(255) NOT NULL,
    target_type VARCHAR(255) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
