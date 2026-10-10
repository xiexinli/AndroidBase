package com.development.base.core.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 设备与宿主 App 版本信息，用于构造公共请求头、埋点与日志。
 *
 * 作为 Library 模块被任意 App 依赖时，`applicationId` / `versionName` 必须来自
 * 运行期的宿主 App，因此这里通过 [PackageManager] 读取，而不是 Library 自身的
 * `BuildConfig` ——后者不携带宿主 App 的包名与版本号。
 */
@Singleton
class DeviceInfo @Inject constructor(@ApplicationContext private val context: Context) {

    /** 宿主 App 的包名（applicationId）。 */
    val appId: String = context.packageName

    /** 宿主 App 的 versionName，读取失败时为 `unknown`。 */
    val versionName: String = readVersionName()

    /** 形如 `com.development.app/1.0.0 (Android 14; Pixel 8)` */
    fun userAgent(): String =
        "$appId/$versionName (Android ${Build.VERSION.RELEASE}; ${Build.MODEL})"

    /** 设备型号 */
    fun deviceModel(): String = Build.MODEL ?: "unknown"

    /** 系统版本号 */
    fun osVersion(): String = Build.VERSION.SDK_INT.toString()

    private fun readVersionName(): String = runCatching {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(context.packageName, 0)
        }
        info.versionName ?: "unknown"
    }.getOrDefault("unknown")
}
