import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { CommonService, createOrder } from '../common.service';

@Component({
  selector: 'app-order',
  imports:
    [
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        ReactiveFormsModule
    ],
  templateUrl: './order.component.html',
  styleUrl: './order.component.scss',
})
export class OrderComponent {
  public commonService = inject(CommonService);

  orderForm: FormGroup = new FormGroup({});

  constructor(private fb : FormBuilder) {
    this.orderForm = this.fb.group({
      pricePaid: ['', [Validators.required, Validators.min(0)]],
      quantity: ['', [Validators.required, Validators.min(1)]],
      skuCode: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.orderForm.valid) {
      const request: createOrder = {
        pricePaid: this.orderForm.value.pricePaid,
        quantity: this.orderForm.value.quantity,
        skuCode: this.orderForm.value.skuCode,
      };
      this.commonService.createOrder(request).subscribe((response) => {
        console.log(response);
      });
    }

  }
}
