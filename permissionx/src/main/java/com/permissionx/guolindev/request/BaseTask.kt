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
import com.permissionx.guolindev.Permission
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
abstract class BaseTask(@JvmField var pb: PermissionBuilder) : ChainTask {
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
             * 对于Q以下版本无后台定位权限，在[RequestBackgroundLocationPermission]中已经做过处理, 从special权限中移除
             * 因此这里不用再单独针对版本做判断处理
             */
            if (pb.shouldRequestBackgroundLocationPermission()) {
                if (PermissionX.isGranted(pb.activity, Permission.ACCESS_BACKGROUND_LOCATION)) {
                    pb.grantedPermissions.add(Permission.ACCESS_BACKGROUND_LOCATION)
                    deniedSet.remove(Permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    deniedSet.add(Permission.ACCESS_BACKGROUND_LOCATION)
                    pb.grantedPermissions.remove(Permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            /**
             * 针对低于M的版本已做了处理，从special中移除，默认授予权限，具体详见[RequestSystemAlertWindowPermission]
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
             * 针对低于M的版本已做了处理，从special中移除，默认授予权限，具体详见[RequestWriteSettingsPermission]
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

            /**
             * 针对低于R版本已经做了处理， 从special中移除， 具体详见[RequestManageExternalStoragePermission]
             * Environment.isExternalStorageManager()为R以上版本才有的api
             */
            if (pb.shouldRequestManageExternalStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    Environment.isExternalStorageManager()) {
                    pb.grantedPermissions.add(Permission.MANAGE_EXTERNAL_STORAGE)
                    deniedSet.remove(Permission.MANAGE_EXTERNAL_STORAGE)
                } else {
                    deniedSet.add(Permission.MANAGE_EXTERNAL_STORAGE)
                    pb.grantedPermissions.remove(Permission.MANAGE_EXTERNAL_STORAGE)
                }
            }

            /**
             * 低于O版本的已经从special中移除，具体详见[RequestInstallPackagesPermission]
             * canRequestPackageInstalls()为 O及以上版本才有的api
             */
            if (pb.shouldRequestInstallPackagesPermission() &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (pb.activity.packageManager.canRequestPackageInstalls()) {
                    pb.grantedPermissions.add(Permission.REQUEST_INSTALL_PACKAGES)
                    deniedSet.remove(Permission.REQUEST_INSTALL_PACKAGES)
                } else {
                    deniedSet.add(Permission.REQUEST_INSTALL_PACKAGES)
                    pb.grantedPermissions.remove(Permission.REQUEST_INSTALL_PACKAGES)
                }
            }
            /**
             * 主要是低于13版本的处理，高于13版本的该权限在[RequestNormalPermissions]中当正常运行时权限处理
             */
            if (pb.shouldRequestNotificationPermission()) {
                if (PermissionX.areNotificationsEnabled(pb.activity)) {
                    pb.grantedPermissions.add(Permission.POST_NOTIFICATIONS)
                    deniedSet.remove(Permission.POST_NOTIFICATIONS)
                } else {
                    deniedSet.add(Permission.POST_NOTIFICATIONS)
                    pb.grantedPermissions.remove(Permission.POST_NOTIFICATIONS)
                }
            }

            /**
             * 对于33以下版本无后台身体传感器权限，在[RequestBodySensorsBackgroundPermission]中已经做过处理, 从special权限中移除
             * 因此这里不用再单独针对版本做判断处理
             */
            if (pb.shouldRequestBodySensorsBackgroundPermission()) {
                if (PermissionX.isGranted(pb.activity, Permission.BODY_SENSORS_BACKGROUND)) {
                    pb.grantedPermissions.add(Permission.BODY_SENSORS_BACKGROUND)
                    deniedSet.remove(Permission.BODY_SENSORS_BACKGROUND)
                } else {
                    deniedSet.add(Permission.BODY_SENSORS_BACKGROUND)
                    pb.grantedPermissions.remove(Permission.BODY_SENSORS_BACKGROUND)
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