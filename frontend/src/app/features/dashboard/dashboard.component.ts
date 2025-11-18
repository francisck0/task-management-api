import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TaskService, AuthService } from '@core/services';
import { TaskStatistics, Task, Page } from '@core/models';
import { LoadingComponent } from '@shared/components';
import { TaskStatusPipe, DateFormatPipe } from '@shared/pipes';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LoadingComponent,
    TaskStatusPipe,
    DateFormatPipe
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private taskService = inject(TaskService);
  private authService = inject(AuthService);

  statistics = signal<TaskStatistics | null>(null);
  recentTasks = signal<Task[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  currentUser = this.authService.currentUser;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading.set(true);
    this.error.set(null);

    // Load statistics
    this.taskService.getStatistics().subscribe({
      next: (stats) => {
        this.statistics.set(stats);
      },
      error: (err) => {
        this.error.set('Error al cargar estadísticas');
        console.error('Error loading statistics:', err);
      }
    });

    // Load recent tasks
    this.taskService.getAllTasks({ page: 0, size: 5, sort: ['createdAt,desc'] }).subscribe({
      next: (page: Page<Task>) => {
        this.recentTasks.set(page.content);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar tareas recientes');
        this.loading.set(false);
        console.error('Error loading recent tasks:', err);
      }
    });
  }

  getCompletionPercentage(): number {
    const stats = this.statistics();
    if (!stats || stats.totalTasks === 0) return 0;
    return Math.round((stats.completedTasks / stats.totalTasks) * 100);
  }
}
