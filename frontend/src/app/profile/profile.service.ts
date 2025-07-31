import {Injectable, WritableSignal} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {MemberResponseDTO} from '../auth/member-response-dto.model';
import {Observable} from 'rxjs';
import {ServiceAppointmentReviewModel} from '../service/service-details/service-appointment-review-model';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = 'http://localhost:8080/members';
  private uploadUrl = 'http://localhost:8080/auth/uploads';

  constructor(private http: HttpClient) {}

  getMemberById(id: number){
    return this.http.get<MemberResponseDTO>(
      `${this.apiUrl}/${id}`,
    );
  }

  uploadProfilePicture(formData: FormData, id : number) {
    return this.http.post<{imageUrl: string}>(
      `${this.uploadUrl}/profile-picture?id=${id}`,
      formData,
    );
  }

  updateMember(updatedUser: Partial<MemberResponseDTO>, id:number) {
    return this.http.put(`${this.apiUrl}/${id}`, updatedUser);
  }

  getReviewsByMemberId(memberId: number): Observable<ServiceAppointmentReviewModel[]> {
    return this.http.get<ServiceAppointmentReviewModel[]>(`${this.apiUrl}/${memberId}/reviews`);
  }

  deleteMember(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  revertDeleteAccount(token: string) {
    return this.http.post(`${this.apiUrl}/revert-delete/${token}`, {});
  }
}
