import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Page } from '@core/models';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.css']
})
export class PaginationComponent {
  @Input() page!: Page<any>;
  @Output() pageChange = new EventEmitter<number>();

  get pages(): number[] {
    const totalPages = this.page.totalPages;
    const currentPage = this.page.number;
    const maxPagesToShow = 5;

    if (totalPages <= maxPagesToShow) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }

    const pages: number[] = [];
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);

    if (currentPage <= 2) {
      endPage = Math.min(totalPages - 1, maxPagesToShow - 1);
    }

    if (currentPage >= totalPages - 3) {
      startPage = Math.max(0, totalPages - maxPagesToShow);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.page.totalPages && page !== this.page.number) {
      this.pageChange.emit(page);
    }
  }

  goToFirstPage(): void {
    this.goToPage(0);
  }

  goToLastPage(): void {
    this.goToPage(this.page.totalPages - 1);
  }

  goToPreviousPage(): void {
    this.goToPage(this.page.number - 1);
  }

  goToNextPage(): void {
    this.goToPage(this.page.number + 1);
  }
}
