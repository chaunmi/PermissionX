package com.permissionx.guolindev.compat

import android.app.Activity
import android.content.Context
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils

@RequiresApi(api = AndroidVersion.ANDROID_9)
internal open class PermissionDelegateImplV28 : PermissionDelegateImplV26() {
    override
    fun isGrantedPermission(context: Context, permission: String?): Boolean {
        return if (PermissionUtils.equalsPermission(permission, Permission.ACCEPT_HANDOVER)) {
            PermissionUtils.checkSelfPermission(context, permission)
        } else super.isGrantedPermission(context, permission)
    }

    override
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        return if (PermissionUtils.equalsPermission(permission, Permission.ACCEPT_HANDOVER)) {
            !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        } else super.isPermissionPermanentDenied(activity, permission)
    }
}