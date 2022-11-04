package com.permissionx.guolindev.compat

import android.content.Context
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

/**
 * 读取手机号码（Android 8.0 新增的权限）
 *
 * 为了兼容 Android 8.0 以下版本，需要在清单文件中注册 [.READ_PHONE_STATE] 权限
 */
class ReadPhoneNumbersPermissionCheck : BasePermissionCheck(){

    override fun isPermissionGranted(context: Context, permission: String): Boolean {

        if (PermissionUtils.equalsPermission(permission, Permission.READ_PHONE_NUMBERS)) {
            if(!AndroidVersion.isAndroid8) {
                return PermissionX.isGranted(context, Permission.READ_PHONE_STATE)
            }
            return PermissionX.isGranted(context, Permission.READ_PHONE_NUMBERS)
        }

        return super.isPermissionGranted(context, permission)
    }

}