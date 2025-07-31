import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export function buildServiceForm(fb: FormBuilder, initialData: any = {}): FormGroup {
  return fb.group({
    name: [initialData.name || '', Validators.required],
    description: [initialData.description || ''],
    price: [initialData.price ?? 0, [Validators.min(0)]],
    discount: [initialData.discount ?? 0, [Validators.min(0)]],
    negotiable: [initialData.negotiable ?? false],
    duration: [initialData.duration ?? 30, Validators.min(1)],
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
