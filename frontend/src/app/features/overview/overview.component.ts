import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { interval, Subject, takeUntil } from 'rxjs';

import { DashboardService } from '@core/services/dashboard.service';
import { MockDataService } from '@core/services/mock-data.service';
import { DashboardStats, RealtimeStats } from '@core/models/dashboard.model';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatFormFieldModule,
    FormsModule,
    NgChartsModule
  ],
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  
  loading = true;
  stats: DashboardStats | null = null;
  realtimeStats: RealtimeStats | null = null;
  selectedPeriod = '24h';
  
  // Chart configurations
  lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: []
  };
  
  lineChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      },
      title: {
        display: true,
        text: 'Sessions Over Time'
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };
  
  barChartData: ChartData<'bar'> = {
    labels: [],
    datasets: []
  };
  
  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      },
      title: {
        display: true,
        text: 'Sessions by Status'
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };
  
  doughnutChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: []
  };
  
  doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'right'
      },
      title: {
        display: true,
        text: 'Success vs Failure Rate'
      }
    }
  };

  constructor(
    private dashboardService: DashboardService,
    private mockDataService: MockDataService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.startRealtimeUpdates();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDashboardData(): void {
    this.loading = true;
    const filter = this.getDateFilter();
    
    this.dashboardService.getDashboardStats(filter)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.stats = data;
          this.updateCharts(data);
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading dashboard data:', error);
          this.loading = false;
        }
      });
  }

  startRealtimeUpdates(): void {
    // Update real-time stats every 30 seconds
    interval(30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.loadRealtimeStats();
      });
    
    // Initial load
    this.loadRealtimeStats();
  }

  loadRealtimeStats(): void {
    this.dashboardService.getRealtimeStats()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.realtimeStats = data;
        },
        error: (error) => {
          console.error('Error loading realtime stats:', error);
        }
      });
  }

  onPeriodChange(): void {
    this.loadDashboardData();
  }

  refreshData(): void {
    this.loadDashboardData();
    this.loadRealtimeStats();
  }

  private getDateFilter() {
    const now = new Date();
    let startDate: Date;

    switch (this.selectedPeriod) {
      case '1h':
        startDate = new Date(now.getTime() - 1 * 60 * 60 * 1000);
        break;
      case '24h':
        startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        break;
      case '7d':
        startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        break;
      case '30d':
        startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        break;
      default:
        startDate = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    }

    return {
      startDate: startDate.toISOString(),
      endDate: now.toISOString()
    };
  }

  private updateCharts(data: DashboardStats): void {
    // Update sessions by status bar chart
    if (data.sessionsByStatus) {
      this.barChartData = {
        labels: data.sessionsByStatus.map(s => s.status),
        datasets: [{
          data: data.sessionsByStatus.map(s => s.count),
          backgroundColor: [
            '#4caf50', // CLOSED
            '#f44336', // ERROR
            '#2196f3', // RUNNING
            '#ff9800', // STARTING
            '#9c27b0', // INITIALIZING
            '#757575', // CANCELLED
            '#ff5722'  // SHUTDOWN_REQUESTED
          ],
          label: 'Sessions'
        }]
      };
    }

    // Update success rate doughnut chart
    const completed = data.completedSessions || 0;
    const failed = data.failedSessions || 0;
    
    this.doughnutChartData = {
      labels: ['Successful', 'Failed'],
      datasets: [{
        data: [completed, failed],
        backgroundColor: ['#4caf50', '#f44336']
      }]
    };

    // Update timeline line chart
    if (data.sessionsByHour) {
      this.lineChartData = {
        labels: data.sessionsByHour.map(h => h.hour),
        datasets: [{
          data: data.sessionsByHour.map(h => h.count),
          label: 'Sessions',
          fill: true,
          borderColor: '#3f51b5',
          backgroundColor: 'rgba(63, 81, 181, 0.1)',
          tension: 0.4
        }]
      };
    }
  }

  getStatusIcon(status: string): string {
    const iconMap: { [key: string]: string } = {
      'CLOSED': 'check_circle',
      'ERROR': 'error',
      'RUNNING': 'play_circle',
      'STARTING': 'cached',
      'INITIALIZING': 'hourglass_empty',
      'CANCELLED': 'cancel',
      'SHUTDOWN_REQUESTED': 'stop_circle'
    };
    return iconMap[status] || 'help';
  }

  getStatusColor(status: string): string {
    const colorMap: { [key: string]: string } = {
      'CLOSED': 'success',
      'ERROR': 'error',
      'RUNNING': 'info',
      'STARTING': 'warning',
      'INITIALIZING': 'info',
      'CANCELLED': 'disabled',
      'SHUTDOWN_REQUESTED': 'warning'
    };
    return colorMap[status] || 'disabled';
  }

  formatDuration(ms: number): string {
    if (!ms) return '0s';
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);

    if (hours > 0) {
      return `${hours}h ${minutes % 60}m`;
    } else if (minutes > 0) {
      return `${minutes}m ${seconds % 60}s`;
    } else {
      return `${seconds}s`;
    }
  }
}
