import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { AuditLog, AuditStatistics, Page, PageRequest } from '@core/models';

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private readonly apiUrl = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {}

  /**
   * Get all audit logs
   */
  getAllAuditLogs(pageRequest?: PageRequest): Observable<Page<AuditLog>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<AuditLog>>(this.apiUrl, { params });
  }

  /**
   * Get audit logs by username
   */
  getAuditLogsByUsername(username: string, pageRequest?: PageRequest): Observable<Page<AuditLog>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<AuditLog>>(`${this.apiUrl}/user/${username}`, { params });
  }

  /**
   * Get audit logs by action
   */
  getAuditLogsByAction(action: string, pageRequest?: PageRequest): Observable<Page<AuditLog>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<AuditLog>>(`${this.apiUrl}/action/${action}`, { params });
  }

  /**
   * Get audit logs by date range
   */
  getAuditLogsByDateRange(
    startDate: string,
    endDate: string,
    pageRequest?: PageRequest
  ): Observable<Page<AuditLog>> {
    let params = this.buildPageParams(pageRequest);
    params = params.append('startDate', startDate);
    params = params.append('endDate', endDate);
    return this.http.get<Page<AuditLog>>(`${this.apiUrl}/date-range`, { params });
  }

  /**
   * Get resource history
   */
  getResourceHistory(
    resource: string,
    resourceId: number,
    pageRequest?: PageRequest
  ): Observable<Page<AuditLog>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<AuditLog>>(`${this.apiUrl}/resource/${resource}/${resourceId}`, { params });
  }

  /**
   * Get failed operations
   */
  getFailures(pageRequest?: PageRequest): Observable<Page<AuditLog>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<AuditLog>>(`${this.apiUrl}/failures`, { params });
  }

  /**
   * Count recent failures
   */
  countRecentFailures(hours: number = 24): Observable<any> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get(`${this.apiUrl}/failures/count`, { params });
  }

  /**
   * Get audit statistics
   */
  getStatistics(): Observable<any> {
    return this.http.get(`${this.apiUrl}/statistics`);
  }

  /**
   * Get most frequent actions
   */
  getMostFrequentActions(limit: number = 10): Observable<{ [key: string]: number }> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/statistics/top-actions`, { params });
  }

  /**
   * Get most active users
   */
  getMostActiveUsers(limit: number = 10): Observable<{ [key: string]: number }> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<{ [key: string]: number }>(`${this.apiUrl}/statistics/top-users`, { params });
  }

  /**
   * Detect suspicious activity
   */
  detectSuspiciousActivity(hours: number = 24): Observable<any> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get(`${this.apiUrl}/suspicious-activity`, { params });
  }

  /**
   * Helper method to build pagination parameters
   */
  private buildPageParams(pageRequest?: PageRequest): HttpParams {
    let params = new HttpParams();

    if (pageRequest) {
      if (pageRequest.page !== undefined) {
        params = params.append('page', pageRequest.page.toString());
      }
      if (pageRequest.size !== undefined) {
        params = params.append('size', pageRequest.size.toString());
      }
      if (pageRequest.sort) {
        pageRequest.sort.forEach(sort => {
          params = params.append('sort', sort);
        });
      }
    }

    return params;
  }
}
