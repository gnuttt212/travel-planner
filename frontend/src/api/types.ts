export interface TravelContextRequest {
  purpose: string;
  tripDate: string;
  duration: string;
  groupType: string;
  groupSize: number;
  budgetPerPerson: number;
  styles: string[];
  startLat: number;
  startLon: number;
  transportation: string;
  city: string;
  variantName?: string;
}

export interface TripActivity {
  id: string;
  destinationId: string;
  orderIndex: number;
  plannedStartTime: string;
  plannedEndTime: string;
  estimatedDurationMinutes: number;
  estimatedCost: number;
  destinationName: string;
  destinationCategory: string;
  destinationLat: number;
  destinationLon: number;
  destinationRating: number;
  destinationImageUrl: string;
  travelDistanceKm: number;
  travelTimeMinutes: number;
  status: string;
}

export interface TripPlanResponse {
  variantName: string;
  variantDescription: string;
  aiNarrative?: string;
  activities: TripActivity[];
  totalCost: number;
  totalDistanceKm: number;
}

export interface TripResponse {
  id: string;
  title: string;
  status: string;
  tripDate: string;
  purpose: string;
  groupSize: number;
  activities: TripActivity[];
  createdAt: string;
}

export interface ProfileResponse {
  email: string;
  role: string;
  createdAt: string;
  displayName: string | null;
  avatarUrl: string | null;
  bio: string | null;
}

export interface UserDto {
  email: string;
}

export interface UserSearchResult {
  email: string;
  displayName: string | null;
  avatarUrl: string | null;
  relationshipStatus: string;
}

export interface Destination {
  id: string;
  name: string;
  description: string;
  city?: string;
  address?: string;
  category?: string | null;
  latitude: number;
  longitude: number;
  avgRating: number;
  reviewCount: number;
  avgCostPerPerson?: number;
  tags: string[];
  indoor: boolean;
  imageUrl?: string;
  distanceKm?: number | null;
}

export interface FriendRequestDto {
  id: string;
  senderEmail: string;
  receiverEmail: string;
  status: string;
}
