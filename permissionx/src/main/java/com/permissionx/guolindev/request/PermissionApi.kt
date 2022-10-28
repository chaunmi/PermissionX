package com.permissionx.guolindev.request

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.permissionx.guolindev.utils.AndroidVersion
import com.permissionx.guolindev.utils.PermissionUtils
import com.permissionx.guolindev.compat.*
import com.permissionx.guolindev.compat.PermissionDelegateImplV28
import com.permissionx.guolindev.compat.PermissionDelegateImplV29
import com.permissionx.guolindev.compat.PermissionDelegateImplV30
import com.permissionx.guolindev.compat.PermissionDelegateImplV31
import com.permissionx.guolindev.compat.PermissionDelegateImplV33

internal object PermissionApi {
    private val DELEGATE: PermissionDelegate

    init {
        if (AndroidVersion.isAndroid13) {
            DELEGATE = PermissionDelegateImplV33()
        } else if (AndroidVersion.isAndroid12) {
            DELEGATE = PermissionDelegateImplV31()
        } else if (AndroidVersion.isAndroid11) {
            DELEGATE = PermissionDelegateImplV30()
        } else if (AndroidVersion.isAndroid10) {
            DELEGATE = PermissionDelegateImplV29()
        } else if (AndroidVersion.isAndroid9) {
            DELEGATE = PermissionDelegateImplV28()
        } else if (AndroidVersion.isAndroid8) {
            DELEGATE = PermissionDelegateImplV26()
        } else if (AndroidVersion.isAndroid6) {
            DELEGATE = PermissionDelegateImplV23()
        } else {
            DELEGATE = PermissionDelegateImplV14()
        }
    }

    /**
     * 判断某个权限是否授予
     */
    fun isGrantedPermission(context: Context, permission: String?): Boolean {
        return DELEGATE.isGrantedPermission(context, permission)
    }

    /**
     * 判断某个权限是否被永久拒绝
     */
    fun isPermissionPermanentDenied(activity: Activity, permission: String?): Boolean {
        return DELEGATE.isPermissionPermanentDenied(activity, permission)
    }

    /**
     * 获取权限设置页意图
     */
    fun getPermissionIntent(context: Context, permission: String?): Intent {
        return DELEGATE.getPermissionIntent(context, permission)
    }

    /**
     * 判断某个权限是否是特殊权限
     */
    fun isSpecialPermission(permission: String?): Boolean {
        return PermissionUtils.isSpecialPermission(permission)
    }

    /**
     * 判断某个权限集合是否包含特殊权限
     */
    fun containsSpecialPermission(permissions: List<String?>?): Boolean {
        if (permissions == null || permissions.isEmpty()) {
            return false
        }
        for (permission in permissions) {
            if (isSpecialPermission(permission)) {
                return true
            }
        }
        return false
    }

    /**
     * 判断某些权限是否全部被授予
     */
    fun isGrantedPermissions(context: Context, permissions: List<String?>?): Boolean {
        if (permissions == null || permissions.isEmpty()) {
            return false
        }
        for (permission in permissions) {
            if (!isGrantedPermission(context, permission)) {
                return false
            }
        }
        return true
    }

    /**
     * 获取已经授予的权限
     */
    fun getGrantedPermissions(context: Context, permissions: List<String>): List<String> {
        val grantedPermission: MutableList<String> = ArrayList(permissions.size)
        for (permission in permissions) {
            if (isGrantedPermission(context, permission)) {
                grantedPermission.add(permission)
            }
        }
        return grantedPermission
    }

    /**
     * 获取已经拒绝的权限
     */
    fun getDeniedPermissions(context: Context, permissions: List<String>): List<String> {
        val deniedPermission: MutableList<String> = ArrayList(permissions.size)
        for (permission in permissions) {
            if (!isGrantedPermission(context, permission)) {
                deniedPermission.add(permission)
            }
        }
        return deniedPermission
    }

    /**
     * 在权限组中检查是否有某个权限是否被永久拒绝
     *
     * @param activity              Activity对象
     * @param permissions            请求的权限
     */
    fun isPermissionPermanentDenied(activity: Activity, permissions: List<String?>): Boolean {
        for (permission in permissions) {
            if (isPermissionPermanentDenied(activity, permission)) {
                return true
            }
        }
        return false
    }

    /**
     * 获取没有授予的权限
     *
     * @param permissions           需要请求的权限组
     * @param grantResults          允许结果组
     */
    fun getDeniedPermissions(permissions: List<String>, grantResults: IntArray): List<String> {
        val deniedPermissions: MutableList<String> = ArrayList()
        for (i in grantResults.indices) {
            // 把没有授予过的权限加入到集合中
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i])
            }
        }
        return deniedPermissions
    }

    /**
     * 获取已授予的权限
     *
     * @param permissions       需要请求的权限组
     * @param grantResults      允许结果组
     */
    fun getGrantedPermissions(permissions: List<String>, grantResults: IntArray): List<String> {
        val grantedPermissions: MutableList<String> = ArrayList()
        for (i in grantResults.indices) {
            // 把授予过的权限加入到集合中
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i])
            }
        }
        return grantedPermissions
    }
}