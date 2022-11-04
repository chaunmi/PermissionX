package com.permissionx.guolindev.compat

import android.Manifest
import android.content.Context
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils

/**
 * 在后台获取位置（Android 10.0 新增的权限）
 *
 * 需要注意的是：
 * 1. 一旦你申请了该权限，在授权的时候，需要选择《始终允许》，而不能选择《仅在使用中允许》
 * 2. 如果你的 App 只在前台状态下使用定位功能，请不要申请该权限（后台定位权限）
 */
class BackgroundLocationPermissionCheck : BasePermissionCheck(){
    override fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_BACKGROUND_LOCATION)) {
            if(AndroidVersion.isAndroid10) {
                // 有后台传感器权限的前提条件是要有前台的传感器权限
                return PermissionX.isGranted(context, Permission.ACCESS_BACKGROUND_LOCATION)
            }else {
                val accessFindLocationGranted = PermissionX.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)
                val accessCoarseLocationGranted = PermissionX.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                return accessCoarseLocationGranted || accessFindLocationGranted
            }
        }else {
            return super.isPermissionGranted(context, permission)
        }
    }
}