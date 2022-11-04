package com.permissionx.guolindev.compat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

/**
 * TODO 待验证，Android11 是否开启了强制分区模式
 */

class ExternalStoragePermissionCheck: BasePermissionCheck() {
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if(PermissionUtils.equalsPermission(permission, Permission.MANAGE_EXTERNAL_STORAGE)) {
            //MANAGE_EXTERNAL_STORAGE为R新增权限
            if(AndroidVersion.isAndroid11) {
                return Environment.isExternalStorageManager()
            }else {
                val readExternalPermission = PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                val writeExternalPermission = PermissionX.isGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                val hasExternalPermission = readExternalPermission && writeExternalPermission
                if(AndroidVersion.isAndroid10) {
                    return isUseDeprecationExternalStorage && hasExternalPermission
                }
                return hasExternalPermission
            }
        }else if(PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_IMAGES)
                ||  PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_AUDIO)
                || PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_VIDEO)){
            //为Android 13新增权限
            if(AndroidVersion.isAndroid13) {
                return PermissionX.isGranted(context, permission)
            }else {
                if(AndroidVersion.isAndroid11) {
                    return Environment.isExternalStorageManager() || PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                }else if(AndroidVersion.isAndroid10) {
                    return isUseDeprecationExternalStorage && PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                }else if(AndroidVersion.isAndroid6){
                    return PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                }else {
                    return true
                }
            }
        }else {
            return super.isPermissionGranted(context, permission)
        }
    }

    companion object {
        /**
         * 是否采用的是非分区存储的模式
         */
         val isUseDeprecationExternalStorage: Boolean
            @RequiresApi(Build.VERSION_CODES.Q) get() = Environment.isExternalStorageLegacy()

        /**
         * 获取所有文件的管理权限设置界面意图
         */
         fun getManageStoragePermissionIntent(context: Context): Intent {
            var intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = PermissionUtils.getPackageNameUri(context)
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
            if (!PermissionUtils.areActivityIntent(context, intent)) {
                intent = PermissionUtils.getApplicationDetailsIntent(context)
            }
            return intent
        }
    }

}