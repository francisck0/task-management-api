import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TaskService } from '@core/services';
import { Task, Page } from '@core/models';
import { LoadingComponent, PaginationComponent } from '@shared/components';
import { TaskStatusPipe, DateFormatPipe } from '@shared/pipes';

@Component({
  selector: 'app-trash',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LoadingComponent,
    PaginationComponent,
    TaskStatusPipe,
    DateFormatPipe
  ],
  templateUrl: './trash.component.html',
  styleUrls: ['./trash.component.css']
})
export class TrashComponent implements OnInit {
  tasksPage = signal<Page<Task> | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadDeletedTasks();
  }

  loadDeletedTasks(page: number = 0): void {
    this.loading.set(true);
    this.error.set(null);

    this.taskService.getDeletedTasks({ page, size: 20 }).subscribe({
      next: (page) => {
        this.tasksPage.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar papelera');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  restoreTask(id: number, event: Event): void {
    event.stopPropagation();
    if (confirm('¿Restaurar esta tarea?')) {
      this.taskService.restoreTask(id).subscribe({
        next: () => {
          this.loadDeletedTasks(this.tasksPage()?.number || 0);
        },
        error: (err) => {
          alert('Error al restaurar tarea');
          console.error(err);
        }
      });
    }
  }

  onPageChange(page: number): void {
    this.loadDeletedTasks(page);
  }
}
