import {effect, Injectable, signal} from '@angular/core';
import {UserProfileService} from '../profile/user-profile.service';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  readonly username = signal<string>("");
  readonly id = signal<number>(-1);
  readonly profilePicture = signal<string>("");
  readonly role = signal<string>("");

  constructor(private readonly userProfileService: UserProfileService) {
    effect(() => {

      const fileName = this.profilePicture();

      if (fileName) {
        console.log(fileName);
        this.userProfileService.loadImage(fileName);
      } else {
        this.userProfileService.revoke(); // Clean up when cleared
      }
    });
  }

  setUsername(name: string) {
    this.username.set(name);
  }

  setId(id:number){
    this.id.set(id)
  }

  setProfilePicture(profilePicture: string) {
    this.profilePicture.set(profilePicture);
    console.log(profilePicture);
  }

  setRole(role: string){
    this.role.set(role)
  }

  clear() {
    this.username.set("");
    this.id.set(-1);
    this.profilePicture.set("");
    this.role.set('');
  }
}
