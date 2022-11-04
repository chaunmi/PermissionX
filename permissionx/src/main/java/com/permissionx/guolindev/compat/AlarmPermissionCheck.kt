package com.permissionx.guolindev.compat

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

/**
 * 闹钟权限（特殊权限，Android 12 新增的权限）
 *
 * 需要注意的是：这个权限和其他特殊权限不同的是，默认已经是授予状态，用户也可以手动撤销授权
 * 官方文档介绍：https://developer.android.google.cn/about/versions/12/behavior-changes-12?hl=zh_cn#exact-alarm-permission
 */
class AlarmPermissionCheck: BasePermissionCheck() {
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if(PermissionUtils.equalsPermission(permission, Permission.SCHEDULE_EXACT_ALARM)) {
            return isGrantedAlarmPermission(context)
        }
        return super.isPermissionGranted(context, permission)
    }

    companion object {
        /**
         * 是否有闹钟权限
         */
        private fun isGrantedAlarmPermission(context: Context): Boolean {
            if(AndroidVersion.isAndroid12) {
                return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
            }else {
                return true
            }
        }

        /**
         * 获取闹钟权限设置界面意图
         */
        private fun getAlarmPermissionIntent(context: Context): Intent {
            var intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            intent.data = PermissionUtils.getPackageNameUri(context)
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }
    }
}