import { Pipe, PipeTransform } from '@angular/core';
import { TaskPriority } from '@core/models';

@Pipe({
  name: 'taskPriority',
  standalone: true
})
export class TaskPriorityPipe implements PipeTransform {
  transform(priority: TaskPriority | undefined): string {
    if (!priority) return 'Sin prioridad';

    const priorityMap: Record<TaskPriority, string> = {
      [TaskPriority.LOW]: 'Baja',
      [TaskPriority.MEDIUM]: 'Media',
      [TaskPriority.HIGH]: 'Alta',
      [TaskPriority.CRITICAL]: 'Crítica'
    };

    return priorityMap[priority] || priority;
  }
}
