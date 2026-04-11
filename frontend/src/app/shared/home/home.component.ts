import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonService, responseInv } from '../../features/common.service';
import { AuthService } from '../../core/auth/service/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-home',
  imports:
   [
    MatIconModule,
    RouterLink,
    MatCardModule,
    MatListModule,
    MatDividerModule
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {

  public commonService = inject(CommonService);
  public authService = inject(AuthService);
  inventoryList: responseInv[] = [];
  private cdr = inject(ChangeDetectorRef);
  isLoggedIn = false;

  ngOnInit() {
      this.isLoggedIn = this.authService.isLoggedIn() as unknown as boolean;

      if (this.isLoggedIn) {
        this.commonService.getInventory().subscribe(res => {
          this.inventoryList = res;
          this.cdr.detectChanges();
        });
      }
  }

    logout(){
    this.authService.logout();
  }

  login(){
    this.authService.login();
  }
}
