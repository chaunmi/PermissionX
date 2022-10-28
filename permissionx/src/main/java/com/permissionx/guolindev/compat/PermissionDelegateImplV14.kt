package com.permissionx.guolindev.compat

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils
import com.permissionx.guolindev.request.PermissionDelegate

@RequiresApi(api = AndroidVersion.ANDROID_4_0)
internal open class PermissionDelegateImplV14 : PermissionDelegate {

    override
    fun isGrantedPermission(context: Context, permission: String?): Boolean {
        // 检测通知栏权限
        if (PermissionUtils.equalsPermission(permission, Permission.NOTIFICATION_SERVICE)) {
            return isGrantedNotifyPermission(context)
        }

        // 检测获取使用统计权限
        if (PermissionUtils.equalsPermission(permission, Permission.PACKAGE_USAGE_STATS)) {
            return isGrantedPackagePermission(context)
        }

        // 检测通知栏监听权限
        if (PermissionUtils.equalsPermission(
                permission,
                Permission.BIND_NOTIFICATION_LISTENER_SERVICE
            )
        ) {
            return isGrantedNotificationListenerPermission(context)
        }

        // 检测 VPN 权限
        if (PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE)) {
            return isGrantedVpnPermission(context)
        }

        /* ---------------------------------------------------------------------------------------- */

        // 向下兼容 Android 13 新权限
        if (!AndroidVersion.isAndroid13) {
            if (PermissionUtils.equalsPermission(permission, Permission.POST_NOTIFICATIONS)) {
                return isGrantedNotifyPermission(context)
            }
        }

        /* ---------------------------------------------------------------------------------------- */
        return true
    }

    override
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        return false
    }

    override
    fun getPermissionIntent(context: Context, permission: String?): Intent {
        if (PermissionUtils.equalsPermission(permission, Permission.NOTIFICATION_SERVICE)) {
            return getNotifyPermissionIntent(context)
        }
        if (PermissionUtils.equalsPermission(permission, Permission.PACKAGE_USAGE_STATS)) {
            return getPackagePermissionIntent(context)
        }
        if (PermissionUtils.equalsPermission(
                permission,
                Permission.BIND_NOTIFICATION_LISTENER_SERVICE
            )
        ) {
            return getNotificationListenerIntent(context)
        }
        if (PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE)) {
            return getVpnPermissionIntent(context)
        }

        /* ---------------------------------------------------------------------------------------- */

        // 向下兼容 Android 13 新权限
        if (!AndroidVersion.isAndroid13) {
            if (PermissionUtils.equalsPermission(permission, Permission.POST_NOTIFICATIONS)) {
                return getNotifyPermissionIntent(context)
            }
        }

        /* ---------------------------------------------------------------------------------------- */return PermissionUtils.getApplicationDetailsIntent(
            context
        )
    }

    companion object {
        /**
         * 是否有通知栏权限
         */
        private fun isGrantedNotifyPermission(context: Context): Boolean {
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
                //intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            }
            if (intent == null || !PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }

        /**
         * 是否通知栏监听的权限
         */
        private fun isGrantedNotificationListenerPermission(context: Context): Boolean {
            if (AndroidVersion.isAndroid4_3) {
                val packageNames: Set<String> =
                    NotificationManagerCompat.getEnabledListenerPackages(context)
                return packageNames.contains(context.packageName)
            }
            return true
        }

        /**
         * 获取通知监听设置界面意图
         */
        private fun getNotificationListenerIntent(context: Context): Intent {
            var intent: Intent
            intent = if (AndroidVersion.isAndroid5_1) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            } else {
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            }
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }

        /**
         * 是否有使用统计权限
         */
        private fun isGrantedPackagePermission(context: Context): Boolean {
            if (AndroidVersion.isAndroid5) {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode: Int
                mode = if (AndroidVersion.isAndroid10) {
                    appOps.unsafeCheckOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        context.applicationInfo.uid, context.packageName
                    )
                } else {
                    appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        context.applicationInfo.uid, context.packageName
                    )
                }
                return mode == AppOpsManager.MODE_ALLOWED
            }
            return true
        }

        /**
         * 获取使用统计权限设置界面意图
         */
        private fun getPackagePermissionIntent(context: Context): Intent {
            var intent: Intent? = null
            if (AndroidVersion.isAndroid5) {
                intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                if (AndroidVersion.isAndroid10) {
                    // 经过测试，只有在 Android 10 及以上加包名才有效果
                    // 如果在 Android 10 以下加包名会导致无法跳转
                    intent.data = PermissionUtils.getPackageNameUri(context)
                }
            }
            if (intent == null || !PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }

        /**
         * 是否有 VPN 权限
         */
        private fun isGrantedVpnPermission(context: Context): Boolean {
            return VpnService.prepare(context) == null
        }

        /**
         * 获取 VPN 权限设置界面意图
         */
        private fun getVpnPermissionIntent(context: Context): Intent {
            var intent = VpnService.prepare(context)
            if (intent == null || !PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }
    }
}