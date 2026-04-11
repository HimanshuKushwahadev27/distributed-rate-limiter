import { Injectable, inject } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from '../auth.config';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

    private oauthService = inject(OAuthService);
    private router = inject(Router);
    initLogin() {
      this.oauthService.configure(authConfig);
      this.oauthService.setupAutomaticSilentRefresh();
      this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
        if (!this.oauthService.hasValidAccessToken()) {
          this.login();
        }else{
          this.router.navigate(['/home']);
        }
      });
    }


    login(){
        this.oauthService.initCodeFlow();
    }

    logout(){
        this.oauthService.logOut();
    }

    isLoggedIn(): Promise<boolean> {
      return Promise.resolve(this.oauthService.hasValidAccessToken());
    }

    getToken(): string | null {
        return this.oauthService.getAccessToken();
    }

}
