package com.permissionx.guolindev.compat

import android.app.Activity
import android.content.Context
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils

@RequiresApi(api = AndroidVersion.ANDROID_13)
internal class PermissionDelegateImplV33 : PermissionDelegateImplV31() {
    override
    fun isGrantedPermission(context: Context, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.BODY_SENSORS_BACKGROUND)) {
            // 有后台传感器权限的前提条件是要有前台的传感器权限
            return PermissionUtils.checkSelfPermission(context, Permission.BODY_SENSORS) &&
                    PermissionUtils.checkSelfPermission(context, Permission.BODY_SENSORS_BACKGROUND)
        }
        return if (PermissionUtils.equalsPermission(permission, Permission.POST_NOTIFICATIONS) ||
            PermissionUtils.equalsPermission(permission, Permission.NEARBY_WIFI_DEVICES) ||
            PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_IMAGES) ||
            PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_VIDEO) ||
            PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_AUDIO)
        ) {
            PermissionUtils.checkSelfPermission(context, permission)
        } else super.isGrantedPermission(context, permission)
    }

    override
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.BODY_SENSORS_BACKGROUND)) {
            return if (!PermissionUtils.checkSelfPermission(activity, Permission.BODY_SENSORS)) {
                !PermissionUtils.shouldShowRequestPermissionRationale(
                    activity,
                    Permission.BODY_SENSORS
                )
            } else !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        }
        return if (PermissionUtils.equalsPermission(permission, Permission.POST_NOTIFICATIONS) ||
            PermissionUtils.equalsPermission(permission, Permission.NEARBY_WIFI_DEVICES) ||
            PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_IMAGES) ||
            PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_VIDEO) ||
            PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_AUDIO)
        ) {
            !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        } else super.isPermissionPermanentDenied(activity!!, permission)
    }
}