package com.util;

import android.Manifest;
import android.content.pm.PackageManager;

/**
 * Created by Cao Dinh Son on 11/13/2016.
 */

public abstract class PermissionUtil {

    public static String[] PERMISSIONS_CONTACT_PHONE = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS, Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_NETWORK_STATE};

    public static final int REQUEST_CONTACTS = 0;

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
