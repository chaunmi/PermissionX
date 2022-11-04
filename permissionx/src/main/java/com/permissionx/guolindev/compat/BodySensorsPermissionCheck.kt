package com.permissionx.guolindev.compat

import android.content.Context
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

class BodySensorsPermissionCheck: BasePermissionCheck() {

    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.BODY_SENSORS_BACKGROUND)) {
            if(AndroidVersion.isAndroid13) {
                // 有后台传感器权限的前提条件是要有前台的传感器权限
                return PermissionX.isGranted(context, Permission.BODY_SENSORS) &&
                        PermissionX.isGranted(context, Permission.BODY_SENSORS_BACKGROUND)
            }else if(AndroidVersion.isAndroid6){
                return PermissionX.isGranted(context, Permission.BODY_SENSORS)
            }else {
                return true
            }
        }else {
            return super.isPermissionGranted(context, permission)
        }
    }
}