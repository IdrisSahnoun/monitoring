import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  DiagnosticSession,
  SessionFilter,
  PagedResponse
} from '../models/session.model';
import { WorkerEvent, PerformanceMetrics } from '../models/worker-event.model';

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private apiUrl = `${environment.apiUrl}/sessions`;

  constructor(private http: HttpClient) {}

  /**
   * Get session by ID
   */
  getSession(sessionId: string): Observable<DiagnosticSession> {
    return this.http.get<DiagnosticSession>(`${this.apiUrl}/${sessionId}`);
  }

  /**
   * Search sessions with filters
   */
  searchSessions(filter: SessionFilter): Observable<PagedResponse<DiagnosticSession>> {
    return this.http.post<PagedResponse<DiagnosticSession>>(
      `${this.apiUrl}/search`,
      filter
    );
  }

  /**
   * Get worker events for a session
   */
  getWorkerEvents(sessionId: string): Observable<WorkerEvent[]> {
    return this.http.get<WorkerEvent[]>(`${this.apiUrl}/${sessionId}/events`);
  }

  /**
   * Get performance metrics for a session
   */
  getPerformanceMetrics(sessionId: string): Observable<PerformanceMetrics> {
    return this.http.get<PerformanceMetrics>(`${this.apiUrl}/${sessionId}/metrics`);
  }

  /**
   * Get recent sessions
   */
  getRecentSessions(limit: number = 10): Observable<DiagnosticSession[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<DiagnosticSession[]>(`${this.apiUrl}/recent`, { params });
  }

  /**
   * Get sessions by status
   */
  getSessionsByStatus(status: string): Observable<DiagnosticSession[]> {
    const params = new HttpParams().set('status', status);
    return this.http.get<DiagnosticSession[]>(`${this.apiUrl}/by-status`, { params });
  }
}
