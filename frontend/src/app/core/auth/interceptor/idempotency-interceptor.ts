import { HttpInterceptorFn } from '@angular/common/http';
import { v4 as uuidv4 } from 'uuid';

export const idempotencyInterceptor: HttpInterceptorFn = (req, next) => {

  const isPost = req.method === 'POST';
  const isApiRequest = req.url.startsWith('/api') || req.url.includes('/api/');

  if (isPost && isApiRequest) {

    const cloned = req.clone({
      setHeaders: {
        'Idempotency-Key': uuidv4()
      }
    });

    return next(cloned);
  }

  return next(req);
};