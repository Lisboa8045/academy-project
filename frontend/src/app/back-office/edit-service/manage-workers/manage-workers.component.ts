import {Component, inject, input, OnInit, signal} from '@angular/core';
import {ServiceProviderModel, ServiceProviderRequestDTO} from '../../../models/service-provider.model';
import {getProviderPermissionEnumLabel, ProviderPermissionEnumModel} from '../../../models/provider-permission.enum';
import {AsyncPipe, NgClass, NgForOf, NgIf} from '@angular/common';
import {EditServiceService} from '../edit-service.service';
import {snackBarSuccess} from '../../../shared/snackbar/snackbar-success';
import {MatSnackBar} from '@angular/material/snack-bar';
import {snackBarError} from '../../../shared/snackbar/snackbar-error';
import {ProfileService} from '../../../profile/profile.service';
import {MemberResponseDTO} from '../../../auth/member-response-dto.model';
import {ActivatedRoute} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {FaIconComponent} from '@fortawesome/angular-fontawesome';
import {faFloppyDisk, faTrash} from '@fortawesome/free-solid-svg-icons';
import {debounceTime, Subject} from 'rxjs';

@Component({
  selector: 'app-manage-workers',
  imports: [
    NgForOf,
    FormsModule,
    FaIconComponent,
    NgIf,
    AsyncPipe,
    NgClass
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
  private readonly debounceTimeMs = 300;
  protected readonly faFloppyDisk = faFloppyDisk;
  protected readonly faTrash = faTrash;
  protected readonly getProviderPermissionEnumLabel = getProviderPermissionEnumLabel;
  loggedUserPermissions = input.required<string[]>();
  allPermissions = Object.keys(ProviderPermissionEnumModel) as ProviderPermissionEnumModel[];
  serviceProviders = signal<ServiceProviderModel[]>([]);
  membersForServiceProviders: MemberResponseDTO[] = [];

  searchTerm = new Subject<string>();
  newWorkers?: MemberResponseDTO[];
  selectedWorker = signal<MemberResponseDTO | undefined>(undefined);

  ngOnInit() {
    this.fetchServiceProviders(this.serviceId);
    this.searchTerm.pipe(debounceTime(this.debounceTimeMs)).subscribe((username) => {
      if (this.selectedWorker() && username != this.selectedWorker()!.username) {
        this.selectedWorker.set(undefined);
      }
      if (username == '') {
        this.newWorkers = undefined;
        return;
      }
      this.searchForNewWorkers(username);
    });
  }

  private fetchServiceProviders(id: number): void {
    this.editServiceService.getServiceProvidersByServiceId(id).subscribe({
      next: (data) => {
        this.serviceProviders.set(data.filter(provider => provider.active));
        this.getMembersForServiceProviders();
      },
      error: () => {
        snackBarError(this.snackBar, "Error loading service providers");
      }
    });
  }

  private getMembersForServiceProviders(){
    for (let provider of this.serviceProviders()!) {
      this.profileService.getMemberByUsername(provider.memberName).subscribe({
        next: (member) => {
          this.membersForServiceProviders.push(member);
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
    let member = this.membersForServiceProviders.find((member) => member.username == provider.memberName);
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
        this.membersForServiceProviders = this.membersForServiceProviders.filter(member => member.username != providerToDelete.memberName);
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
        this.serviceProviders.set([...this.serviceProviders(), serviceProvider]);
        this.membersForServiceProviders.push(selectedWorker);
        this.searchTerm.next('');
        snackBarSuccess(this.snackBar, `Worker ${selectedWorker.username} added successfully!`);
      },
      error: () => {
        snackBarError(this.snackBar, `Unable to add worker ${selectedWorker.username}`);
      }
    })
    this.selectedWorker.set(undefined);
  }

  searchForNewWorkers(searchTerm: string) {
    console.log('Searching for workers')
    this.profileService.getWorkersContainsUsername(searchTerm).subscribe({
      next: (members) => {
        this.newWorkers = members.filter(member => !this.membersForServiceProviders.find(m => m.username == member.username));
      },
    });
  }

  selectWorker(worker: MemberResponseDTO) {
    this.searchTerm.next(worker.username);
    this.selectedWorker.set(worker);
  }

  isCheckboxDisabled(provider: ServiceProviderModel, permission: ProviderPermissionEnumModel) {
    let userLacksPermission = !this.userHasPermission(ProviderPermissionEnumModel.UPDATE_PERMISSIONS);
    let providerIsOwner = provider.permissions.includes(ProviderPermissionEnumModel.OWNER);
    return userLacksPermission || providerIsOwner || permission == ProviderPermissionEnumModel.OWNER;
  }

  userHasPermission(permission: ProviderPermissionEnumModel) {
    return this.loggedUserPermissions().includes(permission);
  }

  protected readonly ProviderPermissionEnumModel = ProviderPermissionEnumModel;
}
