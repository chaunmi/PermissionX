package com.permissionx.guolindev.compat

import android.content.Context
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

class WifiPermissionCheck: BasePermissionCheck() {
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if(PermissionUtils.equalsPermission(permission, Permission.NEARBY_WIFI_DEVICES)) {
            if(AndroidVersion.isAndroid13) {
               return PermissionX.isGranted(context, permission)
            }else {
                return PermissionX.isGranted(context, Permission.ACCESS_FINE_LOCATION)
            }
        }
        return super.isPermissionGranted(context, permission)
    }
}