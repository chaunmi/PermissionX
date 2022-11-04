/*
 * Copyright (C)  guolin, PermissionX Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.permissionx.guolindev.request

import android.Manifest
import android.os.Build
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX

/**
 * Implementation for request ACCESS_BACKGROUND_LOCATION permission.
 * @author guolin
 * @since 2020/6/10
 *
 * Q 新增的权限，低版本上无此权限
 * https://developer.android.google.cn/about/versions/10/privacy/changes#app-access-device-location
 * https://developer.android.com/about/versions/11/privacy/location#background-location
 *
 * https://developer.android.com/training/location/permissions?hl=zh-cn
 * 您应该执行递增位置信息请求。如果您的应用以 Android 11 或更高版本为目标平台，系统会强制执行此最佳做法。
 * 如果您同时请求在前台访问位置信息的权限和在后台访问位置信息的权限，系统会忽略该请求，且不会向您的应用授予其中的任一权限。
 *
 * ACCESS_BACKGROUND_LOCATION 和其他非定位权限定位掺杂在一起申请，在 Android 11 上会出现不申请直接被拒绝的情况。
 *
 * 因此这里需要单独拎出来作为特殊权限处理，单独申请，但其权限申请和运行时权限申请是一样的
 */
internal class RequestBackgroundLocationPermission internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestBackgroundLocationPermission()) {
            val accessFindLocationGranted = PermissionX.isGranted(pb.activity, Manifest.permission.ACCESS_FINE_LOCATION)
            val accessCoarseLocationGranted = PermissionX.isGranted(pb.activity, Manifest.permission.ACCESS_COARSE_LOCATION)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // If app runs under Android Q, there's no ACCESS_BACKGROUND_LOCATION permissions.
                // We remove it from request list, but will append it to the request callback as denied permission.
                pb.specialPermissions.remove(Permission.ACCESS_BACKGROUND_LOCATION)
                //对于Q以下的系统，如果申请了位置权限，则默认授予后台定位权限
                if(accessCoarseLocationGranted || accessFindLocationGranted) {
                    pb.grantedPermissions.add(Permission.ACCESS_BACKGROUND_LOCATION)
                }else {
                    pb.permissionsWontRequest.add(Permission.ACCESS_BACKGROUND_LOCATION)
                }
                finish()
                return
            }
            if (PermissionX.isGranted(pb.activity, Permission.ACCESS_BACKGROUND_LOCATION)) {
                // ACCESS_BACKGROUND_LOCATION has already granted, we can finish this task now.
                finish()
                return
            }

            //仅当有访问位置权限时才需要去申请后台访问权限，不然此权限申请无意义
            if (accessFindLocationGranted || accessCoarseLocationGranted) {
                if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                    val requestList = mutableListOf(Permission.ACCESS_BACKGROUND_LOCATION)
                    if (pb.explainReasonCallbackWithBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(explainScope, requestList, true)
                    } else {
                        pb.explainReasonCallback!!.onExplainReason(explainScope, requestList)
                    }
                } else {
                    // No implementation of explainReasonCallback, so we have to request ACCESS_BACKGROUND_LOCATION without explanation.
                    requestAgain(emptyList())
                }
                return
            }
        }
        // Shouldn't request ACCESS_BACKGROUND_LOCATION at this time, so we call finish() to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // Don't care what the permissions param is, always request ACCESS_BACKGROUND_LOCATION.
        pb.requestAccessBackgroundLocationPermissionNow(this)
    }
}