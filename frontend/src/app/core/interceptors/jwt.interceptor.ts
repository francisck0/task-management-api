import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '@core/services';

/**
 * JWT Interceptor - Adds JWT token to HTTP requests
 * Also handles token refresh on 401 errors
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Skip interceptor for auth endpoints
  if (req.url.includes('/auth/')) {
    return next(req);
  }

  // Add token to request
  const token = authService.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Handle response
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle 401 Unauthorized - Try to refresh token
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        const refreshToken = authService.getRefreshToken();

        if (refreshToken) {
          // Try to refresh the token
          return authService.refreshToken(refreshToken).pipe(
            switchMap(() => {
              // Retry the original request with new token
              const newToken = authService.getToken();
              const clonedReq = req.clone({
                setHeaders: {
                  Authorization: `Bearer ${newToken}`
                }
              });
              return next(clonedReq);
            }),
            catchError((refreshError) => {
              // Refresh failed, logout and redirect to login
              authService.logout().subscribe();
              router.navigate(['/auth/login']);
              return throwError(() => refreshError);
            })
          );
        } else {
          // No refresh token, logout and redirect to login
          authService.logout().subscribe();
          router.navigate(['/auth/login']);
        }
      }

      return throwError(() => error);
    })
  );
};
