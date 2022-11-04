package com.permissionx.guolindev.compat

import android.content.Context
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

class BluetoothPermissionCheck : BasePermissionCheck(){
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_SCAN) ||
            PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_CONNECT) ||
            PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_ADVERTISE)
        ) {
            if(AndroidVersion.isAndroid12) {
                return PermissionX.isGranted(context, permission)
            }else {
                if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_SCAN)) {
                    return PermissionX.isGranted(context, Permission.ACCESS_FINE_LOCATION)
                }
                if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_CONNECT) ||
                    PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_ADVERTISE)
                ) {
                    return true
                }
            }
        }
        return super.isPermissionGranted(context, permission)
    }
}