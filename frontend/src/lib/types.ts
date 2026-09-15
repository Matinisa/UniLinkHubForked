export interface UserResponse {
  id: string;
  studentNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | null;
  role: "STUDENT" | "ADMIN";
  accountStatus: "PENDING_VERIFICATION" | "ACTIVE" | "SUSPENDED" | "DEACTIVATED";
  seller: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  user: UserResponse;
}

export interface BusinessDTO {
  id: string;
  ownerId: string;
  businessName: string;
  description: string;
  category: string;
  verificationStatus: "PENDING" | "VERIFIED" | "REJECTED";
  createdAt: string;
}

export interface ListingDTO {
  id: string;
  businessId: string;
  type: "PRODUCT" | "SERVICE";
  name: string;
  description: string;
  category: string;
  price: number;
  status: "ACTIVE" | "INACTIVE" | "SOLD_OUT";
  viewCount: number;
  stockQuantity: number | null;
  imageUrl: string | null;
  durationMinutes: number | null;
  availabilitySchedule: string | null;
  createdAt: string;
}

export interface ReportDTO {
  id: string;
  reporterId: string;
  targetType: "LISTING" | "USER";
  targetId: string;
  reason: string;
  details: string | null;
  status: "OPEN" | "UNDER_REVIEW" | "RESOLVED" | "DISMISSED";
  reviewedByAdminId: string | null;
  adminNote: string | null;
  createdAt: string;
  resolvedAt: string | null;
}
