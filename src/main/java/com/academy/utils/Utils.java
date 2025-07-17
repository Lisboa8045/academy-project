package com.academy.utils;

import com.academy.models.service.service_provider.ProviderPermissionEnum;

import java.util.List;

public class Utils {

    public static boolean hasPermission(List<ProviderPermissionEnum> permissions, ProviderPermissionEnum permission){
        return permissions.contains(permission);
    }

    public static String formatHours(String minutesStr){
        long totalMinutes = Long.parseLong(minutesStr);

        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        String formattedTime;
        if (hours > 0 && minutes > 0) {
            formattedTime = hours + " hour" + (hours > 1 ? "s" : "") + " and " + minutes + " minute" + (minutes > 1 ? "s" : "");
        } else if (hours > 0) {
            formattedTime = hours + " hour" + (hours > 1 ? "s" : "");
        } else {
            formattedTime = minutes + " minute" + (minutes > 1 ? "s" : "");
        }
        return formattedTime;
    }
}
