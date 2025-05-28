import { HttpInterceptorFn } from '@angular/common/http';
const apiBaseUrl = 'http://localhost:8080';

export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  const shouldSendCredentials = req.url.startsWith(apiBaseUrl);
  const modifiedReq = req.clone({
    withCredentials: shouldSendCredentials
  });
  return next(modifiedReq);
};
