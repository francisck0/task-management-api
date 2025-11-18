# Task Management Frontend

Frontend application built with Angular 19 for the Task Management API.

## Features

- 🔐 Authentication (Login, Register, JWT with Refresh Tokens)
- 📋 Task Management (CRUD operations with pagination and filters)
- 📊 Dashboard with Statistics
- 🗑️ Trash/Recycle Bin for deleted tasks
- 👥 Admin Panel with Audit Logs
- 🎨 Responsive Design
- ⚡ Angular 19 Features (Standalone Components, Signals, New Control Flow)

## Prerequisites

- Node.js 18+
- npm or yarn
- Angular CLI 19

## Installation

```bash
# Install dependencies
npm install

# Install Angular CLI globally (if not installed)
npm install -g @angular/cli@19
```

## Development Server

```bash
# Start development server
npm start

# Or with ng serve
ng serve
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Build

```bash
# Build for production
npm run build

# Output will be in dist/ directory
```

## Project Structure

```
src/
├── app/
│   ├── core/              # Core services, guards, interceptors
│   │   ├── guards/        # Route guards
│   │   ├── interceptors/  # HTTP interceptors
│   │   ├── models/        # TypeScript interfaces
│   │   └── services/      # API services
│   ├── features/          # Feature modules
│   │   ├── auth/          # Authentication
│   │   ├── dashboard/     # Dashboard
│   │   ├── tasks/         # Task management
│   │   ├── trash/         # Trash/Recycle bin
│   │   └── admin/         # Admin panel
│   ├── shared/            # Shared components
│   │   ├── components/    # Reusable components
│   │   └── pipes/         # Custom pipes
│   ├── app.component.ts   # Root component
│   ├── app.config.ts      # App configuration
│   └── app.routes.ts      # Route definitions
├── assets/                # Static assets
├── environments/          # Environment configs
└── styles.css            # Global styles
```

## API Configuration

The application connects to the backend API at `http://localhost:8080/api/v1`.

To change the API URL, modify the environment configuration in:
- `src/environments/environment.ts` (development)
- `src/environments/environment.prod.ts` (production)

## Technologies

- **Angular 19**: Latest Angular version with standalone components
- **TypeScript**: Strongly typed JavaScript
- **RxJS**: Reactive programming
- **Signals**: Angular's new reactivity primitive
- **CSS3**: Modern styling

## Available Scripts

- `npm start` - Start development server
- `npm run build` - Build for production
- `npm run watch` - Build and watch for changes
- `npm test` - Run tests
- `npm run lint` - Run linter

## Backend Integration

This frontend integrates with the Spring Boot Task Management API.

**API Endpoints:**
- `/api/v1/auth/*` - Authentication
- `/api/v1/tasks/*` - Task operations
- `/api/v1/audit/*` - Audit logs (Admin only)

## License

MIT
