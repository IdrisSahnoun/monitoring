# DiagCloud Monitoring Frontend

Angular frontend for the DiagCloud monitoring and supervision system.

## Features

### 5 Main Pages

1. **Overview** - Dashboard with system-wide statistics
2. **Session Explorer** - Search and view diagnostic sessions
3. **Worker Analytics** - Analyze Saga worker performance
4. **Product Analytics** - Product-specific metrics and trends  
5. **User Analytics** - User activity and performance tracking

## Prerequisites

- Node.js 18+ and npm 9+
- Angular CLI 17+
- Backend API running on `http://localhost:8080`

## Getting Started

### 1. Install Dependencies

```powershell
cd frontend
npm install
```

### 2. Configure Backend URL

Edit `src/environments/environment.ts` if your backend is not on `localhost:8080`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### 3. Start Development Server

```powershell
npm start
```

The application will open at `http://localhost:4200`

### 4. Build for Production

```powershell
npm run build
```

Output will be in `dist/diagcloud-monitoring/`

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── core/              # Core services and interceptors
│   │   │   ├── services/      # API services
│   │   │   ├── interceptors/  # HTTP interceptors
│   │   │   └── guards/        # Route guards
│   │   ├── shared/            # Shared components and utilities
│   │   │   ├── components/    # Reusable components
│   │   │   ├── directives/    # Custom directives
│   │   │   └── pipes/         # Custom pipes
│   │   ├── features/          # Feature modules
│   │   │   ├── overview/      # Overview page
│   │   │   ├── session-explorer/ # Session search
│   │   │   ├── worker-analytics/ # Worker stats
│   │   │   ├── product-analytics/ # Product stats
│   │   │   └── user-analytics/ # User stats
│   │   ├── layout/            # Layout components
│   │   └── app.component.ts
│   ├── assets/                # Static assets
│   ├── environments/          # Environment configs
│   └── styles.scss            # Global styles
├── angular.json
├── package.json
└── tsconfig.json
```

## Development

### Running Tests

```powershell
npm test
```

### Linting

```powershell
npm run lint
```

### Proxy Configuration

The app uses a proxy configuration (`src/proxy.conf.json`) to avoid CORS issues during development:
- All requests to `/api/*` are proxied to `http://localhost:8080/api/*`

## Deployment

### Build for Production

```powershell
npm run build
```

### Deploy with Nginx

1. Copy `dist/diagcloud-monitoring/*` to nginx html directory
2. Configure nginx:

```nginx
server {
    listen 80;
    server_name monitoring.diagcloud.com;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Docker Deployment

```dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/diagcloud-monitoring /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## API Integration

The frontend communicates with the backend through these main services:

- `SessionService` - Session CRUD operations
- `DashboardService` - Statistics and metrics
- `WorkerService` - Worker analytics
- `ProductService` - Product analytics
- `UserService` - User analytics
- `ReportService` - PDF report generation

## Technologies

- **Angular 17** - Frontend framework
- **Angular Material** - UI component library
- **Chart.js / ng2-charts** - Data visualization
- **RxJS** - Reactive programming
- **TypeScript** - Type-safe development
- **SCSS** - Styling

## Related Documents

- [Backend README](../backend/README.md) - Backend API documentation
- [Testing Guide](../backend/TESTING_GUIDE.md) - How to test with mock data
- [DiagCloud Updates](../backend/DIAGCLOUD_UPDATES.md) - Domain model reference

## Next Steps

1. ✅ Project structure set up
2. ⏳ Implement core services
3. ⏳ Build Overview page
4. ⏳ Build Session Explorer page
5. ⏳ Build Worker Analytics page
6. ⏳ Build Product Analytics page
7. ⏳ Build User Analytics page
8. ⏳ Add authentication
9. ⏳ Deploy to production
