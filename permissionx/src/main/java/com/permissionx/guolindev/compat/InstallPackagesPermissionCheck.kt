package com.permissionx.guolindev.compat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

/**
 * 安装应用权限（特殊权限，Android 8.0 新增的权限）
 */
class InstallPackagesPermissionCheck: BasePermissionCheck() {
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if(PermissionUtils.equalsPermission(permission, Permission.REQUEST_INSTALL_PACKAGES)) {
            if(AndroidVersion.isAndroid8) {
                return isGrantedInstallPermission(context)
            }else {
                return true
            }
        }
        return super.isPermissionGranted(context, permission)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
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
    }
}