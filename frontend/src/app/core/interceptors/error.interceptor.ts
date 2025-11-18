import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ErrorResponse } from '@core/models';

/**
 * Error Interceptor - Global error handling for HTTP requests
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Ha ocurrido un error inesperado';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        const errorResponse = error.error as ErrorResponse;

        if (errorResponse && errorResponse.message) {
          errorMessage = errorResponse.message;

          // Add validation errors if present
          if (errorResponse.errors && errorResponse.errors.length > 0) {
            errorMessage += '\n' + errorResponse.errors.join('\n');
          }
        } else {
          // Fallback error message based on status code
          switch (error.status) {
            case 0:
              errorMessage = 'No se puede conectar al servidor. Verifique su conexión.';
              break;
            case 400:
              errorMessage = 'Solicitud inválida. Verifique los datos enviados.';
              break;
            case 401:
              errorMessage = 'No autorizado. Por favor, inicie sesión.';
              break;
            case 403:
              errorMessage = 'Acceso denegado. No tiene permisos para realizar esta acción.';
              break;
            case 404:
              errorMessage = 'Recurso no encontrado.';
              break;
            case 500:
              errorMessage = 'Error interno del servidor.';
              break;
            case 503:
              errorMessage = 'Servicio no disponible. Intente más tarde.';
              break;
            default:
              errorMessage = `Error: ${error.status} - ${error.statusText}`;
          }
        }
      }

      console.error('HTTP Error:', {
        message: errorMessage,
        status: error.status,
        error: error.error
      });

      // You can show a toast/notification here
      // this.notificationService.showError(errorMessage);

      return throwError(() => ({
        message: errorMessage,
        error: error.error,
        status: error.status
      }));
    })
  );
};
