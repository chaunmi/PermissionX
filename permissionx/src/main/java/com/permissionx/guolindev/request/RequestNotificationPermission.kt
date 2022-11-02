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

import com.permissionx.guolindev.PermissionX

/**
 * Implementation for request notification permission below Android T.
 * @author guolin
 * @since 2022/8/21
 *
 * https://developer.android.google.cn/about/versions/13/changes/notification-permission
 * 从Android 13开始弹通知栏需要像使用运行时权限一样申请运行时权限，在PermissionBuilder时已经做了处理变为 normalPermission
 * 这里单独处理主要是为了兼容低版本的处理，低于13版本默认授予通知栏权限，如果拒绝了只能去权限设置页面，而且通知栏权限设置是一个单独的权限页面，便于跳转处理
 * 因此这里单独独立出来进行处理
 *
 */
internal class RequestNotificationPermission internal constructor(permissionBuilder: PermissionBuilder)
    : BaseTask(permissionBuilder) {

    override fun request() {
        if (pb.shouldRequestNotificationPermission()) {
            if (PermissionX.areNotificationsEnabled(pb.activity)) {
                // notification permission has already granted, we can finish this task now.
                finish()
                return
            }
            if (pb.explainReasonCallback != null || pb.explainReasonCallbackWithBeforeParam != null) {
                val requestList = mutableListOf(PermissionX.permission.POST_NOTIFICATIONS)
                if (pb.explainReasonCallbackWithBeforeParam != null) {
                    // callback ExplainReasonCallbackWithBeforeParam prior to ExplainReasonCallback
                    pb.explainReasonCallbackWithBeforeParam!!.onExplainReason(explainScope, requestList, true)
                } else {
                    pb.explainReasonCallback!!.onExplainReason(explainScope, requestList)
                }
                return
            }
        }
        // Shouldn't request notification at this time, so we call finish() to finish this task.
        finish()
    }

    override fun requestAgain(permissions: List<String>) {
        // Don't care what the permissions param is, always request notification permission.
        pb.requestNotificationPermissionNow(this)
    }
}