package com.permissionx.guolindev.request

import android.app.Activity
import android.content.Context
import android.content.Intent

interface PermissionDelegate {

    /**
     * 判断某个权限是否授予了
     */
    fun isGrantedPermission(context: Context, permission: String?): Boolean

    /**
     * 判断某个权限是否永久拒绝了
     */
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean

    /**
     * 获取权限设置页的意图
     */
    fun getPermissionIntent(context: Context, permission: String?): Intent
}