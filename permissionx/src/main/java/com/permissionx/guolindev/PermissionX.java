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

package com.permissionx.guolindev;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.utils.PermissionChecker;

/**
 * An open source Android library that makes handling runtime permissions extremely easy.
 *
 * The following snippet shows the simple usage:
 * <pre>
 *   PermissionX.init(activity)
 *      .permissions(Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA)
 *      .request { allGranted, grantedList, deniedList ->
 *          // handling the logic
 *      }
 *</pre>
 *  官方文档：权限 API 参考文档，Ctrl + F 搜Protection level: normal(按需替换) 查找对应级别的权限。
 *  https://developer.android.com/guide/topics/permissions/overview?hl=zh-cn
 *  https://developer.android.com/reference/android/Manifest.permission?hl=zh-cn
 *  1) 安装时权限
 *
 * 系统会在用户允许安装该应用时自动授予相应权限(需在应用中声明)，分为两个子类型：
 * • 普通权限 (normal)→ 不会威胁到用户安全和隐私的权限，只需在AndroidManifest.xml中声明下就能直接使用。
 * • 签名权限 (signature)→ 当应用声明了其他应用已定义的签名权限时，如果两个应用使用同一个签名文件进行签名，系统会在安装时向前者授予该权限。
 * 否则，系统无法向前者授予该权限。
 *
 * 2) 运行时权限 (dangerous)
 * Android 6.0 (M) 的新特性，又称危险权限，指的是可能会触及用户隐私或对设备安全性造成影响的权限，如获得联系人信息、访问位置等。
 * 此类权限需要在代码进行申请，系统弹出许可对话框，当用户手动同意后才会获得授权。
 *
 * 3) 特殊权限
 *
 * 比较少见，Google认为此类权限比危险权限更敏感，因此需要让用户到专门的设置页面手动对某个应用程序授权。
 * 如：悬浮框权限、修改设置权限、管理外部存储等。特殊权限需要特殊处理！！！
 *
 * @author guolin
 * @since 2019/11/2
 */
public class PermissionX {

    /**
     * Init PermissionX to make everything prepare to work.
     *
     * @param activity An instance of FragmentActivity
     * @return PermissionCollection instance.
     */
    public static PermissionMediator init(@NonNull FragmentActivity activity) {
        return new PermissionMediator(activity);
    }

    /**
     * Init PermissionX to make everything prepare to work.
     *
     * @param fragment An instance of Fragment
     * @return PermissionCollection instance.
     */
    public static PermissionMediator init(@NonNull Fragment fragment) {
        return new PermissionMediator(fragment);
    }

    /**
     *  A helper function to check a permission is granted or not.
     *
     *  @param context Any context, will not be retained.
     *  @param permission Specific permission name to check. e.g. [android.Manifest.permission.CAMERA].
     *  @return True if this permission is granted, False otherwise.
     */
    public static boolean isGranted(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * A helper function to check are notifications are enabled for current app.
     * @param context
     *          Any context, will not be retained.
     * @return Note that if Android version is lower than N, the return value will always be true.
     */
    public static boolean areNotificationsEnabled(@NonNull Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static final class permission {
        /**
         * Define the const to compat with system lower than T.
         */
        public static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
    }

    public static void setCheckMode(Boolean checkMode) {
        PermissionChecker.INSTANCE.setCheckMode(checkMode);
    }
}
