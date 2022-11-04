package com.permissionx.guolindev.compat

import android.content.Context

abstract class BasePermissionCheck: IPermissionCheck {
    internal var next: IPermissionCheck? = null

    override fun isPermissionGranted(context: Context, permission: String): Boolean {
       return next?.isPermissionGranted(context, permission)?: false
    }
}