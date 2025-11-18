import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TaskService } from '@core/services';
import { Task, Page, TaskStatus, TaskPriority, TaskFilterDto } from '@core/models';
import { LoadingComponent, PaginationComponent } from '@shared/components';
import { TaskStatusPipe, TaskPriorityPipe, DateFormatPipe } from '@shared/pipes';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    LoadingComponent,
    PaginationComponent,
    TaskStatusPipe,
    TaskPriorityPipe,
    DateFormatPipe
  ],
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css']
})
export class TaskListComponent implements OnInit {
  tasksPage = signal<Page<Task> | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  filterForm: FormGroup;
  showFilters = signal(false);

  TaskStatus = TaskStatus;
  TaskPriority = TaskPriority;

  constructor(
    private taskService: TaskService,
    private fb: FormBuilder
  ) {
    this.filterForm = this.fb.group({
      status: [''],
      priority: [''],
      search: ['']
    });
  }

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(page: number = 0): void {
    this.loading.set(true);
    this.error.set(null);

    const filters: TaskFilterDto = this.filterForm.value;
    const hasFilters = Object.values(filters).some(v => v);

    const request$ = hasFilters
      ? this.taskService.filterTasks(filters, { page, size: 20 })
      : this.taskService.getAllTasks({ page, size: 20 });

    request$.subscribe({
      next: (page) => {
        this.tasksPage.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar tareas');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  applyFilters(): void {
    this.loadTasks(0);
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.loadTasks(0);
  }

  deleteTask(id: number, event: Event): void {
    event.stopPropagation();
    if (confirm('¿Está seguro de eliminar esta tarea?')) {
      this.taskService.deleteTask(id).subscribe({
        next: () => {
          this.loadTasks(this.tasksPage()?.number || 0);
        },
        error: (err) => {
          alert('Error al eliminar tarea');
          console.error(err);
        }
      });
    }
  }

  onPageChange(page: number): void {
    this.loadTasks(page);
  }
}
