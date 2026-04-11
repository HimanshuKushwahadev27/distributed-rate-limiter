import { Injectable, inject } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from '../auth.config';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

    private oauthService = inject(OAuthService);


    initLogin() {
      this.oauthService.configure(authConfig);
      this.oauthService.setupAutomaticSilentRefresh();
      this.oauthService.loadDiscoveryDocumentAndTryLogin().then(() => {
        if (!this.oauthService.hasValidAccessToken()) {
          this.login();
        }
      });
    }


    login(){
        this.oauthService.initCodeFlow();
    }

    logout(){
        this.oauthService.logOut();
    }

    isLoggedIn(): boolean {
        return this.oauthService.hasValidAccessToken();
    }

    getToken(): string | null {
        return this.oauthService.getAccessToken();
    }

}
