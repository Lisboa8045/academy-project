import {Component, inject, input, OnInit} from '@angular/core';
import {ServiceProviderModel} from '../../../models/service-provider.model';
import {ProviderPermissionEnumModel} from '../../../models/provider-permission.enum';
import {NgForOf} from '@angular/common';
import {EditServiceService} from '../edit-service.service';
import {snackBarSuccess} from '../../../shared/snackbar/snackbar-success';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarError} from '../../../shared/snackbar/snackbar-error';
import {ProfileService} from '../../../profile/profile.service';
import {MemberResponseDTO} from '../../../auth/member-response-dto.model';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-manage-workers',
  imports: [
    NgForOf
  ],
  templateUrl: './manage-workers.component.html',
  styleUrl: './manage-workers.component.css'
})
export class ManageWorkersComponent implements OnInit {
  private editServiceService = inject(EditServiceService);
  private snackBar = inject(MatSnackBar);
  private profileService = inject(ProfileService);
  private route = inject(ActivatedRoute);
  serviceProviders = input<ServiceProviderModel[]>();
  members: MemberResponseDTO[] = [];
  permissions = Object.keys(ProviderPermissionEnumModel) as ProviderPermissionEnumModel[];

  ngOnInit() {
    this.getMembersForServiceProviders();
  }

  private getMembersForServiceProviders(){
    for (let provider of this.serviceProviders()!) {
      this.profileService.getMemberByUsername(provider.memberName).subscribe({
        next: (member) => {
          this.members.push(member);
        }
      });
    }
  }

  togglePermission(provider: ServiceProviderModel, permission: ProviderPermissionEnumModel) {
    let providerPermission = provider.permissions;
    if (providerPermission.includes(permission)) {
      providerPermission = providerPermission.filter(p => p !== permission);
      provider.permissions = providerPermission;
      return;
    }
    providerPermission.push(permission);
    provider.permissions = providerPermission;
  }

  updatePermissions(provider: ServiceProviderModel) {
    let member = this.members.find((member) => member.username == provider.memberName);
    let updatePermissionRequest = {
      permissions: provider.permissions,
      memberId: member!.id
    }
    let serviceId = Number(this.route.snapshot.paramMap.get('id'));
    this.editServiceService.setServiceProviderPermissions(serviceId, updatePermissionRequest).subscribe({
      next: () => {
        snackBarSuccess(this.snackBar, `Permissions for ${provider.memberName} updated successfully!`);
      },
      error: () => {
        snackBarError(this.snackBar, `Unable to update permissions for ${provider.memberName}`);
      }
    })
  }
}
