import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  readonly username = signal<string | null>(null);

  setUsername(name: string) {
    this.username.set(name);
  }

  clear() {
    this.username.set(null);
  }
}
