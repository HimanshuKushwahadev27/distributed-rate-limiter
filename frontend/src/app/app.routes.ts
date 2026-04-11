import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'home',
   loadComponent: () => (import('./shared/home/home.component'))
      .then(m => m.HomeComponent)
  },
  {path:'inventory/create',
   loadComponent: () => (import('./features/inventory/inventory.component'))
      .then(m => m.InventoryComponent)
  },
  {path:'order/create',
   loadComponent: () => (import('./features/order/order.component'))
      .then(m => m.OrderComponent)
  }
  
];
