import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-worker-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1>Worker Analytics</h1>
      <p>Analyze Saga worker performance</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }
  `]
})
export class WorkerAnalyticsComponent {}
