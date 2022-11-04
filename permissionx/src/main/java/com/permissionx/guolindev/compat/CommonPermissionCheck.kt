package com.permissionx.guolindev.compat

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

class CommonPermissionCheck : BasePermissionCheck(){

    override fun isPermissionGranted(context: Context, permission: String): Boolean {

        // 检测悬浮窗权限
        if (PermissionUtils.equalsPermission(permission, Permission.SYSTEM_ALERT_WINDOW)) {
            return isGrantedAlertWindowPermission(context)
        }

        // 检测系统设置权限
        if (PermissionUtils.equalsPermission(permission, Permission.WRITE_SETTINGS)) {
            return isGrantedWriteSettingPermission(context)
        }

        // 检测 VPN 权限
        if (PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE)) {
            return isGrantedVpnPermission(context)
        }

        return PermissionX.isGranted(context, permission)
    }

    companion object {
        /**
         * 是否授予了悬浮窗权限
         */
        private fun isGrantedAlertWindowPermission(context: Context): Boolean {
            return if(AndroidVersion.isAndroid6) {
                Settings.canDrawOverlays(context)
            }else {
                true
            }
        }

        /**
         * 获取悬浮窗权限设置界面意图
         */
        private fun getAlertWindowPermissionIntent(context: Context): Intent {
            var intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            // 在 Android 11 加包名跳转也是没有效果的，官方文档链接：
            // https://developer.android.google.cn/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION
            intent.data = PermissionUtils.getPackageNameUri(context)
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }

        /**
         * 是否有系统设置权限
         */
        private fun isGrantedWriteSettingPermission(context: Context): Boolean {
            return if (AndroidVersion.isAndroid6) {
                Settings.System.canWrite(context)
            } else true
        }

        /**
         * 获取系统设置权限界面意图
         */
        private fun getWriteSettingPermissionIntent(context: Context): Intent {
            var intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = PermissionUtils.getPackageNameUri(context)
            if (!PermissionUtils.areActivityIntent(context, intent)) {
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