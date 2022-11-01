package com.permissionx.guolindev.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.XmlResourceParser
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Surface
import androidx.annotation.RequiresApi
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.request.PermissionApi
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.util.*

object PermissionUtils {
    /** Handler 对象  */
    private val HANDLER = Handler(Looper.getMainLooper())

    /**
     * 判断某个权限是否是特殊权限
     */
    fun isSpecialPermission(permission: String?): Boolean {
        return equalsPermission(permission, Permission.MANAGE_EXTERNAL_STORAGE) ||
                equalsPermission(permission, Permission.REQUEST_INSTALL_PACKAGES) ||
                equalsPermission(permission, Permission.SYSTEM_ALERT_WINDOW) ||
                equalsPermission(permission, Permission.WRITE_SETTINGS) ||
                equalsPermission(permission, Permission.POST_NOTIFICATIONS) ||
                equalsPermission(permission, Permission.PACKAGE_USAGE_STATS) ||
                equalsPermission(permission, Permission.SCHEDULE_EXACT_ALARM) ||
                equalsPermission(permission, Permission.BIND_NOTIFICATION_LISTENER_SERVICE) ||
                equalsPermission(permission, Permission.ACCESS_NOTIFICATION_POLICY) ||
                equalsPermission(permission, Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) ||
                equalsPermission(permission, Permission.BIND_VPN_SERVICE) ||
                equalsPermission(permission, Permission.PICTURE_IN_PICTURE)
    }

    /**
     * 判断某个危险权限是否授予了
     */
    @RequiresApi(api = AndroidVersion.ANDROID_6)
    fun checkSelfPermission(context: Context, permission: String?): Boolean {
        return context.checkSelfPermission(permission!!) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 解决 Android 12 调用 shouldShowRequestPermissionRationale 出现内存泄漏的问题
     * Android 12L 和 Android 13 版本经过测试不会出现这个问题，证明 Google 在新版本上已经修复了这个问题
     * 但是对于 Android 12 仍是一个历史遗留问题，这是我们所有应用开发者不得不面对的一个事情
     *
     * issues 地址：https://github.com/getActivity/XXPermissions/issues/133
     */
    @RequiresApi(api = AndroidVersion.ANDROID_6)
    fun shouldShowRequestPermissionRationale(activity: Activity, permission: String?): Boolean {
        if (AndroidVersion.androidVersionCode === AndroidVersion.ANDROID_12) {
            try {
                val packageManager = activity.application.packageManager
                val method = PackageManager::class.java.getMethod(
                    "shouldShowRequestPermissionRationale",
                    String::class.java
                )
                return method.invoke(packageManager, permission) as Boolean
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        return activity.shouldShowRequestPermissionRationale(permission!!)
    }

    /**
     * 延迟一段时间执行
     */
    fun postDelayed(runnable: Runnable?, delayMillis: Long) {
        HANDLER.postDelayed(runnable!!, delayMillis)
    }

    /**
     * 延迟一段时间执行 OnActivityResult，避免有些机型明明授权了，但还是回调失败的问题
     */
    fun postActivityResult(permissions: List<String?>?, runnable: Runnable?) {
        var delayMillis: Long
        delayMillis = if (AndroidVersion.isAndroid11) {
            200
        } else {
            300
        }
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
        if (manufacturer.contains("huawei")) {
            // 需要加长时间等待，不然某些华为机型授权了但是获取不到权限
            delayMillis = if (AndroidVersion.isAndroid8) {
                300
            } else {
                500
            }
        } else if (manufacturer.contains("xiaomi")) {
            // 经过测试，发现小米 Android 11 及以上的版本，申请这个权限需要 1 秒钟才能判断到
            // 因为在 Android 10 的时候，这个特殊权限弹出的页面小米还是用谷歌原生的
            // 然而在 Android 11 之后的，这个权限页面被小米改成了自己定制化的页面
            // 测试了原生的模拟器和 vivo 云测并发现没有这个问题，所以断定这个 Bug 就是小米特有的
            if (AndroidVersion.isAndroid11 &&
                containsPermission(permissions, Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            ) {
                delayMillis = 1000
            }
        }
        HANDLER.postDelayed(runnable!!, delayMillis)
    }

    /**
     * 获取 Android 属性命名空间
     */
    val androidNamespace: String
        get() = "http://schemas.android.com/apk/res/android"

    /**
     * 当前是否处于 debug 模式
     */
    fun isDebugMode(context: Context): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    /**
     * 返回应用程序在清单文件中注册的权限
     */
    fun getManifestPermissions(context: Context): HashMap<String, Int> {
        val manifestPermissions = HashMap<String, Int>()
        val parser = parseAndroidManifest(context)
        if (parser != null) {
            try {
                do {
                    // 当前节点必须为标签头部
                    if (parser.eventType != XmlResourceParser.START_TAG) {
                        continue
                    }

                    // 当前标签必须为 uses-permission
                    if ("uses-permission" != parser.name) {
                        continue
                    }
                    manifestPermissions[parser.getAttributeValue(androidNamespace, "name")] =
                        parser.getAttributeIntValue(
                            androidNamespace,
                            "maxSdkVersion",
                            Int.MAX_VALUE
                        )
                } while (parser.next() != XmlResourceParser.END_DOCUMENT)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } finally {
                parser.close()
            }
        }
        if (manifestPermissions.isEmpty()) {
            try {
                // 当清单文件没有注册任何权限的时候，那么这个数组对象就是空的
                // https://github.com/getActivity/XXPermissions/issues/35
                val requestedPermissions = context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.GET_PERMISSIONS
                ).requestedPermissions
                if (requestedPermissions != null) {
                    for (permission in requestedPermissions) {
                        manifestPermissions[permission] = Int.MAX_VALUE
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        return manifestPermissions
    }

    /**
     * 优化权限回调结果
     */
    fun optimizePermissionResults(
        activity: Activity,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        for (i in permissions.indices) {
            var recheck = false
            val permission = permissions[i]

            // 如果这个权限是特殊权限，那么就重新进行权限检测
            if (PermissionApi.isSpecialPermission(permission)) {
                recheck = true
            }
            if (!AndroidVersion.isAndroid13 &&
                (equalsPermission(permission, Permission.POST_NOTIFICATIONS) ||
                        equalsPermission(permission, Permission.NEARBY_WIFI_DEVICES) ||
                        equalsPermission(permission, Permission.BODY_SENSORS_BACKGROUND) ||
                        equalsPermission(permission, Permission.READ_MEDIA_IMAGES) ||
                        equalsPermission(permission, Permission.READ_MEDIA_VIDEO) ||
                        equalsPermission(permission, Permission.READ_MEDIA_AUDIO))
            ) {
                recheck = true
            }

            // 重新检查 Android 12 的三个新权限
            if (!AndroidVersion.isAndroid12 &&
                (equalsPermission(permission, Permission.BLUETOOTH_SCAN) ||
                        equalsPermission(permission, Permission.BLUETOOTH_CONNECT) ||
                        equalsPermission(permission, Permission.BLUETOOTH_ADVERTISE))
            ) {
                recheck = true
            }

            // 重新检查 Android 10.0 的三个新权限
            if (!AndroidVersion.isAndroid10 &&
                (equalsPermission(permission, Permission.ACCESS_BACKGROUND_LOCATION) ||
                        equalsPermission(permission, Permission.ACTIVITY_RECOGNITION) ||
                        equalsPermission(permission, Permission.ACCESS_MEDIA_LOCATION))
            ) {
                recheck = true
            }

            // 重新检查 Android 9.0 的一个新权限
            if (!AndroidVersion.isAndroid9 &&
                equalsPermission(permission, Permission.ACCEPT_HANDOVER)
            ) {
                recheck = true
            }

            // 重新检查 Android 8.0 的两个新权限
            if (!AndroidVersion.isAndroid8 &&
                (equalsPermission(permission, Permission.ANSWER_PHONE_CALLS) ||
                        equalsPermission(permission, Permission.READ_PHONE_NUMBERS))
            ) {
                recheck = true
            }
            if (recheck) {
                grantResults[i] = if (PermissionApi.isGrantedPermission(
                        activity,
                        permission
                    )
                ) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
            }
        }
    }

    /**
     * 将数组转换成 ArrayList
     *
     * 这里解释一下为什么不用 Arrays.asList
     * 第一是返回的类型不是 java.util.ArrayList 而是 java.util.Arrays.ArrayList
     * 第二是返回的 ArrayList 对象是只读的，也就是不能添加任何元素，否则会抛异常
     */
    fun <T> asArrayList(vararg array: T): ArrayList<T> {
        val list = ArrayList<T>(array.size)
        if (array == null || array.size == 0) {
            return list
        }
        for (t in array) {
            list.add(t)
        }
        return list
    }

    @SafeVarargs
    fun <T> asArrayLists(vararg arrays: Array<T>): ArrayList<T> {
        val list = ArrayList<T>()
        if (arrays == null || arrays.size == 0) {
            return list
        }
        for (ts in arrays) {
            list.addAll(asArrayList(*ts))
        }
        return list
    }

    /**
     * 寻找上下文中的 Activity 对象
     */
    fun findActivity(context: Context?): Activity? {
        var context = context
        do {
            context = if (context is Activity) {
                return context
            } else if (context is ContextWrapper) {
                context.baseContext
            } else {
                return null
            }
        } while (context != null)
        return null
    }

    /**
     * 获取当前应用 Apk 在 AssetManager 中的 Cookie，如果获取失败，则为 0
     */
    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    fun findApkPathCookie(context: Context, apkPath: String?): Int {
        val assets = context.assets
        var cookie: Int
        try {
            if (AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_9 &&
                AndroidVersion.androidVersionCode >= AndroidVersion.ANDROID_9 &&
                AndroidVersion.androidVersionCode < AndroidVersion.ANDROID_11
            ) {
                val clazz: Class<*> = assets.javaClass
                val findCookieForPathMethod = clazz.getDeclaredMethod(
                    "findCookieForPath",
                    String::class.java
                )
                // 注意 AssetManager.findCookieForPath 是 Android 9.0（API 28）的时候才添加的方法
                // 而 Android 9.0 用的是 AssetManager.addAssetPath 来获取 cookie
                // 具体可以参考 PackageParser.parseBaseApk 方法源码的实现
                if (findCookieForPathMethod != null) {
                    findCookieForPathMethod.isAccessible = true
                    cookie = findCookieForPathMethod.invoke(assets, apkPath) as Int
                    if (cookie != null) {
                        return cookie
                    }
                }
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        try {
            val addAssetPathMethod = assets.javaClass.getDeclaredMethod(
                "addAssetPath",
                String::class.java
            )
            cookie = addAssetPathMethod.invoke(assets, apkPath) as Int
            if (cookie != null) {
                return cookie
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        // 获取失败
        return 0
    }

    /**
     * 解析清单文件
     */
    fun parseAndroidManifest(context: Context): XmlResourceParser? {
        val cookie = findApkPathCookie(context, context.applicationInfo.sourceDir)
        if (cookie == 0) {
            // 如果 cookie 为 0，证明获取失败，直接 return
            return null
        }
        try {
            val parser = context.assets.openXmlResourceParser(cookie, "AndroidManifest.xml")
            do {
                // 当前节点必须为标签头部
                if (parser.eventType != XmlResourceParser.START_TAG) {
                    continue
                }
                if ("manifest" == parser.name) {
                    // 如果读取到的包名和当前应用的包名不是同一个的话，证明这个清单文件的内容不是当前应用的
                    // 具体案例：https://github.com/getActivity/XXPermissions/issues/102
                    if (TextUtils.equals(
                            context.packageName,
                            parser.getAttributeValue(null, "package")
                        )
                    ) {
                        return parser
                    }
                }
            } while (parser.next() != XmlResourceParser.END_DOCUMENT)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 判断是否适配了分区存储
     */
    fun isScopedStorage(context: Context): Boolean {
        try {
            val metaKey = "ScopedStorage"
            val metaData = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            ).metaData
            if (metaData != null && metaData.containsKey(metaKey)) {
                return java.lang.Boolean.parseBoolean(metaData[metaKey].toString())
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 锁定当前 Activity 的方向
     */
    @SuppressLint("SwitchIntDef")
    fun lockActivityOrientation(activity: Activity) {
        try {
            // 兼容问题：在 Android 8.0 的手机上可以固定 Activity 的方向，但是这个 Activity 不能是透明的，否则就会抛出异常
            // 复现场景：只需要给 Activity 主题设置 <item name="android:windowIsTranslucent">true</item> 属性即可
            when (activity.resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> activity.requestedOrientation =
                    if (isActivityReverse(activity)) ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                Configuration.ORIENTATION_PORTRAIT -> activity.requestedOrientation =
                    if (isActivityReverse(activity)) ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else -> {}
            }
        } catch (e: IllegalStateException) {
            // java.lang.IllegalStateException: Only fullscreen activities can request orientation
            e.printStackTrace()
        }
    }

    /**
     * 判断 Activity 是否反方向旋转了
     */
    fun isActivityReverse(activity: Activity): Boolean {
        // 获取 Activity 旋转的角度
        val activityRotation: Int
        activityRotation = if (AndroidVersion.isAndroid11) {
            activity.display!!.rotation
        } else {
            activity.windowManager.defaultDisplay.rotation
        }
        return when (activityRotation) {
            Surface.ROTATION_180, Surface.ROTATION_270 -> true
            Surface.ROTATION_0, Surface.ROTATION_90 -> false
            else -> false
        }
    }

    /**
     * 判断这个意图的 Activity 是否存在
     */
    fun areActivityIntent(context: Context, intent: Intent?): Boolean {
        if(intent == null) {
            return false
        }
        return if(AndroidVersion.isAndroid13) {
            context.packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(
                PackageManager.MATCH_DEFAULT_ONLY.toLong()
            )).isNotEmpty()
        }else {
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            ).isNotEmpty()
        }
    }

    /**
     * 获取应用详情界面意图
     */
    fun getApplicationDetailsIntent(context: Context): Intent {
        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = getPackageNameUri(context)
        if (!areActivityIntent(context, intent)) {
            intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            if (!areActivityIntent(context, intent)) {
                intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
            }
        }
        return intent
    }

    /**
     * 获取包名 uri
     */
    fun getPackageNameUri(context: Context): Uri {
        return Uri.parse("package:" + context.packageName)
    }

    /**
     * 根据传入的权限自动选择最合适的权限设置页
     * 主要是在特殊权限的时候用，一般只有特殊权限需要跳转具体权限页面
     * 另一种情况是拒绝不再询问后需要跳转到app权限页面
     * @param permissions                 请求失败的权限
     */
    fun getSmartPermissionIntent(context: Context, permissions: List<String?>?): Intent {
        // 如果失败的权限里面不包含特殊权限
        if (permissions == null || permissions.isEmpty() ||
            !PermissionApi.containsSpecialPermission(permissions)
        ) {
            return getApplicationDetailsIntent(context)
        }
        when (permissions.size) {
            1 ->                 // 如果当前只有一个权限被拒绝了
                return PermissionApi.getPermissionIntent(context, permissions[0])
            3 -> if (AndroidVersion.isAndroid11 &&
                containsPermission(permissions, Permission.MANAGE_EXTERNAL_STORAGE) &&
                containsPermission(permissions, Permission.READ_EXTERNAL_STORAGE) &&
                containsPermission(permissions, Permission.WRITE_EXTERNAL_STORAGE)
            ) {
                return PermissionApi.getPermissionIntent(
                    context,
                    Permission.MANAGE_EXTERNAL_STORAGE
                )
            }
            else -> {}
        }
        return getApplicationDetailsIntent(context)
    }

    /**
     * 判断两个权限字符串是否为同一个
     */
    fun equalsPermission(permission1: String?, permission2: String?): Boolean {
        if (permission1 == null || permission2 == null) {
            return false
        }
        val length = permission1.length
        if (length != permission2.length) {
            return false
        }

        // 因为权限字符串都是 android.permission 开头
        // 所以从最后一个字符开始判断，可以提升 equals 的判断效率
        for (i in length - 1 downTo 0) {
            if (permission1[i] != permission2[i]) {
                return false
            }
        }
        return true
    }

    /**
     * 判断权限集合中是否包含某个权限
     */
    fun containsPermission(permissions: Collection<String?>?, permission: String?): Boolean {
        if (permissions == null || permissions.isEmpty() || permission == null) {
            return false
        }
        for (s in permissions) {
            // 使用 equalsPermission 来判断可以提升代码效率
            if (equalsPermission(s, permission)) {
                return true
            }
        }
        return false
    }
}