import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  readonly username = signal<string>("");
  readonly id = signal<number>(-1);

  setUsername(name: string) {
    this.username.set(name);
  }

  setId(id:number){
    this.id.set(id)
  }

  clear() {
    this.username.set("");
    this.id.set(-1);
  }
}
