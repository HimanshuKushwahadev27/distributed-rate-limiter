import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/service/auth.service';
import { HomeComponent } from './shared/home/home.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HomeComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('rateLimiter');

  private authService = inject(AuthService);

  ngOnInit() {
    this.authService.initLogin();
  }
}
