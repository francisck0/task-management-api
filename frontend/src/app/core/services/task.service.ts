import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import {
  Task,
  TaskRequestDto,
  TaskPatchDto,
  TaskFilterDto,
  TaskStatistics,
  TaskStatus,
  Page,
  PageRequest
} from '@core/models';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private readonly apiUrl = `${environment.apiUrl}/tasks`;

  constructor(private http: HttpClient) {}

  /**
   * Get all tasks with pagination
   */
  getAllTasks(pageRequest?: PageRequest): Observable<Page<Task>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<Task>>(this.apiUrl, { params });
  }

  /**
   * Get task by ID
   */
  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get tasks by status
   */
  getTasksByStatus(status: TaskStatus, pageRequest?: PageRequest): Observable<Page<Task>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<Task>>(`${this.apiUrl}/status/${status}`, { params });
  }

  /**
   * Search tasks by title
   */
  searchTasks(title: string, pageRequest?: PageRequest): Observable<Page<Task>> {
    let params = this.buildPageParams(pageRequest);
    params = params.append('title', title);
    return this.http.get<Page<Task>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Filter tasks with advanced criteria
   */
  filterTasks(filters: TaskFilterDto, pageRequest?: PageRequest): Observable<Page<Task>> {
    let params = this.buildPageParams(pageRequest);

    if (filters.status) params = params.append('status', filters.status);
    if (filters.priority) params = params.append('priority', filters.priority);
    if (filters.createdAfter) params = params.append('createdAfter', filters.createdAfter);
    if (filters.createdBefore) params = params.append('createdBefore', filters.createdBefore);
    if (filters.dueDateAfter) params = params.append('dueDateAfter', filters.dueDateAfter);
    if (filters.dueDateBefore) params = params.append('dueDateBefore', filters.dueDateBefore);
    if (filters.search) params = params.append('search', filters.search);

    return this.http.get<Page<Task>>(`${this.apiUrl}/filter`, { params });
  }

  /**
   * Create new task
   */
  createTask(task: TaskRequestDto): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, task);
  }

  /**
   * Update task completely (PUT)
   */
  updateTask(id: number, task: TaskRequestDto): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task);
  }

  /**
   * Update task partially (PATCH)
   */
  patchTask(id: number, task: TaskPatchDto): Observable<Task> {
    return this.http.patch<Task>(`${this.apiUrl}/${id}`, task);
  }

  /**
   * Delete task (soft delete)
   */
  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get task statistics
   */
  getStatistics(): Observable<TaskStatistics> {
    return this.http.get<TaskStatistics>(`${this.apiUrl}/statistics`);
  }

  /**
   * Get deleted tasks (trash)
   */
  getDeletedTasks(pageRequest?: PageRequest): Observable<Page<Task>> {
    let params = this.buildPageParams(pageRequest);
    return this.http.get<Page<Task>>(`${this.apiUrl}/trash`, { params });
  }

  /**
   * Restore deleted task
   */
  restoreTask(id: number): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/${id}/restore`, {});
  }

  /**
   * Purge old deleted tasks
   */
  purgeOldDeletedTasks(retentionDays: number = 90): Observable<any> {
    const params = new HttpParams().set('retentionDays', retentionDays.toString());
    return this.http.delete(`${this.apiUrl}/trash/purge`, { params });
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
