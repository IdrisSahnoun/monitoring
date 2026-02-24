import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-analytics',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1>User Analytics</h1>
      <p>User activity and performance tracking</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }
  `]
})
export class UserAnalyticsComponent {}
