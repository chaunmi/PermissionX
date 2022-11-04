package com.permissionx.guolindev.compat

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

class NotificationPermissionCheck: BasePermissionCheck() {

    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.POST_NOTIFICATIONS)) {
            if(AndroidVersion.isAndroid13) {
                // Android 13起通知栏权限当运行时权限处理
                return PermissionX.isGranted(context, Permission.POST_NOTIFICATIONS)
            }else {
                return areNotificationsEnabled(context)
            }
        }else {
            return super.isPermissionGranted(context, permission)
        }
    }

    companion object {
        fun areNotificationsEnabled(context: Context): Boolean {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

        /**
         * 获取通知栏权限设置界面意图
         */
        private fun getNotifyPermissionIntent(context: Context): Intent {
            var intent: Intent? = null
            if (AndroidVersion.isAndroid8) {
                intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            if (intent == null || !PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }
    }
}