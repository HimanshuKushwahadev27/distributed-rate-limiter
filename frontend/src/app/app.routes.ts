import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'',
   loadComponent: () => (import('./shared/home/home.component'))
      .then(m => m.HomeComponent)
  },
  {path:'',
   loadComponent: () => (import('./features/inventory/inventory.component'))
      .then(m => m.InventoryComponent)
  },
  {path:'',
   loadComponent: () => (import('./features/order/order.component'))
      .then(m => m.OrderComponent)
  }
  
];
