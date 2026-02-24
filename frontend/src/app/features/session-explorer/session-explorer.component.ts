import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-session-explorer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1>Session Explorer</h1>
      <p>Search and view diagnostic sessions</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }
  `]
})
export class SessionExplorerComponent {}
