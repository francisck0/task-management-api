import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { TaskService } from '@core/services';
import { Task, TaskStatus, TaskPriority } from '@core/models';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css']
})
export class TaskFormComponent implements OnInit {
  taskForm: FormGroup;
  loading = signal(false);
  error = signal<string | null>(null);
  isEditMode = signal(false);
  taskId: number | null = null;

  TaskStatus = TaskStatus;
  TaskPriority = TaskPriority;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(1000)],
      status: [TaskStatus.PENDING, Validators.required],
      priority: [TaskPriority.MEDIUM],
      dueDate: ['']
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.taskId = +id;
      this.isEditMode.set(true);
      this.loadTask(this.taskId);
    }
  }

  loadTask(id: number): void {
    this.loading.set(true);
    this.taskService.getTaskById(id).subscribe({
      next: (task) => {
        this.taskForm.patchValue({
          title: task.title,
          description: task.description,
          status: task.status,
          priority: task.priority,
          dueDate: task.dueDate ? task.dueDate.substring(0, 16) : ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar tarea');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  onSubmit(): void {
    if (this.taskForm.valid) {
      this.loading.set(true);
      this.error.set(null);

      const formValue = this.taskForm.value;
      const taskData = {
        ...formValue,
        dueDate: formValue.dueDate || null
      };

      const request$ = this.isEditMode() && this.taskId
        ? this.taskService.updateTask(this.taskId, taskData)
        : this.taskService.createTask(taskData);

      request$.subscribe({
        next: () => {
          this.router.navigate(['/tasks']);
        },
        error: (err) => {
          this.error.set(err.message || 'Error al guardar tarea');
          this.loading.set(false);
        }
      });
    } else {
      this.taskForm.markAllAsTouched();
    }
  }

  get title() { return this.taskForm.get('title'); }
  get description() { return this.taskForm.get('description'); }
}
