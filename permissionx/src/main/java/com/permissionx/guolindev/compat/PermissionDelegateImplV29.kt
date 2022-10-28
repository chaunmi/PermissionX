package com.permissionx.guolindev.compat

import android.app.Activity
import android.content.Context
import android.os.Environment
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils

@RequiresApi(api = AndroidVersion.ANDROID_10)
internal open class PermissionDelegateImplV29 : PermissionDelegateImplV28() {

    override fun isGrantedPermission(context: Context, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_MEDIA_LOCATION)) {
            return hasReadStoragePermission(context) &&
                    PermissionUtils.checkSelfPermission(context, Permission.ACCESS_MEDIA_LOCATION)
        }
        if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_BACKGROUND_LOCATION) ||
            PermissionUtils.equalsPermission(permission, Permission.ACTIVITY_RECOGNITION)
        ) {
            return PermissionUtils.checkSelfPermission(context, permission)
        }

        // 向下兼容 Android 11 新权限
        if (!AndroidVersion.isAndroid11) {
            if (PermissionUtils.equalsPermission(permission, Permission.MANAGE_EXTERNAL_STORAGE)) {
                // 这个是 Android 10 上面的历史遗留问题，假设申请的是 MANAGE_EXTERNAL_STORAGE 权限
                // 必须要在 AndroidManifest.xml 中注册 android:requestLegacyExternalStorage="true"
                if (!isUseDeprecationExternalStorage) {
                    return false
                }
            }
        }
        return super.isGrantedPermission(context, permission)
    }

    override fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_BACKGROUND_LOCATION)) {
            return if (!PermissionUtils.checkSelfPermission(
                    activity,
                    Permission.ACCESS_FINE_LOCATION
                )
            ) {
                !PermissionUtils.shouldShowRequestPermissionRationale(
                    activity,
                    Permission.ACCESS_FINE_LOCATION
                )
            } else !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        }
        if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_MEDIA_LOCATION)) {
            return hasReadStoragePermission(activity) &&
                    !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        }
        if (PermissionUtils.equalsPermission(permission, Permission.ACTIVITY_RECOGNITION)) {
            return !PermissionUtils.checkSelfPermission(activity, permission) &&
                    !PermissionUtils.shouldShowRequestPermissionRationale(activity, permission)
        }

        // 向下兼容 Android 11 新权限
        if (!AndroidVersion.isAndroid11) {
            if (PermissionUtils.equalsPermission(permission, Permission.MANAGE_EXTERNAL_STORAGE)) {
                // 处理 Android 10 上面的历史遗留问题
                if (!isUseDeprecationExternalStorage) {
                    return true
                }
            }
        }
        return super.isPermissionPermanentDenied(activity, permission)
    }

    /**
     * 是否有读取文件的权限
     */
    private fun hasReadStoragePermission(context: Context): Boolean {
        if (AndroidVersion.isAndroid13 && AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_13) {
            return PermissionUtils.checkSelfPermission(context, Permission.READ_MEDIA_IMAGES) ||
                    isGrantedPermission(context, Permission.MANAGE_EXTERNAL_STORAGE)
        }
        return if (AndroidVersion.isAndroid11 && AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_11) {
            PermissionUtils.checkSelfPermission(context, Permission.READ_EXTERNAL_STORAGE) ||
                    isGrantedPermission(context, Permission.MANAGE_EXTERNAL_STORAGE)
        } else PermissionUtils.checkSelfPermission(context, Permission.READ_EXTERNAL_STORAGE)
    }

    companion object {
        /**
         * 是否采用的是非分区存储的模式
         */
        private val isUseDeprecationExternalStorage: Boolean
            private get() = Environment.isExternalStorageLegacy()
    }
}