package gr.eduinvoice.data.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network connectivity monitor that provides real-time network state information
 * and connection type detection.
 */
@Singleton
class NetworkMonitor @Inject constructor(
    private val context: Context
) {
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if the device is currently connected to any network
     */
    @SuppressLint("MissingPermission")
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Get the current connection type
     */
    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.BLUETOOTH
            else -> ConnectionType.UNKNOWN
        }
    }

    /**
     * Get network quality based on available bandwidth and latency
     */
    fun getNetworkQuality(): NetworkQuality {
        val network = connectivityManager.activeNetwork ?: return NetworkQuality.POOR
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.POOR

        val downSpeed = capabilities.linkDownstreamBandwidthKbps
        val upSpeed = capabilities.linkUpstreamBandwidthKbps

        return when {
            downSpeed >= 10000 && upSpeed >= 5000 -> NetworkQuality.EXCELLENT
            downSpeed >= 5000 && upSpeed >= 2000 -> NetworkQuality.GOOD
            downSpeed >= 1000 && upSpeed >= 500 -> NetworkQuality.FAIR
            else -> NetworkQuality.POOR
        }
    }

    /**
     * Observe network connectivity changes
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(isConnected)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Send initial state
        trySend(isConnected())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Observe network connection type changes
     */
    fun observeConnectionType(): Flow<ConnectionType> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val connectionType = when {
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.BLUETOOTH
                    else -> ConnectionType.UNKNOWN
                }
                trySend(connectionType)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Send initial state
        trySend(getConnectionType())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * Check if the current connection is metered (e.g., mobile data)
     */
    fun isMeteredConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }

    /**
     * Check if the current connection is expensive (e.g., roaming)
     */
    fun isExpensiveConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return true
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return true
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING).not()
    }
}

/**
 * Represents different types of network connections
 */
enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
    UNKNOWN,
    NONE
}

/**
 * Represents network quality levels
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
} 