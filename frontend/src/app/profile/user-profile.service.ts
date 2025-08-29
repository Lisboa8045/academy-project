import {Injectable, signal} from '@angular/core';

@Injectable({ providedIn: 'root' })
export class UserProfileService {
  private readonly _imageUrl = signal<string | null>(null);
  readonly imageUrl = this._imageUrl.asReadonly();
  fetched = false;
  private apiUrl = 'http://localhost:8080/auth/uploads';
  serviceImageUrl: string[] = [];

  async loadImage(fileName: string) {
    if (!fileName || this.fetched) return;

    console.log("Fetching image..." + fileName);
    const res = await fetch(`${this.apiUrl}/${fileName}`);
    if (!res.ok) return;

    console.log("Fetched image..." + fileName);

    const blob = await res.blob();
    const objectUrl = URL.createObjectURL(blob);
    this._imageUrl.set(objectUrl);
    this.fetched = true;
  }

  async getImage(fileName: string): Promise<string | null> {
    if (!fileName) return Promise.resolve(null);

    return fetch(`${this.apiUrl}/${fileName}`)
      .then(res => {
        if (!res.ok) return null;
        return res.blob();
      })
      .then(blob => {
        if (!blob) return null;
        return URL.createObjectURL(blob);
      })
      .catch(() => null);
  }

  setImageUrl(url: string | null) {
    this._imageUrl.set(url);
  }

  revoke() {
    const current = this._imageUrl();
    if (current) URL.revokeObjectURL(current);
    this._imageUrl.set(null);
    this.fetched = false;
  }

  async loadImages(fileNames: string[]) {
    if (!fileNames || fileNames.length === 0 || this.fetched) {
      return;
    }

    for (const fileName of fileNames) {
      try {
        console.log("Fetching image..." + fileName);
        const res = await fetch(`${this.apiUrl}/${fileName}`);
        if (!res.ok) return;

        console.log("Fetched image..." + fileName);

        const blob = await res.blob();
        const objectUrl = URL.createObjectURL(blob);
        this.serviceImageUrl.push(objectUrl);
        this.fetched = true;
      } catch (error) {
        console.error("Error loading the image", fileName, error)
      }

    }
  }
}
