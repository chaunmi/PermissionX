package com.permissionx.guolindev.compat

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils

@RequiresApi(api = AndroidVersion.ANDROID_12)
internal open class PermissionDelegateImplV31 : PermissionDelegateImplV30() {
    override fun isGrantedPermission(context: Context, permission: String?): Boolean {
        // 检测闹钟权限
        if (PermissionUtils.equalsPermission(permission, Permission.SCHEDULE_EXACT_ALARM)) {
            return isGrantedAlarmPermission(context)
        }
        return if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_SCAN) ||
            PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_CONNECT) ||
            PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_ADVERTISE)
        ) {
            PermissionUtils.checkSelfPermission(context, permission)
        } else super.isGrantedPermission(context, permission)
    }

    override fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.SCHEDULE_EXACT_ALARM)) {
            return false
        }
        if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_SCAN) ||
            PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_CONNECT) ||
            PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_ADVERTISE)
        ) {
            return !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        }
        return if (activity.applicationInfo.targetSdkVersion >= AndroidVersion.ANDROID_12 &&
            PermissionUtils.equalsPermission(permission, Permission.ACCESS_BACKGROUND_LOCATION)
        ) {
            if (!PermissionUtils.checkSelfPermission(activity, Permission.ACCESS_FINE_LOCATION) &&
                !PermissionUtils.checkSelfPermission(activity, Permission.ACCESS_COARSE_LOCATION)
            ) {
                !PermissionUtils.shouldShowRequestPermissionRationale(
                    activity,
                    Permission.ACCESS_FINE_LOCATION
                ) &&
                        !PermissionUtils.shouldShowRequestPermissionRationale(
                            activity,
                            Permission.ACCESS_COARSE_LOCATION
                        )
            } else !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        } else super.isPermissionPermanentDenied(activity, permission)
    }

    override fun getPermissionIntent(context: Context, permission: String?): Intent {
        return if (PermissionUtils.equalsPermission(permission, Permission.SCHEDULE_EXACT_ALARM)) {
            getAlarmPermissionIntent(context)
        } else super.getPermissionIntent(context, permission)
    }

    companion object {
        /**
         * 是否有闹钟权限
         */
        private fun isGrantedAlarmPermission(context: Context): Boolean {
            return context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
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