import {Component, inject, OnInit, signal} from '@angular/core';
import {ServiceProviderModel, ServiceProviderRequestDTO} from '../../../models/service-provider.model';
import {ProviderPermissionEnumModel} from '../../../models/provider-permission.enum';
import {NgForOf} from '@angular/common';
import {EditServiceService} from '../edit-service.service';
import {snackBarSuccess} from '../../../shared/snackbar/snackbar-success';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarError} from '../../../shared/snackbar/snackbar-error';
import {ProfileService} from '../../../profile/profile.service';
import {MemberResponseDTO} from '../../../auth/member-response-dto.model';
import {ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {LoadingComponent} from '../../../loading/loading.component';

@Component({
  selector: 'app-manage-workers',
  imports: [
    NgForOf,
    FormsModule,
    LoadingComponent
  ],
  templateUrl: './manage-workers.component.html',
  styleUrl: './manage-workers.component.css'
})
export class ManageWorkersComponent implements OnInit {
  private editServiceService = inject(EditServiceService);
  private snackBar = inject(MatSnackBar);
  private profileService = inject(ProfileService);
  private route = inject(ActivatedRoute);
  private serviceId = Number(this.route.snapshot.paramMap.get('id'));
  serviceProviders = signal<ServiceProviderModel[]>([]);
  members: MemberResponseDTO[] = [];
  newWorkers?: MemberResponseDTO[];
  permissions = Object.keys(ProviderPermissionEnumModel) as ProviderPermissionEnumModel[];
  selectedWorker = signal<MemberResponseDTO | undefined>(undefined);

  ngOnInit() {
    this.fetchServiceProviders(this.serviceId);
  }
  private fetchServiceProviders(id: number): void {
    this.editServiceService.getServiceProvidersByServiceId(id).subscribe({
      next: (data) => {
        this.serviceProviders.set(data.filter(provider => provider.active));
        this.getMembersForServiceProviders();
      },
      error: () => {
        console.error("Error loading service providers");
      }
    });
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

  removeServiceProvider(providerToDelete: ServiceProviderModel) {
    console.log('removing ' + providerToDelete.memberName);
    this.editServiceService.deleteServiceProvider(providerToDelete.id).subscribe({
      next: () => {
        let newServiceProviders =
          this.serviceProviders()?.filter(provider => provider.id != providerToDelete.id)
        this.serviceProviders.set(newServiceProviders);
        this.members = this.members.filter(member => member.username != providerToDelete.memberName);
        this.searchForNewWorkers();
        snackBarSuccess(this.snackBar, `Service Provider ${providerToDelete.memberName} removed successfully!`);
      },
      error:() => {
        snackBarError(this.snackBar, `Unable to remove ${providerToDelete.memberName}`);
      }
    })
  }

  addWorker() {
    if (!this.selectedWorker()) {
      snackBarError(this.snackBar, 'Please select a worker to add');
      return;
    }
    let selectedWorker = this.selectedWorker()!;
    let request = {
      serviceId: this.serviceId,
      memberId: selectedWorker.id,
      permissions: [],
      isServiceCreation: false
    } as ServiceProviderRequestDTO;
    this.editServiceService.createServiceProvider(request).subscribe({
      next: (serviceProvider) => {
        this.newWorkers = this.newWorkers!.filter(worker => worker.username != selectedWorker.username);
        this.serviceProviders.set([...this.serviceProviders(), serviceProvider]);
        this.members.push(selectedWorker);
        snackBarSuccess(this.snackBar, `Worker ${selectedWorker.username} added successfully!`);
      },
      error: () => {
        snackBarError(this.snackBar, `Unable to add worker ${selectedWorker.username}`);
      }
    })
    this.selectedWorker.set(undefined);
  }

  searchForNewWorkers() {
    console.log('Searching for workers')
    this.profileService.getAllMembers().subscribe({
      next: (members) => {
        this.newWorkers = members.filter(member => member.role == 'WORKER' && !this.members.find(m => m.username == member.username));
      },
    });
  }
}
