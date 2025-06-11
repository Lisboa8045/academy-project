package com.academy.utils;

import com.academy.models.service.service_provider.ProviderPermissionEnum;

import java.util.List;

public class Utils {

    public static boolean hasPermission(List<ProviderPermissionEnum> permissions, ProviderPermissionEnum permission){
        return permissions.contains(permission);
    }
}
