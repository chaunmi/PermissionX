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
 * @since 2022/8/26
 * https://developer.android.google.cn/about/versions/13/behavior-changes-13#body-sensors-background-permission
 * 如果您的应用以 Android 13 为目标平台，并且在后台运行时需要访问身体传感器信息，那么除了现有的 BODY_SENSORS 权限外，您还必须声明新的 BODY_SENSORS_BACKGROUND 权限。
 *
 * 这里主要是多了一个BODY_SENSORS的处理，因此单独独立出来，但其权限申请和运行时权限申请是一样的
 */
internal class RequestBodySensorsBackgroundPermission internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestBodySensorsBackgroundPermission()) {
            val bodySensorGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                PermissionX.isGranted(pb.activity, Manifest.permission.BODY_SENSORS)
            } else {
                false
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // If app runs under Android T, there's no BODY_SENSORS_BACKGROUND permissions.
                // We remove it from request list, but will append it to the request callback as denied permission.
                pb.specialPermissions.remove(Permission.BODY_SENSORS_BACKGROUND)
                if(bodySensorGranted) {
                    pb.grantedPermissions.add(Permission.BODY_SENSORS_BACKGROUND)
                }else {
                    pb.permissionsWontRequest.add(Permission.BODY_SENSORS_BACKGROUND)
                }
                finish()
                return
            }
            if (PermissionX.isGranted(pb.activity, Permission.BODY_SENSORS_BACKGROUND)) {
                // BODY_SENSORS_BACKGROUND has already granted, we can finish this task now.
                finish()
                return
            }
            if (bodySensorGranted) {
                if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                    val requestList = mutableListOf(Permission.BODY_SENSORS_BACKGROUND)
                    if (pb.explainReasonCallbackWithBeforeParam != null) {
                        // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                        pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(explainScope, requestList, true)
                    } else {
                        pb.explainReasonCallback!!.onExplainReason(explainScope, requestList)
                    }
                } else {
                    // No implementation of explainReasonCallback, so we have to request BODY_SENSORS_BACKGROUND without explanation.
                    requestAgain(emptyList())
                }
                return
            }
        }
        // Shouldn't request BODY_SENSORS_BACKGROUND at this time, so we call finish() to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // Don't care what the permissions param is, always request BODY_SENSORS_BACKGROUND.
        pb.requestBodySensorsBackgroundPermissionNow(this)
    }

}