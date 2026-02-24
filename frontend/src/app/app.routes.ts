import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'overview',
    pathMatch: 'full'
  },
  {
    path: 'overview',
    loadComponent: () => import('./features/overview/overview.component').then(m => m.OverviewComponent)
  },
  {
    path: 'sessions',
    loadComponent: () => import('./features/session-explorer/session-explorer.component').then(m => m.SessionExplorerComponent)
  },
  {
    path: 'sessions/:id',
    loadComponent: () => import('./features/session-explorer/session-detail/session-detail.component').then(m => m.SessionDetailComponent)
  },
  {
    path: 'workers',
    loadComponent: () => import('./features/worker-analytics/worker-analytics.component').then(m => m.WorkerAnalyticsComponent)
  },
  {
    path: 'products',
    loadComponent: () => import('./features/product-analytics/product-analytics.component').then(m => m.ProductAnalyticsComponent)
  },
  {
    path: 'users',
    loadComponent: () => import('./features/user-analytics/user-analytics.component').then(m => m.UserAnalyticsComponent)
  },
  {
    path: '**',
    redirectTo: 'overview'
  }
];
