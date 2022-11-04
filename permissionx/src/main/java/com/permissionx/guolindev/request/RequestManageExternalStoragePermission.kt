/*
 * Copyright (C) guolin, PermissionX Open Source Project
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
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.PermissionX

/**
 * Implementation for request android.permission.MANAGE_EXTERNAL_STORAGE.
 *
 * R版本新加的权限
 * https://developer.android.google.cn/about/versions/11/privacy/storage
 * https://developer.android.google.cn/training/data-storage/manage-all-files
 * 理论上等同于 READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE权限，在R以上只需要申请 MANAGE_EXTERNAL_STORAGE 权限，在R以下
 * 需要申请READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE权限，不能同时申请，因为且申请逻辑不同，MANAGE_EXTERNAL_STORAGE是跳到一个单独的页面授权
 * 而READ_EXTERNAL_STORAGE 和 WRITE_EXTERNAL_STORAGE是弹窗授权，如果同时申请将会出现两次授权提示
 *
 *
 * @author guolin
 * @since 2021/3/1
 */
internal class RequestManageExternalStoragePermission internal constructor(permissionBuilder: PermissionBuilder) :
    BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestManageExternalStoragePermission()) {

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                pb.specialPermissions.remove(Permission.MANAGE_EXTERNAL_STORAGE)
                /**
                 * 对于小于R版本 READ_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE的权限与MANAGE_EXTERNAL_STORAGE相当
                 * https://juejin.cn/post/7053453973990146056#heading-1
                 */
                val readExternalPermission = PermissionX.isGranted(pb.activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                val writeExternalPermission = PermissionX.isGranted(pb.activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if(readExternalPermission && writeExternalPermission) {
                    pb.grantedPermissions.add(Permission.MANAGE_EXTERNAL_STORAGE)
                }else {
                    pb.permissionsWontRequest.add(Permission.MANAGE_EXTERNAL_STORAGE)
                }
                finish()
                return
            }

            if (Environment.isExternalStorageManager()) {
                // MANAGE_EXTERNAL_STORAGE permission has already granted, we can finish this task now.
                finish()
                return
            }
            if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                val requestList = mutableListOf(Permission.MANAGE_EXTERNAL_STORAGE)
                if (pb.explainReasonCallbackWithBeforeParam != null) {
                    // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                    pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(explainScope, requestList, true)
                } else {
                    pb.explainReasonCallback!!.onExplainReason(explainScope, requestList)
                }
            } else {
                // No implementation of explainReasonCallback, we can't request
                // MANAGE_EXTERNAL_STORAGE permission at this time, because user won't understand why.
                finish()
            }
            return
        }
        // shouldn't request MANAGE_EXTERNAL_STORAGE permission at this time, so we call finish()
        // to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // don't care what the permissions param is, always request WRITE_SETTINGS permission.
        pb.requestManageExternalStoragePermissionNow(this)
    }
}