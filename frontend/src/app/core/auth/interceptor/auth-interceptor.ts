import {  HttpInterceptorFn } from "@angular/common/http";
import { inject } from '@angular/core';
import { OAuthService } from "angular-oauth2-oidc";

export const authInterceptor : HttpInterceptorFn = (req , next) => {

  const authService  = inject(OAuthService);
  const token = authService.getAccessToken();

  if(token && req.url.includes('/api/')){
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
}