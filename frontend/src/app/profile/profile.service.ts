import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {MemberResponseDTO} from '../auth/member-response-dto.model';
import {Observable} from 'rxjs';
import {ServiceAppointmentReviewModel} from '../service/service-details/service-appointment-review-model';

export interface PagedReviewResponse {
  content: ServiceAppointmentReviewModel[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

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

  getMemberByUsername(username: string){
    return this.http.get<MemberResponseDTO>(
      `${this.apiUrl}/byUsername/${username}`,
    );
  }

  getWorkersContainsUsername(username: string){
    return this.http.get<MemberResponseDTO[]>(
      `${this.apiUrl}/search?username=${username}&roleName=WORKER`,
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

  getAllMembers() {
    return this.http.get<MemberResponseDTO[]>(
      this.apiUrl
    );
  }

  getAllReviewsByMemberId(memberId: number): Observable<ServiceAppointmentReviewModel[]> {
    return this.http.get<ServiceAppointmentReviewModel[]>(`${this.apiUrl}/${memberId}/allReviews`);
  }

  getReviewsByMemberId(memberId: number, page = 0, size = 5): Observable<PagedReviewResponse> {
    let params = new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString());

    return this.http.get<PagedReviewResponse>(`${this.apiUrl}/${memberId}/reviews`, { params });
  }

  deleteMember(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  revertDeleteAccount(token: string) {
    return this.http.post(`${this.apiUrl}/revert-delete/${token}`, {});
  }
}
