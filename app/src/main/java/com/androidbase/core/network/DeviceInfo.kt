package com.androidbase.core.network

import android.os.Build
import com.androidbase.BuildConfig

/**
 * 设备与版本信息，用于构造公共请求头、埋点与日志。
 */
object DeviceInfo {

    /** 形如 `com.androidbase/1.0.0 (Android 14; Pixel 8)` */
    fun userAgent(): String =
        "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"

    /** 设备型号 */
    fun deviceModel(): String = Build.MODEL ?: "unknown"

    /** 系统版本号 */
    fun osVersion(): String = Build.VERSION.SDK_INT.toString()
}
