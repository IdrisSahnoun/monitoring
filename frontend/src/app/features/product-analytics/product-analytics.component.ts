import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1>Product Analytics</h1>
      <p>Product-specific metrics and trends</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }
  `]
})
export class ProductAnalyticsComponent {}
