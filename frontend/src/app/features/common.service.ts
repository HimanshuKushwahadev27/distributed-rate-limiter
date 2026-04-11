import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface responseInv{
  id: string,
  quantity: number,
  skuCode: string
}

export interface requestInv{
  quantity: number,
    skuCode: string
}

export interface requestUpdateInv{
    id: string,
    skuCode: string,
  quantity: number
}

export interface createOrder{
  pricePaid: number,
    quantity: number,
  skuCode: string
}

@Injectable({
  providedIn: 'root',
})
export class CommonService {

  private http = inject(HttpClient);

  getInventory(): Observable<responseInv[]> {
    return this.http.get<responseInv[]>('/api/inventory/get');
  }

  createInventory(request: requestInv): Observable<string> {
    return this.http.post<string>('/api/inventory/create', request);
  }

  updateInventory(request: requestUpdateInv): Observable<string> {
    return this.http.patch<string>('/api/inventory/update', request);
  }

  createOrder(request: createOrder): Observable<string> {
    return this.http.post<string>('/api/order/create', request);
  } 
  

}
