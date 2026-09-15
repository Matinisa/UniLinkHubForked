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

export type ReportStatus = "OPEN" | "UNDER_REVIEW" | "RESOLVED" | "DISMISSED";
export type ReportReason = "MISREPRESENTATION" | "NON_DELIVERY" | "INAPPROPRIATE_CONDUCT" | "SPAM" | "OTHER";

export interface ReporterSummary {
  id: string;
  studentNumber: string;
  fullName: string;
}

export interface TargetSummary {
  type: "LISTING" | "USER";
  id: string;
  label: string;
  secondaryLabel: string | null;
}

export interface ReportSummaryView {
  id: string;
  reason: ReportReason;
  details: string | null;
  status: ReportStatus;
  adminNote: string | null;
  createdAt: string;
  resolvedAt: string | null;
  reporter: ReporterSummary;
  target: TargetSummary;
}

export interface ReportStatusCounts {
  open: number;
  underReview: number;
  resolved: number;
  dismissed: number;
}

export interface AdminBusinessView {
  id: string;
  businessName: string;
  description: string;
  category: string;
  verificationStatus: "PENDING" | "VERIFIED" | "REJECTED";
  createdAt: string;
  updatedAt: string;
  ownerStudentNumber: string;
  ownerFullName: string;
}

export interface ProviderProfileDTO {
  businessId: string;
  businessName: string;
  description: string;
  category: string;
  verificationStatus: "PENDING" | "VERIFIED" | "REJECTED";
  ownerId: string;
  ownerFullName: string;
  activeListingCount: number;
  totalViews: number;
  memberSince: string;
}

export interface AdminStatsDTO {
  totalStudents: number;
  pendingAccounts: number;
  businesses: { pending: number; verified: number; rejected: number };
  listings: { active: number; inactive: number; soldOut: number };
  reports: ReportStatusCounts;
}
