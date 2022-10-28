package com.permissionx.guolindev.compat

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils

@RequiresApi(api = AndroidVersion.ANDROID_8)
internal open class PermissionDelegateImplV26 : PermissionDelegateImplV23() {

    override fun isGrantedPermission(context: Context, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.REQUEST_INSTALL_PACKAGES)) {
            return isGrantedInstallPermission(context)
        }
        if (PermissionUtils.equalsPermission(permission, Permission.PICTURE_IN_PICTURE)) {
            return isGrantedPictureInPicturePermission(context)
        }
        return if (PermissionUtils.equalsPermission(permission, Permission.READ_PHONE_NUMBERS) ||
            PermissionUtils.equalsPermission(permission, Permission.ANSWER_PHONE_CALLS)
        ) {
            PermissionUtils.checkSelfPermission(context, permission)
        } else super.isGrantedPermission(context, permission)
    }

    override
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.REQUEST_INSTALL_PACKAGES)) {
            return false
        }
        if (PermissionUtils.equalsPermission(permission, Permission.PICTURE_IN_PICTURE)) {
            return false
        }
        return if (PermissionUtils.equalsPermission(permission, Permission.READ_PHONE_NUMBERS) ||
            PermissionUtils.equalsPermission(permission, Permission.ANSWER_PHONE_CALLS)
        ) {
            !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        } else super.isPermissionPermanentDenied(activity!!, permission)
    }

    override fun getPermissionIntent(context: Context, permission: String?): Intent {
        if (PermissionUtils.equalsPermission(permission, Permission.REQUEST_INSTALL_PACKAGES)) {
            return getInstallPermissionIntent(context)
        }
        return if (PermissionUtils.equalsPermission(permission, Permission.PICTURE_IN_PICTURE)) {
            getPictureInPicturePermissionIntent(context)
        } else super.getPermissionIntent(context, permission)
    }

    companion object {
        /**
         * 是否有安装权限
         */
        private fun isGrantedInstallPermission(context: Context): Boolean {
            return context.packageManager.canRequestPackageInstalls()
        }

        /**
         * 获取安装权限设置界面意图
         */
        private fun getInstallPermissionIntent(context: Context): Intent {
            var intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = PermissionUtils.getPackageNameUri(context)
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }

        /**
         * 是否有画中画权限
         */
        private fun isGrantedPictureInPicturePermission(context: Context): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode: Int
            mode = if (AndroidVersion.isAndroid10) {
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