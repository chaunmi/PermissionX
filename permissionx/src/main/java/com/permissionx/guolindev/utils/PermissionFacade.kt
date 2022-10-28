package com.permissionx.guolindev.utils

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import com.permissionx.guolindev.request.PermissionApi

object PermissionFacade {

    /**
     * 判断一个或多个权限是否全部授予了
     */
    fun isGranted(context: Context, vararg permissions: String): Boolean {
        return isGranted(context, listOf(*permissions))
    }

    fun isGranted(context: Context, permissions: List<String>): Boolean {
        return PermissionApi.isGrantedPermissions(context, permissions)
    }

    /**
     * 获取没有授予的权限
     */
    fun getDenied(context: Context, vararg permissions: String): List<String> {
        return getDenied(context, listOf(*permissions))
    }

    fun getDenied(context: Context, permissions: List<String>): List<String> {
        return PermissionApi.getDeniedPermissions(context, permissions)
    }

    /**
     * 判断某个权限是否为特殊权限
     */
    fun isSpecial(permission: String?): Boolean {
        return PermissionApi.isSpecialPermission(permission)
    }

    /**
     * 判断权限列表中是否包含特殊权限
     */
    fun containsSpecial(vararg permissions: String): Boolean {
        return containsSpecial(listOf(*permissions))
    }

    fun containsSpecial(permissions: List<String>): Boolean {
        return PermissionApi.containsSpecialPermission(permissions)
    }

    /**
     * 判断一个或多个权限是否被永久拒绝了
     *
     * （注意不能在请求权限之前调用，应该在 [OnPermissionCallback.onDenied] 方法中调用）
     */
    fun isPermanentDenied(activity: Activity, vararg permissions: String): Boolean {
        return isPermanentDenied(activity, listOf(*permissions))
    }

    fun isPermanentDenied(activity: Activity, permissions: List<String>): Boolean {
        return PermissionApi.isPermissionPermanentDenied(activity, permissions)
    }

    /**
     * 跳转到应用权限设置页
     *
     * @param permissions  没有授予或者被拒绝的权限组
     */

    fun startPermissionActivity(context: Context, vararg permissions: String) {
        startPermissionActivity(context, listOf(*permissions))
    }

    fun startPermissionActivity(context: Context, permissions: List<String>?) {
        val activity = PermissionUtils.findActivity(context)
        val intent = PermissionUtils.getSmartPermissionIntent(context, permissions)
        if(activity != null) {
            activity.startActivity(intent)
        }else {
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}