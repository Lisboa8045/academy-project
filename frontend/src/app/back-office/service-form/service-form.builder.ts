import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export function buildServiceForm(fb: FormBuilder, initialData: any = {}): FormGroup {
  return fb.group({
    name: [initialData.name || '', [Validators.maxLength(60), Validators.required]],
    description: [initialData.description || '', [Validators.maxLength(2000), Validators.required]],
    price: [initialData.price ?? 0, [Validators.min(0.01), Validators.required]],
    discount: [initialData.discount ?? 0, [Validators.min(0), Validators.max(100)]],
    negotiable: [initialData.negotiable ?? false],
    duration: [initialData.duration ?? 30, [Validators.min(1), Validators.required]],
    entity: [initialData.entity || '', [Validators.maxLength(10), Validators.required]],
    serviceTypeName: [initialData.serviceTypeName || '', Validators.required],
    permissions: [initialData.permissions || []],
    tagNames: fb.array(
      (initialData.tagNames || []).map((t: string) => fb.control(t))
    ),
    images: fb.array(
      (initialData.images || []).map((i: string) => fb.control(i))
    )
  });
}
