package gr.eduinvoice.data.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkMonitorTest {

    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities
    private lateinit var networkMonitor: NetworkMonitor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        connectivityManager = mockk()
        network = mockk()
        networkCapabilities = mockk()

        networkMonitor = NetworkMonitor(context)
    }

    @Test
    fun testIsConnected_WhenNetworkAvailable_ReturnsTrue() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        // When
        val result = networkMonitor.isConnected()

        // Then
        assertTrue(result)
    }

    @Test
    fun testIsConnected_WhenNoNetwork_ReturnsFalse() {
        // Given
        every { connectivityManager.activeNetwork } returns null

        // When
        val result = networkMonitor.isConnected()

        // Then
        assertFalse(result)
    }

    @Test
    fun testIsConnected_WhenNoInternetCapability_ReturnsFalse() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        // When
        val result = networkMonitor.isConnected()

        // Then
        assertFalse(result)
    }

    @Test
    fun testGetConnectionType_WhenWifi_ReturnsWifi() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) } returns false

        // When
        val result = networkMonitor.getConnectionType()

        // Then
        assertEquals(ConnectionType.WIFI, result)
    }

    @Test
    fun testGetConnectionType_WhenCellular_ReturnsCellular() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) } returns false

        // When
        val result = networkMonitor.getConnectionType()

        // Then
        assertEquals(ConnectionType.CELLULAR, result)
    }

    @Test
    fun testGetConnectionType_WhenNoNetwork_ReturnsNone() {
        // Given
        every { connectivityManager.activeNetwork } returns null

        // When
        val result = networkMonitor.getConnectionType()

        // Then
        assertEquals(ConnectionType.NONE, result)
    }

    @Test
    fun testGetNetworkQuality_WhenExcellentBandwidth_ReturnsExcellent() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.linkDownstreamBandwidthKbps } returns 15000
        every { networkCapabilities.linkUpstreamBandwidthKbps } returns 8000

        // When
        val result = networkMonitor.getNetworkQuality()

        // Then
        assertEquals(NetworkQuality.EXCELLENT, result)
    }

    @Test
    fun testGetNetworkQuality_WhenGoodBandwidth_ReturnsGood() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.linkDownstreamBandwidthKbps } returns 8000
        every { networkCapabilities.linkUpstreamBandwidthKbps } returns 3000

        // When
        val result = networkMonitor.getNetworkQuality()

        // Then
        assertEquals(NetworkQuality.GOOD, result)
    }

    @Test
    fun testGetNetworkQuality_WhenPoorBandwidth_ReturnsPoor() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.linkDownstreamBandwidthKbps } returns 500
        every { networkCapabilities.linkUpstreamBandwidthKbps } returns 200

        // When
        val result = networkMonitor.getNetworkQuality()

        // Then
        assertEquals(NetworkQuality.POOR, result)
    }

    @Test
    fun testIsMeteredConnection_WhenNotMetered_ReturnsFalse() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns true

        // When
        val result = networkMonitor.isMeteredConnection()

        // Then
        assertFalse(result)
    }

    @Test
    fun testIsMeteredConnection_WhenMetered_ReturnsTrue() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } returns false

        // When
        val result = networkMonitor.isMeteredConnection()

        // Then
        assertTrue(result)
    }

    @Test
    fun testIsExpensiveConnection_WhenNotRoaming_ReturnsFalse() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) } returns true

        // When
        val result = networkMonitor.isExpensiveConnection()

        // Then
        assertFalse(result)
    }

    @Test
    fun testIsExpensiveConnection_WhenRoaming_ReturnsTrue() {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING) } returns false

        // When
        val result = networkMonitor.isExpensiveConnection()

        // Then
        assertTrue(result)
    }

    @Test
    fun testObserveConnectivity_EmitsInitialState() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        // When & Then
        val connectivityFlow = networkMonitor.observeConnectivity()
        val initialValue = connectivityFlow.first()
        assertTrue(initialValue)
    }

    @Test
    fun testObserveConnectionType_EmitsInitialState() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) } returns false

        // When & Then
        val connectionTypeFlow = networkMonitor.observeConnectionType()
        val initialValue = connectionTypeFlow.first()
        assertEquals(ConnectionType.WIFI, initialValue)
    }
} 