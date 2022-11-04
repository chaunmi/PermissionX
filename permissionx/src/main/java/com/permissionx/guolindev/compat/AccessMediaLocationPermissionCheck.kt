package com.permissionx.guolindev.compat

import android.content.Context
import android.os.Environment
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils


/**
 * 读取照片中的地理位置（Android 10.0 新增的权限）
 *
 * 需要注意的是：如果这个权限申请成功了但是不能正常读取照片的地理信息，那么需要先申请存储权限：
 *
 * 如果项目 targetSdkVersion <= 29 需要申请 [Permission.Group.STORAGE]
 * 如果项目 targetSdkVersion >= 30 需要申请 [Permission.MANAGE_EXTERNAL_STORAGE]
 *
 * TODO 待验证，是否 11 开启强制分区
 */
class AccessMediaLocationPermissionCheck: BasePermissionCheck() {
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_MEDIA_LOCATION)) {
            return hasReadStoragePermission(context) &&
                    PermissionX.isGranted(context, Permission.ACCESS_MEDIA_LOCATION)
        }
        return super.isPermissionGranted(context, permission)
    }

    /**
     * 是否有读取文件的权限
     */
    private fun hasReadStoragePermission(context: Context): Boolean {
        return if (AndroidVersion.isAndroid13 && AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_13) {
            PermissionX.isGranted(context, Permission.READ_MEDIA_IMAGES) ||
                    Environment.isExternalStorageManager()
        }else if (AndroidVersion.isAndroid11 && AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_11) {
            PermissionX.isGranted(context, Permission.READ_EXTERNAL_STORAGE) ||
                    Environment.isExternalStorageManager()
        }else if(AndroidVersion.isAndroid10 && AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_10) {
            ExternalStoragePermissionCheck.isUseDeprecationExternalStorage &&
                    PermissionX.isGranted(context, Permission.READ_EXTERNAL_STORAGE)
        } else PermissionX.isGranted(context, Permission.READ_EXTERNAL_STORAGE)
    }
}