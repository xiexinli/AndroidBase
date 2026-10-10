package com.development.base.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 网络状态监听（参考 AndroidBuyer 的 DHNetworkKUtil / DHNetworkUtil）。
 *
 * - [isConnected] / [isWifi] / [networkTypeName]：同步查询当前网络状态，适合发请求前做前置判断。
 * - [isOnline]：网络可用性变化的冷流，UI 可据此提示「网络已断开」。需要 `ACCESS_NETWORK_STATE` 权限。
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext context: Context
) {
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    /** 当前是否有可用网络 */
    fun isConnected(): Boolean {
        val cm = connectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** 当前是否 Wi-Fi */
    fun isWifi(): Boolean = hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

    /** 当前是否蜂窝网络 */
    fun isCellular(): Boolean = hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

    private fun hasTransport(transport: Int): Boolean {
        val cm = connectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(transport)
    }

    /** 当前网络类型名称，用于埋点/日志 */
    fun networkTypeName(): String = when {
        !isConnected() -> "无网络"
        isWifi() -> "WIFI"
        isCellular() -> "CELLULAR"
        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
        else -> "UNKNOWN"
    }

    /** 网络可用性变化流，仅当状态真正变化时才发射。 */
    val isOnline: Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(isConnected())
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        }
        trySend(isConnected())
        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (_: SecurityException) {
            // 缺少 ACCESS_NETWORK_STATE 权限时降级为仅初始值
        }
        awaitClose {
            runCatching { cm.unregisterNetworkCallback(callback) }
        }
    }.distinctUntilChanged()
}
