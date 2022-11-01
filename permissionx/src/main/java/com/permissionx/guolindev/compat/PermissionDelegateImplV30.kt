package com.permissionx.guolindev.compat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.utils.PermissionUtils

@RequiresApi(api = AndroidVersion.ANDROID_11)
internal open class PermissionDelegateImplV30 : PermissionDelegateImplV29() {

    override
    fun isGrantedPermission(context: Context, permission: String?): Boolean {
        return if (PermissionUtils.equalsPermission(
                permission,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        ) {
            isGrantedManageStoragePermission
        } else super.isGrantedPermission(context, permission)
    }

    override
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        return if (PermissionUtils.equalsPermission(
                permission,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        ) {
            false
        } else super.isPermissionPermanentDenied(activity, permission)
    }

    override fun getPermissionIntent(context: Context, permission: String?): Intent {
        return if (PermissionUtils.equalsPermission(
                permission,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        ) {
            getManageStoragePermissionIntent(context)
        } else super.getPermissionIntent(context, permission)
    }

    companion object {
        /**
         * 是否有所有文件的管理权限
         */
        private val isGrantedManageStoragePermission: Boolean
            private get() = Environment.isExternalStorageManager()

        /**
         * 获取所有文件的管理权限设置界面意图
         */
        private fun getManageStoragePermissionIntent(context: Context): Intent {
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