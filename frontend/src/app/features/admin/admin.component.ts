import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuditService } from '@core/services';
import { AuditLog, Page } from '@core/models';
import { LoadingComponent, PaginationComponent } from '@shared/components';
import { DateFormatPipe } from '@shared/pipes';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LoadingComponent,
    PaginationComponent,
    DateFormatPipe
  ],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  auditLogsPage = signal<Page<AuditLog> | null>(null);
  statistics = signal<any>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(private auditService: AuditService) {}

  ngOnInit(): void {
    this.loadAuditLogs();
    this.loadStatistics();
  }

  loadAuditLogs(page: number = 0): void {
    this.loading.set(true);
    this.error.set(null);

    this.auditService.getAllAuditLogs({ page, size: 20 }).subscribe({
      next: (page) => {
        this.auditLogsPage.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar registros de auditoría');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  loadStatistics(): void {
    this.auditService.getStatistics().subscribe({
      next: (stats) => {
        this.statistics.set(stats);
      },
      error: (err) => {
        console.error('Error loading statistics:', err);
      }
    });
  }

  onPageChange(page: number): void {
    this.loadAuditLogs(page);
  }

  getStatusClass(status: string): string {
    return status === 'SUCCESS' ? 'badge-success' : 'badge-danger';
  }
}
