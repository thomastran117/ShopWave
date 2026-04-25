import api from "../api";

export interface VendorAnalyticsSummary {
  vendorId: number;
  marketplaceId: number;
  windowDays: number;
  from: string;
  to: string;
  totalSubOrders: number;
  totalGrossRevenue: number;
  totalCommission: number;
  totalNetRevenue: number;
  avgOrderValue: number;
  cancellationRate: number;
  refundRate: number;
  lateShipmentRate: number;
  avgShipHours: number | null;
}

export interface DailyRevenuePoint {
  day: string;
  gross: number;
  commission: number;
  net: number;
  orderCount: number;
}

export interface VendorRevenue {
  vendorId: number;
  windowDays: number;
  from: string;
  to: string;
  totalGross: number;
  totalCommission: number;
  totalNet: number;
  daily: DailyRevenuePoint[];
}

export interface ProductEntry {
  productId: number;
  productName: string;
  totalUnitsSold: number;
  totalRevenue: number;
}

export interface VendorTopProducts {
  vendorId: number;
  windowDays: number;
  products: ProductEntry[];
}

export interface DailyPoint {
  day: string;
  count: number;
  value: number | null;
}

export interface VendorOrdersMetric {
  total: number;
  cancelled: number;
  cancellationRate: number;
  daily: DailyPoint[];
}

export interface VendorRefundsMetric {
  totalReturns: number;
  totalOrders: number;
  refundRate: number;
  daily: DailyPoint[];
}

export interface PayoutSummary {
  payoutId: number;
  netAmount: number;
  currency: string;
  status: string;
  paidAt: string | null;
}

export interface VendorPayoutsMetric {
  vendorId: number;
  totalPaidOut: number;
  totalPayouts: number;
  recent: PayoutSummary[];
}

export interface MarketplaceAnalyticsSummary {
  marketplaceId: number;
  windowDays: number;
  from: string;
  to: string;
  totalOrders: number;
  gmv: number;
  totalCommission: number;
  takeRate: number;
  activeVendors: number;
  ordersDaily: DailyPoint[];
}

export interface TopVendor {
  vendorId: number;
  vendorName: string;
  totalSubOrders: number;
  totalGrossRevenue: number;
  totalCommission: number;
  cancellationRate: number;
}

export const analyticsApi = {
  // Vendor analytics
  getSummary: (marketplaceId: number, vendorId: number, days = 30) =>
    api.get<VendorAnalyticsSummary>(
      `/marketplaces/${marketplaceId}/vendors/${vendorId}/analytics/summary`,
      { params: { days } }
    ),

  getRevenue: (marketplaceId: number, vendorId: number, days = 30) =>
    api.get<VendorRevenue>(
      `/marketplaces/${marketplaceId}/vendors/${vendorId}/analytics/revenue`,
      { params: { days } }
    ),

  getTopProducts: (marketplaceId: number, vendorId: number, days = 30, limit = 10) =>
    api.get<VendorTopProducts>(
      `/marketplaces/${marketplaceId}/vendors/${vendorId}/analytics/top-products`,
      { params: { days, limit } }
    ),

  getOrders: (marketplaceId: number, vendorId: number, days = 30) =>
    api.get<VendorOrdersMetric>(
      `/marketplaces/${marketplaceId}/vendors/${vendorId}/analytics/orders`,
      { params: { days } }
    ),

  getRefunds: (marketplaceId: number, vendorId: number, days = 30) =>
    api.get<VendorRefundsMetric>(
      `/marketplaces/${marketplaceId}/vendors/${vendorId}/analytics/refunds`,
      { params: { days } }
    ),

  getPayouts: (marketplaceId: number, vendorId: number, recent = 10) =>
    api.get<VendorPayoutsMetric>(
      `/marketplaces/${marketplaceId}/vendors/${vendorId}/analytics/payouts`,
      { params: { recent } }
    ),

  // Marketplace operator analytics
  getMarketplaceSummary: (marketplaceId: number, days = 30) =>
    api.get<MarketplaceAnalyticsSummary>(
      `/marketplaces/${marketplaceId}/analytics/summary`,
      { params: { days } }
    ),

  getTopVendors: (marketplaceId: number, days = 30, limit = 10) =>
    api.get<TopVendor[]>(
      `/marketplaces/${marketplaceId}/analytics/top-vendors`,
      { params: { days, limit } }
    ),
};
