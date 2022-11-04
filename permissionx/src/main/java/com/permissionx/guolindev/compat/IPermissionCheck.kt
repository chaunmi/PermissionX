package com.permissionx.guolindev.compat

import android.content.Context

interface IPermissionCheck {
    fun isPermissionGranted(context: Context, permission: String): Boolean
}