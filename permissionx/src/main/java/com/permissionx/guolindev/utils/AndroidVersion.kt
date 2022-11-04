package com.permissionx.guolindev.utils

import android.content.Context
import android.os.Build

object AndroidVersion {
    const val ANDROID_13 = Build.VERSION_CODES.TIRAMISU
    const val ANDROID_12_L = Build.VERSION_CODES.S_V2
    const val ANDROID_12 = Build.VERSION_CODES.S
    const val ANDROID_11 = Build.VERSION_CODES.R
    const val ANDROID_10 = Build.VERSION_CODES.Q
    const val ANDROID_9 = Build.VERSION_CODES.P
    const val ANDROID_8_1 = Build.VERSION_CODES.O_MR1
    const val ANDROID_8 = Build.VERSION_CODES.O
    const val ANDROID_7_1 = Build.VERSION_CODES.N_MR1
    const val ANDROID_7 = Build.VERSION_CODES.N
    const val ANDROID_6 = Build.VERSION_CODES.M
    const val ANDROID_5_1 = Build.VERSION_CODES.LOLLIPOP_MR1
    const val ANDROID_5 = Build.VERSION_CODES.LOLLIPOP
    const val ANDROID_4_4 = Build.VERSION_CODES.KITKAT
    const val ANDROID_4_3 = Build.VERSION_CODES.JELLY_BEAN_MR2
    const val ANDROID_4_2 = Build.VERSION_CODES.JELLY_BEAN_MR1
    const val ANDROID_4_1 = Build.VERSION_CODES.JELLY_BEAN
    const val ANDROID_4_0 = Build.VERSION_CODES.ICE_CREAM_SANDWICH

    private var androidVersion: Int = 0

    /**
     * 获取 Android 版本码
     */
    fun getRuntimeAndroidVersion(): Int {
        if(androidVersion == 0) {
            androidVersion = Build.VERSION.SDK_INT
        }
        return androidVersion
    }

    private var targetSdkVersion = 0

    /**
     * 获取 targetSdk 版本码
     */
    fun getTargetSdkVersion(context: Context): Int {
        if(targetSdkVersion == 0) {
            targetSdkVersion = context.applicationInfo.targetSdkVersion
        }
        return targetSdkVersion
    }

    /**
     * 是否是 Android 13 及以上版本
     */
    val isAndroid13: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_13

    /**
     * 是否是 Android 12 及以上版本
     */
    val isAndroid12: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_12

    /**
     * 是否是 Android 11 及以上版本
     */
    val isAndroid11: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_11

    /**
     * 是否是 Android 10 及以上版本
     */
    val isAndroid10: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_10

    /**
     * 是否是 Android 9.0 及以上版本
     */
    val isAndroid9: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_9

    /**
     * 是否是 Android 8.0 及以上版本
     */
    val isAndroid8: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_8

    /**
     * 是否是 Android 6.0 及以上版本
     */
    val isAndroid6: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_6

    /**
     * 是否是 Android 5.0 及以上版本
     */
    val isAndroid5_1: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_5_1

    /**
     * 是否是 Android 5.0 及以上版本
     */
    val isAndroid5: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_5

    /**
     * 是否是 Android 4.3 及以上版本
     */
    val isAndroid4_3: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_4_3

    /**
     * 是否是 Android 4.2 及以上版本
     */
    val isAndroid4_2: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_4_2

    /**
     * 是否是 Android 4.0 及以上版本
     */
    val isAndroid4: Boolean
        get() = getRuntimeAndroidVersion() >= ANDROID_4_0
}