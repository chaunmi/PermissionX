package com.permissionx.guolindev.compat

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

class PictureInPicturePermissionCheck: BasePermissionCheck() {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.PICTURE_IN_PICTURE)) {
            return isGrantedPictureInPicturePermission(context)
        }
        return super.isPermissionGranted(context, permission)
    }

    companion object {
        /**
         * 是否有画中画权限
         */
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        private fun isGrantedPictureInPicturePermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode: Int = if (AndroidVersion.isAndroid10) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    context.applicationInfo.uid, context.packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    context.applicationInfo.uid, context.packageName
                )
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }

        /**
         * 获取画中画权限设置界面意图
         */
        private fun getPictureInPicturePermissionIntent(context: Context): Intent {
            var intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS")
            intent.data = PermissionUtils.getPackageNameUri(context)
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }
    }
}