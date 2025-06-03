import {Injectable, WritableSignal} from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = 'http://localhost:8080/members';

  constructor(private http: HttpClient) {}

  getMemberById(id: WritableSignal<number | null>){
    return this.http.get<MemberResponseDTO>(
      `${this.apiUrl}/${id}`,
      { withCredentials: true }
    );
  }
}

export interface MemberResponseDTO {
  id: number;
  username: string;
  email: string;
  address: string;
  postalCode: string;
  phoneNumber: string;
  role: string;
}
