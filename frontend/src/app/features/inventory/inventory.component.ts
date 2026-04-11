import { Component, inject } from '@angular/core';
import { CommonService, requestInv } from '../common.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-inventory',
  imports: 
  [
      MatCardModule,
      MatFormFieldModule,
      MatInputModule,
      MatButtonModule,
      ReactiveFormsModule
  ],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss',
})
export class InventoryComponent {

  public commonService = inject(CommonService);

  inventoryForm: FormGroup = new FormGroup({});

  constructor(private fb : FormBuilder) {
    this.inventoryForm = this.fb.group({
      skuCode: ['', Validators.required],
      quantity: ['', [Validators.required, Validators.min(1)]],
    });
  }

  onSubmit() {
    if (this.inventoryForm.valid) {
      const request: requestInv = {
        skuCode: this.inventoryForm.value.skuCode,
        quantity: this.inventoryForm.value.quantity,
      };
      this.commonService.createInventory(request).subscribe((response) => {
        console.log(response);
      });
    }

  }
}
