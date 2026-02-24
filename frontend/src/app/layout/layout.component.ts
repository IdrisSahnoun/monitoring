import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent {
  menuItems = [
    { icon: 'dashboard', label: 'Overview', route: '/overview' },
    { icon: 'search', label: 'Session Explorer', route: '/sessions' },
    { icon: 'work', label: 'Worker Analytics', route: '/workers' },
    { icon: 'inventory', label: 'Product Analytics', route: '/products' },
    { icon: 'people', label: 'User Analytics', route: '/users' }
  ];
}
