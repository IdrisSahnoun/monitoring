import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-session-detail',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-container">
      <h1>Session Detail</h1>
      <p>Session ID: {{ sessionId }}</p>
    </div>
  `,
  styles: [`
    .page-container {
      padding: 24px;
    }
  `]
})
export class SessionDetailComponent {
  sessionId: string | null = null;

  constructor(private route: ActivatedRoute) {
    this.sessionId = this.route.snapshot.paramMap.get('id');
  }
}
