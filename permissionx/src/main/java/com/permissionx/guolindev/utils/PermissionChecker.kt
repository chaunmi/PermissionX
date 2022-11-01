package com.permissionx.guolindev.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.XmlResourceParser
import android.os.Build
import com.permissionx.guolindev.Permission
import com.permissionx.guolindev.logger.LogUtils
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.reflect.Field
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal object PermissionChecker {
    /** 设置不检查  */
    private var checkMode: Boolean? = null

    private var defaultCheckMode: Boolean? = null

    fun checkPermissions(context: Context?, permissions: List<String>): Boolean{

        val checkMode: Boolean = isCheckMode()

        // 检查当前 Activity 状态是否是正常的，如果不是则不请求权限
        val activity = PermissionUtils.findActivity(context)
        if (!checkActivityStatus(activity, checkMode)) {
            return false
        }

        // 必须要传入正常的权限或者权限组才能申请权限
        if (!checkPermissionArgument(permissions, checkMode)) {
            return false
        }

        if (checkMode && context != null) {
            // 检查申请的读取媒体位置权限是否符合规范
            checkMediaLocationPermission(context, permissions)
            // 检查申请的存储权限是否符合规范
            checkStoragePermission(context, permissions)
            // 检查申请的传感器权限是否符合规范
            checkBodySensorsPermission(permissions)
            // 检查申请的定位权限是否符合规范
            checkLocationPermission(context, permissions)
            // 检查申请的画中画权限是否符合规范
            checkPictureInPicturePermission(context, permissions)
            // 检查申请的权限和 targetSdk 版本是否能吻合
            checkTargetSdkVersion(context, permissions)
            // 检测权限有没有在清单文件中注册
            checkManifestPermissions(context, permissions)
        }
        return true
    }


    /**
     * 当前是否为检测模式
     */
    fun isCheckMode(): Boolean {
        if (checkMode == null) {
            checkMode = defaultCheckMode
        }
        return checkMode?:false
    }

    fun updateDefaultCheckModel(context: Context) {
        if(defaultCheckMode == null) {
            defaultCheckMode = PermissionUtils.isDebugMode(context)
        }
    }

    fun setCheckMode(checkMode: Boolean) {
        this.checkMode = checkMode
    }

    /**
     * 检查 Activity 的状态是否正常
     *
     * @param checkMode         是否是检查模式
     * @return                  是否检查通过
     */
    fun checkActivityStatus(activity: Activity?, checkMode: Boolean): Boolean {
        // 检查当前 Activity 状态是否是正常的，如果不是则不请求权限
        if (activity == null) {
            if (checkMode) {
                // Context 的实例必须是 Activity 对象
                throw IllegalArgumentException("The instance of the context must be an activity object")
            }else {
                LogUtils.e("activity is null !!")
            }
            return false
        }
        if (activity.isFinishing) {
            if (checkMode) {
                // 这个 Activity 对象当前不能是关闭状态，这种情况常出现在执行异步请求后申请权限
                // 请自行在外层判断 Activity 状态是否正常之后再进入权限申请
                throw IllegalStateException(
                    "The activity has been finishing, " +
                            "please manually determine the status of the activity"
                )
            }else {
                LogUtils.e("activity isFinishing !!")
            }
            return false
        }
        if (Build.VERSION.SDK_INT >= AndroidVersion.ANDROID_4_2 && activity.isDestroyed) {
            if (checkMode) {
                // 这个 Activity 对象当前不能是销毁状态，这种情况常出现在执行异步请求后申请权限
                // 请自行在外层判断 Activity 状态是否正常之后再进入权限申请
                throw IllegalStateException(
                    "The activity has been destroyed, " +
                            "please manually determine the status of the activity"
                )
            }else {
                LogUtils.e("activity isDestroyed !!")
            }
            return false
        }
        return true
    }

    /**
     * 检查传入的权限是否符合要求
     *
     * @param requestPermissions        请求的权限组
     * @param checkMode                 是否是检查模式
     * @return                          是否检查通过
     */
    fun checkPermissionArgument(requestPermissions: List<String>?, checkMode: Boolean): Boolean {
        if (requestPermissions == null || requestPermissions.isEmpty()) {
            if (checkMode) {
                // 不传任何权限，就想动态申请权限？
                throw IllegalArgumentException("The requested permission cannot be empty")
            }else {
                LogUtils.e("requested permission is empty !!")
            }
            return false
        }
        if (Build.VERSION.SDK_INT > AndroidVersion.ANDROID_12_L) {
            // 如果是 Android 12L 后面的版本，则不进行检查
            return true
        }
        if (checkMode) {
            val allPermissions: MutableList<String> = ArrayList()
            val fields: Array<Field> = Permission::class.java.declaredFields
            // 在开启代码混淆之后，反射 Permission 类中的字段会得到空的字段数组
            // 这个是因为编译后常量会在代码中直接引用，所以 Permission 常量字段在混淆的时候会被移除掉
            if (fields.isEmpty()) {
                return true
            }
            for (field: Field in fields) {
                if (String::class.java != field.type) {
                    continue
                }
                try {
                    allPermissions.add(field[null] as String)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
            for (permission: String in requestPermissions) {
                if (!PermissionUtils.containsPermission(allPermissions, permission)) {
                    // 请不要申请危险权限和特殊权限之外的权限
                    throw IllegalArgumentException(
                        "The " + permission +
                                " is not a dangerous permission or special permission, " +
                                "please do not apply dynamically"
                    )
                }
            }
        }
        return true
    }

    /**
     * 检查读取媒体位置权限
     */
    fun checkMediaLocationPermission(context: Context, requestPermissions: List<String?>) {
        // 如果请求的权限中没有包含外部读取媒体位置权限，那么就直接返回
        if (!PermissionUtils.containsPermission(
                requestPermissions,
                Permission.ACCESS_MEDIA_LOCATION
            )
        ) {
            return
        }
//        for (permission: String? in requestPermissions) {
//            if ((PermissionUtils.equalsPermission(permission, Permission.ACCESS_MEDIA_LOCATION)
//                        || PermissionUtils.equalsPermission(
//                    permission,
//                    Permission.READ_MEDIA_IMAGES
//                )
//                        || PermissionUtils.equalsPermission(
//                    permission,
//                    Permission.READ_EXTERNAL_STORAGE
//                )
//                        || PermissionUtils.equalsPermission(
//                    permission,
//                    Permission.WRITE_EXTERNAL_STORAGE
//                )
//                        || PermissionUtils.equalsPermission(
//                    permission,
//                    Permission.MANAGE_EXTERNAL_STORAGE
//                ))
//            ) {
//                continue
//            }
//            throw IllegalArgumentException(
//                "Because it includes access media location permissions, " +
//                        "do not apply for permissions unrelated to access media location"
//            )
//        }
        if (AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_13) {
            if (!PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.READ_MEDIA_IMAGES
                ) &&
                !PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.MANAGE_EXTERNAL_STORAGE
                )
            ) {
                // 你需要在外层手动添加 READ_MEDIA_IMAGES 或者 MANAGE_EXTERNAL_STORAGE 才可以申请 ACCESS_MEDIA_LOCATION 权限
                throw IllegalArgumentException(
                    ("You must add " + Permission.READ_MEDIA_IMAGES.toString() + " or " +
                            Permission.MANAGE_EXTERNAL_STORAGE.toString() + " rights to apply for " + Permission.ACCESS_MEDIA_LOCATION.toString() + " rights")
                )
            }
        } else {
            if (!PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.READ_EXTERNAL_STORAGE
                ) &&
                !PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.MANAGE_EXTERNAL_STORAGE
                )
            ) {
                // 你需要在外层手动添加 READ_EXTERNAL_STORAGE 或者 MANAGE_EXTERNAL_STORAGE 才可以申请 ACCESS_MEDIA_LOCATION 权限
                throw IllegalArgumentException(
                    ("You must add " + Permission.READ_EXTERNAL_STORAGE.toString() + " or " +
                            Permission.MANAGE_EXTERNAL_STORAGE.toString() + " rights to apply for " + Permission.ACCESS_MEDIA_LOCATION.toString() + " rights")
                )
            }
        }
    }

    /**
     * 检查存储权限
     */
    fun checkStoragePermission(context: Context, requestPermissions: List<String?>?) {
        // 如果请求的权限中没有包含外部存储相关的权限，那么就直接返回
        if ((!PermissionUtils.containsPermission(
                requestPermissions,
                Permission.READ_MEDIA_IMAGES
            ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_MEDIA_VIDEO
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_MEDIA_AUDIO
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.MANAGE_EXTERNAL_STORAGE
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_EXTERNAL_STORAGE
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.WRITE_EXTERNAL_STORAGE
                    ))
        ) {
            return
        }
        if (AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_13 &&
            (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.READ_EXTERNAL_STORAGE
            ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.WRITE_EXTERNAL_STORAGE
                    ))
        ) {
            // 当 targetSdkVersion >= 33 应该使用 READ_MEDIA_IMAGES、READ_MEDIA_VIDEO、READ_MEDIA_AUDIO 来代替 READ_EXTERNAL_STORAGE、WRITE_EXTERNAL_STORAGE
            // 因为经过测试，如果当 targetSdkVersion >= 33 申请 READ_EXTERNAL_STORAGE 或者 WRITE_EXTERNAL_STORAGE 会被系统直接拒绝，不会弹出任何授权框
            throw IllegalArgumentException(
                ("When targetSdkVersion >= 33 should use " +
                        Permission.READ_MEDIA_IMAGES.toString() + ", " + Permission.READ_MEDIA_VIDEO.toString() + ", " + Permission.READ_MEDIA_AUDIO.toString() +
                        " instead of " + Permission.READ_EXTERNAL_STORAGE.toString() + ", " + Permission.WRITE_EXTERNAL_STORAGE)
            )
        }

        // 如果申请的是 Android 13 读取照片权限，则绕过本次检查
        if (PermissionUtils.containsPermission(requestPermissions, Permission.READ_MEDIA_IMAGES)) {
            return
        }

        // 如果申请的是 Android 10 获取媒体位置权限，则绕过本次检查
        if (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.ACCESS_MEDIA_LOCATION
            )
        ) {
            return
        }

        // 是否适配了分区存储
        val scopedStorage: Boolean = PermissionUtils.isScopedStorage(context)
        val parser: XmlResourceParser = PermissionUtils.parseAndroidManifest(context) ?: return
        try {
            do {
                // 当前节点必须为标签头部
                if (parser.eventType != XmlResourceParser.START_TAG) {
                    continue
                }

                // 当前标签必须为 application
                if ("application" != parser.name) {
                    continue
                }
                val targetSdkVersion: Int = AndroidVersion.getTargetSdkVersionCode(context)
                val requestLegacyExternalStorage = parser.getAttributeBooleanValue(
                    PermissionUtils.androidNamespace,
                    "requestLegacyExternalStorage", false
                )
                // 如果在已经适配 Android 10 的情况下
                if (((targetSdkVersion >= AndroidVersion.ANDROID_10) && !requestLegacyExternalStorage &&
                            (PermissionUtils.containsPermission(
                                requestPermissions,
                                Permission.MANAGE_EXTERNAL_STORAGE
                            ) || !scopedStorage))
                ) {
                    // 请在清单文件 Application 节点中注册 android:requestLegacyExternalStorage="true" 属性
                    // 否则就算申请了权限，也无法在 Android 10 的设备上正常读写外部存储上的文件
                    // 如果你的项目已经全面适配了分区存储，请在清单文件中注册一个 meta-data 属性
                    // <meta-data android:name="ScopedStorage" android:value="true" /> 来跳过该检查
                    throw IllegalStateException(
                        "Please register the android:requestLegacyExternalStorage=\"true\" " +
                                "attribute in the AndroidManifest.xml file, otherwise it will cause incompatibility with the old version"
                    )
                }

                // 如果在已经适配 Android 11 的情况下
                if (((targetSdkVersion >= AndroidVersion.ANDROID_11) &&
                            !PermissionUtils.containsPermission(
                                requestPermissions,
                                Permission.MANAGE_EXTERNAL_STORAGE
                            ) && !scopedStorage)
                ) {
                    // 1. 适配分区存储的特性，并在清单文件中注册一个 meta-data 属性
                    // <meta-data android:name="ScopedStorage" android:value="true" />
                    // 2. 如果不想适配分区存储，则需要使用 Permission.MANAGE_EXTERNAL_STORAGE 来申请权限
                    // 上面两种方式需要二选一，否则无法在 Android 11 的设备上正常读写外部存储上的文件
                    // 如果不知道该怎么选择，可以看文档：https://github.com/getActivity/XXPermissions/blob/master/HelpDoc
                    throw IllegalArgumentException(
                        ("The storage permission application is abnormal. If you have adapted the scope storage, " +
                                "please register the <meta-data android:name=\"ScopedStorage\" android:value=\"true\" /> attribute in the AndroidManifest.xml file. " +
                                "If there is no adaptation scope storage, please use " + Permission.MANAGE_EXTERNAL_STORAGE + " to apply for permission")
                    )
                }

                // 终止循环
                break
            } while (parser.next() != XmlResourceParser.END_DOCUMENT)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } finally {
            parser.close()
        }
    }

    /**
     * 检查传感器权限
     */
    fun checkBodySensorsPermission(requestPermissions: List<String?>) {
        // 判断是否包含后台传感器权限
        if (!PermissionUtils.containsPermission(
                requestPermissions,
                Permission.BODY_SENSORS_BACKGROUND
            )
        ) {
            return
        }
        if (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.BODY_SENSORS_BACKGROUND
            ) &&
            !PermissionUtils.containsPermission(requestPermissions, Permission.BODY_SENSORS)
        ) {
            // 必须要申请前台传感器权限才能申请后台传感器权限
            throw IllegalArgumentException("Applying for background sensor permissions must contain " + Permission.BODY_SENSORS)
        }
        for (permission: String? in requestPermissions) {
            if (PermissionUtils.equalsPermission(
                    permission,
                    Permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                // 不支持同时申请后台传感器权限和后台定位权限
                throw IllegalArgumentException(
                    ("Applying for permissions " + Permission.BODY_SENSORS_BACKGROUND.toString() +
                            " and " + Permission.ACCESS_BACKGROUND_LOCATION.toString() + " at the same time is not supported")
                )
            }
            if (PermissionUtils.equalsPermission(permission, Permission.ACCESS_MEDIA_LOCATION)) {
                // 不支持同时申请后台传感器权限和获取媒体位置权限
                throw IllegalArgumentException(
                    ("Applying for permissions " + Permission.BODY_SENSORS_BACKGROUND.toString() +
                            " and " + Permission.ACCESS_MEDIA_LOCATION.toString() + " at the same time is not supported")
                )
            }
        }
    }

    /**
     * 检查定位权限
     */
    fun checkLocationPermission(context: Context, requestPermissions: List<String?>) {
        if (AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_12) {
            if (PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.ACCESS_FINE_LOCATION
                ) &&
                !PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.ACCESS_COARSE_LOCATION
                )
            ) {
                // 如果您的应用以 Android 12 为目标平台并且您请求 ACCESS_FINE_LOCATION 权限
                // 则还必须请求 ACCESS_COARSE_LOCATION 权限。您必须在单个运行时请求中包含这两项权限
                // 如果您尝试仅请求 ACCESS_FINE_LOCATION，则系统会忽略该请求并在 Logcat 中记录以下错误消息：
                // ACCESS_FINE_LOCATION must be requested with ACCESS_COARSE_LOCATION
                // 官方适配文档：https://developer.android.google.cn/about/versions/12/approximate-location
                throw IllegalArgumentException(
                    ("If your app targets Android 12 or higher " +
                            "and requests the ACCESS_FINE_LOCATION runtime permission, " +
                            "you must also request the ACCESS_COARSE_LOCATION permission. " +
                            "You must include both permissions in a single runtime request.")
                )
            }
        }

        // 判断是否包含后台定位权限
        if (!PermissionUtils.containsPermission(
                requestPermissions,
                Permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
            return
        }

        // 申请后台定位权限可以不包含模糊定位权限，但是一定要包含精确定位权限，否则后台定位权限会无法申请
        // 也就是会导致无法弹出授权弹窗，经过实践，在 Android 12 上这个问题已经被解决了
        // 但是为了兼容 Android 12 以下的设备还是要那么做，否则在 Android 11 及以下设备会出现异常
        if (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.ACCESS_COARSE_LOCATION
            ) &&
            !PermissionUtils.containsPermission(requestPermissions, Permission.ACCESS_FINE_LOCATION)
        ) {
            throw IllegalArgumentException(
                "Applying for background positioning permissions must include " +
                        Permission.ACCESS_FINE_LOCATION
            )
        }
//        for (permission: String? in requestPermissions) {
//            if ((PermissionUtils.equalsPermission(permission, Permission.ACCESS_FINE_LOCATION)
//                        || PermissionUtils.equalsPermission(
//                    permission,
//                    Permission.ACCESS_COARSE_LOCATION
//                )
//                        || PermissionUtils.equalsPermission(
//                    permission,
//                    Permission.ACCESS_BACKGROUND_LOCATION
//                ))
//            ) {
//                continue
//            }
//            throw IllegalArgumentException(
//                "Because it includes background location permissions, " +
//                        "do not apply for permissions unrelated to location"
//            )
//        }
    }

    /**
     * 检查画中画权限
     */
    fun checkPictureInPicturePermission(context: Context, requestPermissions: List<String?>?) {
        // 如果请求的权限中没有画中画权限，那么就直接返回
        if (!PermissionUtils.containsPermission(
                requestPermissions,
                Permission.PICTURE_IN_PICTURE
            )
        ) {
            return
        }
        val parser: XmlResourceParser = PermissionUtils.parseAndroidManifest(context) ?: return
        try {
            do {
                // 当前节点必须为标签头部
                if (parser.eventType != XmlResourceParser.START_TAG) {
                    continue
                }

                // 当前标签必须为 activity
                if ("activity" != parser.name) {
                    continue
                }
                val supportsPictureInPicture = parser.getAttributeBooleanValue(
                    PermissionUtils.androidNamespace,
                    "supportsPictureInPicture", false
                )
                if (supportsPictureInPicture) {
                    // 终止循环并返回
                    return
                }
            } while (parser.next() != XmlResourceParser.END_DOCUMENT)
            throw IllegalArgumentException(
                "No activity registered supportsPictureInPicture attribute, " +
                        "please register <activity android:supportsPictureInPicture=\"true\" > in AndroidManifest.xml"
            )
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } finally {
            parser.close()
        }
    }

    /**
     * 检查targetSdkVersion 是否符合要求
     *
     * @param requestPermissions            请求的权限组
     */
    fun checkTargetSdkVersion(context: Context, requestPermissions: List<String?>?) {
        // targetSdk 最低版本要求
        val targetSdkMinVersion: Int
        if ((PermissionUtils.containsPermission(
                requestPermissions,
                Permission.POST_NOTIFICATIONS
            ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.NEARBY_WIFI_DEVICES
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.BODY_SENSORS_BACKGROUND
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_MEDIA_IMAGES
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_MEDIA_VIDEO
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_MEDIA_AUDIO
                    ))
        ) {
            targetSdkMinVersion = AndroidVersion.ANDROID_13
        } else if ((PermissionUtils.containsPermission(
                requestPermissions,
                Permission.BLUETOOTH_SCAN
            ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.BLUETOOTH_CONNECT
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.BLUETOOTH_ADVERTISE
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.SCHEDULE_EXACT_ALARM
                    ))
        ) {
            targetSdkMinVersion = AndroidVersion.ANDROID_12
        } else if (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        ) {
            // 必须设置 targetSdkVersion >= 30 才能正常检测权限，否则请使用 Permission.Group.STORAGE 来申请存储权限
            targetSdkMinVersion = AndroidVersion.ANDROID_11
        } else if ((PermissionUtils.containsPermission(
                requestPermissions,
                Permission.ACCESS_BACKGROUND_LOCATION
            ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.ACTIVITY_RECOGNITION
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.ACCESS_MEDIA_LOCATION
                    ))
        ) {
            targetSdkMinVersion = AndroidVersion.ANDROID_10
        } else if (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.ACCEPT_HANDOVER
            )
        ) {
            targetSdkMinVersion = AndroidVersion.ANDROID_9
        } else if ((PermissionUtils.containsPermission(
                requestPermissions,
                Permission.REQUEST_INSTALL_PACKAGES
            ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.ANSWER_PHONE_CALLS
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_PHONE_NUMBERS
                    ) ||
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.PICTURE_IN_PICTURE
                    ))
        ) {
            targetSdkMinVersion = AndroidVersion.ANDROID_8
        } else {
            targetSdkMinVersion = AndroidVersion.ANDROID_6
        }

        // 必须设置正确的 targetSdkVersion 才能正常检测权限
        if (AndroidVersion.getTargetSdkVersionCode(context) < targetSdkMinVersion) {
            throw RuntimeException(
                ("The targetSdkVersion SDK must be " + targetSdkMinVersion +
                        " or more, if you do not want to upgrade targetSdkVersion, " +
                        "please apply with the old permissions")
            )
        }
    }

    /**
     * 检查清单文件中所注册的权限是否正常
     *
     * @param requestPermissions            请求的权限组
     */
    fun checkManifestPermissions(context: Context, requestPermissions: List<String>) {
        val manifestPermissions: HashMap<String, Int> =
            PermissionUtils.getManifestPermissions(context)
        if (manifestPermissions.isEmpty()) {
            throw IllegalStateException("No permissions are registered in the AndroidManifest.xml file")
        }
        val minSdkVersion =
            if (Build.VERSION.SDK_INT >= AndroidVersion.ANDROID_7) context.applicationInfo.minSdkVersion else AndroidVersion.ANDROID_6
        for (permission: String in requestPermissions) {
            if ((PermissionUtils.equalsPermission(
                            permission,
                            Permission.BIND_NOTIFICATION_LISTENER_SERVICE
                        ) ||
                        PermissionUtils.equalsPermission(permission, Permission.BIND_VPN_SERVICE) ||
                        PermissionUtils.equalsPermission(permission, Permission.PICTURE_IN_PICTURE))
            ) {
                // 不检测权限有没有在清单文件中注册，因为这几个权限是框架虚拟出来的，有没有在清单文件中注册都没关系
                continue
            }
            if (PermissionUtils.equalsPermission(permission, Permission.BODY_SENSORS_BACKGROUND)) {
                // 申请后台的传感器权限必须要先注册前台的传感器权限
                checkManifestPermission(manifestPermissions, Permission.BODY_SENSORS, Int.MAX_VALUE)
                continue
            }
            if (PermissionUtils.equalsPermission(
                    permission,
                    Permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                // 在 Android 11 及之前的版本，申请后台定位权限需要精确定位权限
                // 在 Android 12 及之后的版本，申请后台定位权限即可以用精确定位权限也可以用模糊定位权限
                if (AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_12) {
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.ACCESS_FINE_LOCATION,
                        AndroidVersion.ANDROID_11
                    )
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.ACCESS_COARSE_LOCATION,
                        Int.MAX_VALUE
                    )
                } else {
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.ACCESS_FINE_LOCATION,
                        Int.MAX_VALUE
                    )
                }
                continue
            }
            if (minSdkVersion < AndroidVersion.ANDROID_13) {
                if ((PermissionUtils.equalsPermission(permission, Permission.READ_MEDIA_IMAGES) ||
                            PermissionUtils.equalsPermission(
                                permission,
                                Permission.READ_MEDIA_VIDEO
                            ) ||
                            PermissionUtils.equalsPermission(
                                permission,
                                Permission.READ_MEDIA_AUDIO
                            ))
                ) {
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.READ_EXTERNAL_STORAGE,
                        AndroidVersion.ANDROID_12_L
                    )
                    continue
                }
                if (PermissionUtils.equalsPermission(permission, Permission.NEARBY_WIFI_DEVICES)) {
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.ACCESS_FINE_LOCATION,
                        AndroidVersion.ANDROID_12_L
                    )
                    continue
                }
                if (PermissionUtils.equalsPermission(permission, Permission.SCHEDULE_EXACT_ALARM)) {
                    // https://developer.android.google.cn/reference/android/Manifest.permission?hl=zh_cn#USE_EXACT_ALARM
                    if (AndroidVersion.getTargetSdkVersionCode(context) >= AndroidVersion.ANDROID_13) {
                        checkManifestPermission(
                            manifestPermissions,
                            Permission.SCHEDULE_EXACT_ALARM,
                            AndroidVersion.ANDROID_12_L
                        )
                    } else {
                        checkManifestPermission(
                            manifestPermissions,
                            Permission.SCHEDULE_EXACT_ALARM,
                            Int.MAX_VALUE
                        )
                    }
                    continue
                }
            }
            if (minSdkVersion < AndroidVersion.ANDROID_12) {
                if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_SCAN)) {
                    checkManifestPermission(
                        manifestPermissions,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        AndroidVersion.ANDROID_11
                    )
                    // 这是 Android 12 之前遗留的问题，获取扫描蓝牙的结果需要精确定位权限
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.ACCESS_FINE_LOCATION,
                        AndroidVersion.ANDROID_11
                    )
                    continue
                }
                if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_CONNECT)) {
                    checkManifestPermission(
                        manifestPermissions,
                        Manifest.permission.BLUETOOTH,
                        AndroidVersion.ANDROID_11
                    )
                    continue
                }
                if (PermissionUtils.equalsPermission(permission, Permission.BLUETOOTH_ADVERTISE)) {
                    checkManifestPermission(
                        manifestPermissions,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        AndroidVersion.ANDROID_11
                    )
                    continue
                }
            }
            if (minSdkVersion < AndroidVersion.ANDROID_11) {
                if (PermissionUtils.equalsPermission(
                        permission,
                        Permission.MANAGE_EXTERNAL_STORAGE
                    )
                ) {
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.READ_EXTERNAL_STORAGE,
                        AndroidVersion.ANDROID_10
                    )
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.WRITE_EXTERNAL_STORAGE,
                        AndroidVersion.ANDROID_10
                    )
                    continue
                }
            }
            if (minSdkVersion < AndroidVersion.ANDROID_8) {
                if (PermissionUtils.equalsPermission(permission, Permission.READ_PHONE_NUMBERS)) {
                    checkManifestPermission(
                        manifestPermissions,
                        Permission.READ_PHONE_STATE,
                        AndroidVersion.ANDROID_7_1
                    )
                    continue
                }
            }
            checkManifestPermission(manifestPermissions, permission, Int.MAX_VALUE)
        }
    }

    /**
     * 检查某个权限注册是否正常，如果是则会抛出异常
     *
     * @param manifestPermissions       清单权限组
     * @param checkPermission           被检查的权限
     * @param maxSdkVersion             最低要求的 maxSdkVersion
     */
    fun checkManifestPermission(
        manifestPermissions: HashMap<String, Int>,
        checkPermission: String, maxSdkVersion: Int
    ) {
        if (!manifestPermissions.containsKey(checkPermission)) {
            // 动态申请的权限没有在清单文件中注册，分为以下两种情况：
            // 1. 如果你的项目没有在清单文件中注册这个权限，请直接在清单文件中注册一下即可
            // 2. 如果你的项目明明已注册这个权限，可以检查一下编译完成的 apk 包中是否包含该权限，如果里面没有，证明框架的判断是没有问题的
            //    一般是第三方 sdk 或者框架在清单文件中注册了 <uses-permission android:name="xxx" tools:node="remove"/> 导致的
            //    解决方式也很简单，通过在项目中注册 <uses-permission android:name="xxx" tools:node="replace"/> 即可替换掉原先的配置
            // 具体案例：https://github.com/getActivity/XXPermissions/issues/98
            throw IllegalStateException(
                ("Please register permissions in the AndroidManifest.xml file " +
                        "<uses-permission android:name=\"" + checkPermission + "\" />")
            )
        }
        val manifestMaxSdkVersion = manifestPermissions.get(checkPermission) ?: return
        if (manifestMaxSdkVersion < maxSdkVersion) {
            // 清单文件中所注册的权限 maxSdkVersion 大小不符合最低要求，分为以下两种情况：
            // 1. 如果你的项目中注册了该属性，请根据报错提示修改 maxSdkVersion 属性值或者删除 maxSdkVersion 属性
            // 2. 如果你明明没有注册过 maxSdkVersion 属性，可以检查一下编译完成的 apk 包中是否有该属性，如果里面存在，证明框架的判断是没有问题的
            //    一般是第三方 sdk 或者框架在清单文件中注册了 <uses-permission android:name="xxx" android:maxSdkVersion="xx"/> 导致的
            //    解决方式也很简单，通过在项目中注册 <uses-permission android:name="xxx" tools:node="replace"/> 即可替换掉原先的配置
            throw IllegalArgumentException(
                ("The AndroidManifest.xml file " +
                        "<uses-permission android:name=\"" + checkPermission +
                        "\" android:maxSdkVersion=\"" + manifestMaxSdkVersion +
                        "\" /> does not meet the requirements, " +
                        (if (maxSdkVersion != Int.MAX_VALUE) "the minimum requirement for maxSdkVersion is $maxSdkVersion" else "please delete the android:maxSdkVersion=\"$manifestMaxSdkVersion\" attribute"))
            )
        }
    }

    /**
     * 处理和优化已经过时的权限
     *
     * @param requestPermissions            请求的权限组
     */
    fun optimizeDeprecatedPermission(permissions: List<String>): List<String> {
        val requestPermissions = ArrayList(permissions)
        // 如果本次申请包含了 Android 13 WIFI 权限
        if (!AndroidVersion.isAndroid13) {
            if (PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.NEARBY_WIFI_DEVICES
                ) &&
                !PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.ACCESS_FINE_LOCATION
                )
            ) {
                // 这是 Android 13 之前遗留的问题，使用 WIFI 需要精确定位权限
                requestPermissions.add(Permission.ACCESS_FINE_LOCATION)
            }
            if (((PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.READ_MEDIA_IMAGES
                ) ||
                        PermissionUtils.containsPermission(
                            requestPermissions,
                            Permission.READ_MEDIA_VIDEO
                        ) ||
                        PermissionUtils.containsPermission(
                            requestPermissions,
                            Permission.READ_MEDIA_AUDIO
                        ))) &&
                !PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // 添加旧版的存储权限
                requestPermissions.add(Permission.READ_EXTERNAL_STORAGE)
            }
        }

        // 如果本次申请包含了 Android 12 蓝牙扫描权限
        if ((!AndroidVersion.isAndroid12 &&
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.BLUETOOTH_SCAN
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.ACCESS_FINE_LOCATION
                    ))
        ) {
            // 这是 Android 12 之前遗留的问题，扫描蓝牙需要精确定位权限
            requestPermissions.add(Permission.ACCESS_FINE_LOCATION)
        }

        // 如果本次申请包含了 Android 11 存储权限
        if (PermissionUtils.containsPermission(
                requestPermissions,
                Permission.MANAGE_EXTERNAL_STORAGE
            )
        ) {
            if (PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.READ_EXTERNAL_STORAGE
                ) ||
                PermissionUtils.containsPermission(
                    requestPermissions,
                    Permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // 检测是否有旧版的存储权限，有的话直接抛出异常，请不要自己动态申请这两个权限
                // 框架会在 Android 10 以下的版本上自动添加并申请这两个权限
                throw IllegalArgumentException(
                    "If you have applied for MANAGE_EXTERNAL_STORAGE permissions, " +
                            "do not apply for the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions，" +
                            "for below Android 11, the permissionX will add READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE auto。"
                )
            }
            if (!AndroidVersion.isAndroid11) {
                // 自动添加旧版的存储权限，因为旧版的系统不支持申请新版的存储权限
                requestPermissions.add(Permission.READ_EXTERNAL_STORAGE)
                requestPermissions.add(Permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if ((!AndroidVersion.isAndroid10 &&
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.ACTIVITY_RECOGNITION
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.BODY_SENSORS
                    ))
        ) {
            // 自动添加传感器权限，因为 ACTIVITY_RECOGNITION 是从 Android 10 开始才从传感器权限中剥离成独立权限
            requestPermissions.add(Permission.BODY_SENSORS)
        }
        if ((!AndroidVersion.isAndroid8 &&
                    PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_PHONE_NUMBERS
                    ) &&
                    !PermissionUtils.containsPermission(
                        requestPermissions,
                        Permission.READ_PHONE_STATE
                    ))
        ) {
            // 自动添加旧版的读取电话号码权限，因为旧版的系统不支持申请新版的权限
            requestPermissions.add(Permission.READ_PHONE_STATE)
        }
        return requestPermissions
    }
}