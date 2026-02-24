import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  DashboardStats,
  RealtimeStats,
  WorkerMetric,
  DateRangeFilter
} from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  /**
   * Get dashboard statistics
   */
  getDashboardStats(filter?: DateRangeFilter): Observable<DashboardStats> {
    let params = new HttpParams();
    if (filter?.startDate) {
      params = params.set('startDate', filter.startDate);
    }
    if (filter?.endDate) {
      params = params.set('endDate', filter.endDate);
    }
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`, { params });
  }

  /**
   * Get real-time statistics
   */
  getRealtimeStats(): Observable<RealtimeStats> {
    return this.http.get<RealtimeStats>(`${this.apiUrl}/realtime`);
  }

  /**
   * Get worker-specific statistics
   */
  getWorkerStats(messageType: string): Observable<WorkerMetric> {
    return this.http.get<WorkerMetric>(`${this.apiUrl}/worker/${messageType}`);
  }

  /**
   * Get top products
   */
  getTopProducts(limit: number = 5): Observable<any[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<any[]>(`${this.apiUrl}/top-products`, { params });
  }
}
