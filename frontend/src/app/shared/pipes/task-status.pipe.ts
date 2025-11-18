import { Pipe, PipeTransform } from '@angular/core';
import { TaskStatus } from '@core/models';

@Pipe({
  name: 'taskStatus',
  standalone: true
})
export class TaskStatusPipe implements PipeTransform {
  transform(status: TaskStatus): string {
    const statusMap: Record<TaskStatus, string> = {
      [TaskStatus.PENDING]: 'Pendiente',
      [TaskStatus.IN_PROGRESS]: 'En Progreso',
      [TaskStatus.COMPLETED]: 'Completada',
      [TaskStatus.CANCELLED]: 'Cancelada'
    };

    return statusMap[status] || status;
  }
}
