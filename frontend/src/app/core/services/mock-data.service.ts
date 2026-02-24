import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { MockDataStats } from '../models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class MockDataService {
  private apiUrl = `${environment.apiUrl}/mock-data`;

  constructor(private http: HttpClient) {}

  /**
   * Quick setup: Clear DB and create 100 test sessions
   */
  quickSetup(): Observable<number> {
    return this.http.post<number>(`${this.apiUrl}/quick-setup`, {});
  }

  /**
   * Generate specific number of sessions
   */
  generateSessions(count: number): Observable<number> {
    const params = new HttpParams().set('count', count.toString());
    return this.http.post<number>(`${this.apiUrl}/generate`, {}, { params });
  }

  /**
   * Generate comprehensive dataset (24h, 7d, 30d)
   */
  generateComprehensive(): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/generate-comprehensive`, {});
  }

  /**
   * Get mock data statistics
   */
  getStats(): Observable<MockDataStats> {
    return this.http.get<MockDataStats>(`${this.apiUrl}/stats`);
  }

  /**
   * Clear all mock data
   */
  clearAll(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/clear`);
  }
}
