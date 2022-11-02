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
import android.os.Environment
import android.provider.Settings
import com.permissionx.guolindev.PermissionX
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Define a BaseTask to implement the duplicate logic codes. No need to implement them in every task.
 *
 * @author guolin
 * @since 2020/6/10
 */
internal abstract class BaseTask(@JvmField var pb: PermissionBuilder) : ChainTask {
    /**
     * Point to the next task. When this task finish will run next task. If there's no next task, the request process end.
     */
    @JvmField
    var next: ChainTask? = null

    /**
     * Provide specific scopes for explainReasonCallback for specific functions to call.
     */
    private var explainReasonScope = ExplainScope(pb, this)

    /**
     * Provide specific scopes for forwardToSettingsCallback for specific functions to call.
     */
    private var forwardToSettingsScope = ForwardScope(pb, this)

    override fun getExplainScope() = explainReasonScope

    override fun getForwardScope() = forwardToSettingsScope

    override fun finish() {
        // If there's next task, then run it.
        next?.request() ?: run {
            // If there's no next task, finish the request process and notify the result
            val deniedSet: MutableSet<String> = HashSet()
            deniedSet.addAll(pb.deniedPermissions)
            deniedSet.addAll(pb.permanentDeniedPermissions)
            deniedSet.addAll(pb.permissionsWontRequest)

            /**
             * 最后再check一下是否已授权
             * TODO 这里逻辑可以优化以下，针对低版本的情况，比如MANAGE_EXTERNAL_STORAGE和REQUEST_INSTALL_PACKAGES的处理
             */

            if (pb.shouldRequestBackgroundLocationPermission()) {
                if (PermissionX.isGranted(pb.activity, RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)) {
                    pb.grantedPermissions.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                    deniedSet.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    deniedSet.add(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                    pb.grantedPermissions.remove(RequestBackgroundLocationPermission.ACCESS_BACKGROUND_LOCATION)
                }
            }

            /**
             * 针对低于M的版本已做了处理，具体详见
             * @see RequestSystemAlertWindowPermission
             * 由于canDrawOverlays是大于M版本才有的api，因此需要限制到 M才判断，M以下默认授予权限
             */

            if (pb.shouldRequestSystemAlertWindowPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pb.targetSdkVersion >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(pb.activity)) {
                    pb.grantedPermissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    deniedSet.remove(Manifest.permission.SYSTEM_ALERT_WINDOW)
                } else {
                    deniedSet.add(Manifest.permission.SYSTEM_ALERT_WINDOW)
                    pb.grantedPermissions.remove(Manifest.permission.SYSTEM_ALERT_WINDOW)
                }
            }
            /**
             * 针对低于M的版本已做了处理，具体详见
             * @see RequestWriteSettingsPermission
             * 由于canWrite是大于M版本才有的api，因此需要限制到 M才判断，M以下默认授予权限
             */
            if (pb.shouldRequestWriteSettingsPermission()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pb.targetSdkVersion >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(pb.activity)) {
                    pb.grantedPermissions.add(Manifest.permission.WRITE_SETTINGS)
                    deniedSet.remove(Manifest.permission.WRITE_SETTINGS)
                } else {
                    deniedSet.add(Manifest.permission.WRITE_SETTINGS)
                    pb.grantedPermissions.remove(Manifest.permission.WRITE_SETTINGS)
                }
            }

            if (pb.shouldRequestManageExternalStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    Environment.isExternalStorageManager()) {
                    pb.grantedPermissions.add(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                    deniedSet.remove(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                } else {
                    deniedSet.add(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                    pb.grantedPermissions.remove(RequestManageExternalStoragePermission.MANAGE_EXTERNAL_STORAGE)
                }
            }
            if (pb.shouldRequestInstallPackagesPermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pb.targetSdkVersion >= Build.VERSION_CODES.O) {
                    if (pb.activity.packageManager.canRequestPackageInstalls()) {
                        pb.grantedPermissions.add(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                        deniedSet.remove(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                    } else {
                        deniedSet.add(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                        pb.grantedPermissions.remove(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                    }
                } else {
                    deniedSet.add(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                    pb.grantedPermissions.remove(RequestInstallPackagesPermission.REQUEST_INSTALL_PACKAGES)
                }
            }
            if (pb.shouldRequestNotificationPermission()) {
                if (PermissionX.areNotificationsEnabled(pb.activity)) {
                    pb.grantedPermissions.add(PermissionX.permission.POST_NOTIFICATIONS)
                    deniedSet.remove(PermissionX.permission.POST_NOTIFICATIONS)
                } else {
                    deniedSet.add(PermissionX.permission.POST_NOTIFICATIONS)
                    pb.grantedPermissions.remove(PermissionX.permission.POST_NOTIFICATIONS)
                }
            }
            if (pb.shouldRequestBodySensorsBackgroundPermission()) {
                if (PermissionX.isGranted(pb.activity, RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)) {
                    pb.grantedPermissions.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                    deniedSet.remove(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                } else {
                    deniedSet.add(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                    pb.grantedPermissions.remove(RequestBodySensorsBackgroundPermission.BODY_SENSORS_BACKGROUND)
                }
            }

            pb.requestCallback?.onResult(deniedSet.isEmpty(), ArrayList(pb.grantedPermissions), ArrayList(deniedSet))

            pb.endRequest()
        }
    }

    init {
        explainReasonScope = ExplainScope(pb, this)
        forwardToSettingsScope = ForwardScope(pb, this)
    }
}