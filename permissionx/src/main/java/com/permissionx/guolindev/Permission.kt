package com.permissionx.guolindev

import android.Manifest

object Permission {
    /**
     * 闹钟权限（特殊权限，Android 12 新增的权限）
     *
     * 需要注意的是：这个权限和其他特殊权限不同的是，默认已经是授予状态，用户也可以手动撤销授权
     * 官方文档介绍：https://developer.android.google.cn/about/versions/12/behavior-changes-12?hl=zh_cn#exact-alarm-permission
     */
    const val SCHEDULE_EXACT_ALARM = "android.permission.SCHEDULE_EXACT_ALARM"

    /**
     * 文件管理权限（特殊权限，Android 11 新增的权限）
     *
     * 为了兼容 Android 11 以下版本，需要在清单文件中注册
     * [Permission.READ_EXTERNAL_STORAGE] 和 [Permission.WRITE_EXTERNAL_STORAGE] 权限
     *
     * 如果你的应用需要上架 GooglePlay，那么需要详细查看：https://support.google.com/googleplay/android-developer/answer/9956427
     */
    const val MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE"

    /**
     * 安装应用权限（特殊权限，Android 8.0 新增的权限）
     *
     * Android 11 特性调整，安装外部来源应用需要重启 App：https://cloud.tencent.com/developer/news/637591
     * 经过实践，Android 12 已经修复了此问题，授权或者取消授权后应用并不会重启
     */
    const val REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES"

    /**
     * 画中画权限（特殊权限，Android 8.0 新增的权限，注意此权限不需要在清单文件中注册也能申请）
     *
     * 需要注意的是：这个权限和其他特殊权限不同的是，默认已经是授予状态，用户也可以手动撤销授权
     */
    const val PICTURE_IN_PICTURE = "android.permission.PICTURE_IN_PICTURE"

    /**
     * 悬浮窗权限（特殊权限，Android 6.0 新增的权限）
     *
     * 在 Android 10 及之前的版本能跳转到应用悬浮窗设置页面，而在 Android 11 及之后的版本只能跳转到系统设置悬浮窗管理列表了
     * 官方解释：https://developer.android.google.cn/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION
     */
    const val SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW"

    /** 系统设置权限（特殊权限，Android 6.0 新增的权限）  */
    const val WRITE_SETTINGS = "android.permission.WRITE_SETTINGS"

    /** 请求忽略电池优化选项权限（特殊权限，Android 6.0 新增的权限） */
    const val REQUEST_IGNORE_BATTERY_OPTIMIZATIONS =
        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"

    /** 勿扰权限，可控制手机响铃模式【静音，震动】（特殊权限，Android 6.0 新增的权限） */
    const val ACCESS_NOTIFICATION_POLICY = "android.permission.ACCESS_NOTIFICATION_POLICY"

    /** 查看应用使用情况权限，简称使用统计权限（特殊权限，Android 5.0 新增的权限）  */
    const val PACKAGE_USAGE_STATS = "android.permission.PACKAGE_USAGE_STATS"

    /** 通知栏监听权限（特殊权限，Android 4.3 新增的权限，注意此权限不需要在清单文件中注册也能申请）  */
    const val BIND_NOTIFICATION_LISTENER_SERVICE =
        "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"

    /** VPN 权限（特殊权限，Android 4.0 新增的权限，注意此权限不需要在清单文件中注册也能申请）  */
    const val BIND_VPN_SERVICE = "android.permission.BIND_VPN_SERVICE"

    /** 通知栏权限（特殊权限，注意此权限不需要在清单文件中注册也能申请）  */
    const val NOTIFICATION_SERVICE = "android.permission.NOTIFICATION_SERVICE"
    /* ------------------------------------ 我是一条华丽的分割线 ------------------------------------ */
    /**
     * 发送通知权限（Android 13.0 新增的权限）
     *
     * 为了兼容 Android 13 以下版本，框架会自动申请 [.NOTIFICATION_SERVICE] 权限
     */
    const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

    /**
     * WIFI 权限（Android 13.0 新增的权限）
     *
     * 需要在清单文件中加入 android:usesPermissionFlags="neverForLocation" 属性（表示不推导设备地理位置）
     * 否则就会导致在没有定位权限的情况下扫描不到附近的 WIFI 设备，这个是经过测试的，下面是清单权限注册案例，请参考以下进行注册
     * <uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" android:usesPermissionFlags="neverForLocation" tools:targetApi="s"></uses-permission>
     *
     * 为了兼容 Android 13 以下版本，需要清单文件中注册 [.ACCESS_FINE_LOCATION] 权限
     * 还有 Android 13 以下设备，使用 WIFI 需要精确定位权限，框架会自动在旧的安卓设备上自动添加此权限进行动态申请
     */
    const val NEARBY_WIFI_DEVICES = "android.permission.NEARBY_WIFI_DEVICES"

    /**
     * 后台传感器权限（Android 13.0 新增的权限）
     *
     * 需要注意的是：
     * 1. 一旦你申请了该权限，在授权的时候，需要选择《始终允许》，而不能选择《仅在使用中允许》
     * 2. 如果你的 App 只在前台状态下使用传感器功能，请不要申请该权限（后台传感器权限）
     */
    const val BODY_SENSORS_BACKGROUND = "android.permission.BODY_SENSORS_BACKGROUND"

    /**
     * 读取图片权限（Android 13.0 新增的权限）
     *
     * 为了兼容 Android 13 以下版本，需要在清单文件中注册 [.READ_EXTERNAL_STORAGE] 权限
     */
    const val READ_MEDIA_IMAGES = "android.permission.READ_MEDIA_IMAGES"

    /**
     * 读取视频权限（Android 13.0 新增的权限）
     *
     * 为了兼容 Android 13 以下版本，需要在清单文件中注册 [.READ_EXTERNAL_STORAGE] 权限
     */
    const val READ_MEDIA_VIDEO = "android.permission.READ_MEDIA_VIDEO"

    /**
     * 读取音频权限（Android 13.0 新增的权限）
     *
     * 为了兼容 Android 13 以下版本，需要在清单文件中注册 [.READ_EXTERNAL_STORAGE] 权限
     */
    const val READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

    /**
     * 蓝牙扫描权限（Android 12.0 新增的权限）
     *
     * 需要在清单文件中加入 android:usesPermissionFlags="neverForLocation" 属性（表示不推导设备地理位置）
     * 否则就会导致在没有定位权限的情况下扫描不到附近的蓝牙设备，这个是经过测试的，下面是清单权限注册案例，请参考以下进行注册
     * <uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" tools:targetApi="s"></uses-permission>
     *
     * 为了兼容 Android 12 以下版本，需要清单文件中注册 [Manifest.permission.BLUETOOTH_ADMIN] 权限
     * 还有 Android 12 以下设备，获取蓝牙扫描结果需要精确定位权限，框架会自动在旧的安卓设备上自动添加此权限进行动态申请
     */
    const val BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"

    /**
     * 蓝牙连接权限（Android 12.0 新增的权限）
     *
     * 为了兼容 Android 12 以下版本，需要在清单文件中注册 [Manifest.permission.BLUETOOTH] 权限
     */
    const val BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"

    /**
     * 蓝牙广播权限（Android 12.0 新增的权限）
     *
     * 将当前设备的蓝牙进行广播，供其他设备扫描时需要用到该权限
     * 为了兼容 Android 12 以下版本，需要在清单文件中注册 [Manifest.permission.BLUETOOTH_ADMIN] 权限
     */
    const val BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE"

    /**
     * 在后台获取位置（Android 10.0 新增的权限）
     *
     * 需要注意的是：
     * 1. 一旦你申请了该权限，在授权的时候，需要选择《始终允许》，而不能选择《仅在使用中允许》
     * 2. 如果你的 App 只在前台状态下使用定位功能，请不要申请该权限（后台定位权限）
     */
    const val ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION"

    /**
     * 获取活动步数（Android 10.0 新增的权限）
     *
     * 需要注意的是：Android 10 以下不需要传感器（BODY_SENSORS）权限也能获取到步数
     */
    const val ACTIVITY_RECOGNITION = "android.permission.ACTIVITY_RECOGNITION"

    /**
     * 读取照片中的地理位置（Android 10.0 新增的权限）
     *
     * 需要注意的是：如果这个权限申请成功了但是不能正常读取照片的地理信息，那么需要先申请存储权限：
     *
     * 如果项目 targetSdkVersion <= 29 需要申请 [Permission.Group.STORAGE]
     * 如果项目 targetSdkVersion >= 30 需要申请 [Permission.MANAGE_EXTERNAL_STORAGE]
     */
    const val ACCESS_MEDIA_LOCATION = "android.permission.ACCESS_MEDIA_LOCATION"

    /** 允许呼叫应用继续在另一个应用中启动的呼叫（Android 9.0 新增的权限）  */
    const val ACCEPT_HANDOVER = "android.permission.ACCEPT_HANDOVER"

    /**
     * 读取手机号码（Android 8.0 新增的权限）
     *
     * 为了兼容 Android 8.0 以下版本，需要在清单文件中注册 [.READ_PHONE_STATE] 权限
     */
    const val READ_PHONE_NUMBERS = "android.permission.READ_PHONE_NUMBERS"

    /**
     * 接听电话（Android 8.0 新增的权限，Android 8.0 以下可以采用模拟耳机按键事件来实现接听电话，这种方式不需要权限）
     */
    const val ANSWER_PHONE_CALLS = "android.permission.ANSWER_PHONE_CALLS"

    /** 读取外部存储  */
    const val READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"

    /** 写入外部存储  */
    const val WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE"

    /** 相机权限  */
    const val CAMERA = "android.permission.CAMERA"

    /** 麦克风权限  */
    const val RECORD_AUDIO = "android.permission.RECORD_AUDIO"

    /** 获取精确位置  */
    const val ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"

    /** 获取粗略位置  */
    const val ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"

    /** 读取联系人  */
    const val READ_CONTACTS = "android.permission.READ_CONTACTS"

    /** 修改联系人  */
    const val WRITE_CONTACTS = "android.permission.WRITE_CONTACTS"

    /** 访问账户列表  */
    const val GET_ACCOUNTS = "android.permission.GET_ACCOUNTS"

    /** 读取日历  */
    const val READ_CALENDAR = "android.permission.READ_CALENDAR"

    /** 修改日历  */
    const val WRITE_CALENDAR = "android.permission.WRITE_CALENDAR"

    /**
     * 读取电话状态
     *
     * 需要注意的是：这个权限在某些手机上面是没办法获取到的，因为某些系统禁止应用获得该权限
     * 所以你要是申请了这个权限之后没有弹授权框，而是直接回调授权失败方法
     * 请不要惊慌，这个不是 Bug、不是 Bug、不是 Bug，而是正常现象
     *
     * 后续情况汇报：有人反馈在 iQOO 手机上面获取不到该权限，在清单文件加入下面这个权限就可以了（这里只是做记录，并不代表这种方式就一定有效果）
     * <uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"></uses-permission>
     */
    const val READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"

    /** 拨打电话  */
    const val CALL_PHONE = "android.permission.CALL_PHONE"

    /** 读取通话记录  */
    const val READ_CALL_LOG = "android.permission.READ_CALL_LOG"

    /** 修改通话记录  */
    const val WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG"

    /** 添加语音邮件  */
    const val ADD_VOICEMAIL = "com.android.voicemail.permission.ADD_VOICEMAIL"

    /** 使用SIP视频  */
    const val USE_SIP = "android.permission.USE_SIP"

    /**
     * 处理拨出电话
     *
     */
    @Deprecated("在 Android 10 已经过时，请见：https://developer.android.google.cn/reference/android/Manifest.permission?hl=zh_cn#PROCESS_OUTGOING_CALLS")
    val PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS"

    /** 使用传感器  */
    const val BODY_SENSORS = "android.permission.BODY_SENSORS"

    /** 发送短信  */
    const val SEND_SMS = "android.permission.SEND_SMS"

    /** 接收短信  */
    const val RECEIVE_SMS = "android.permission.RECEIVE_SMS"

    /** 读取短信  */
    const val READ_SMS = "android.permission.READ_SMS"

    /** 接收 WAP 推送消息  */
    const val RECEIVE_WAP_PUSH = "android.permission.RECEIVE_WAP_PUSH"

    /** 接收彩信  */
    const val RECEIVE_MMS = "android.permission.RECEIVE_MMS"
}