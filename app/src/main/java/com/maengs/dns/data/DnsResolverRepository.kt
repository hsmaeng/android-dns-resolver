package com.maengs.dns.data

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED
import android.net.NetworkCapabilities.NET_CAPABILITY_NOT_VPN
import android.net.NetworkCapabilities.NET_CAPABILITY_TRUSTED
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "DnsResolverRepository"

interface DnsResolverRepository {
    fun getAvailableInterfaces(): Flow<List<String>>

    companion object
}

class DnsResolverRepositoryImpl(connectivityManager: ConnectivityManager) : DnsResolverRepository {
    private data class NetworkProperty(val network: Network, val interfaceName: String?)

    private val scope = CoroutineScope(SupervisorJob())
    private val singleDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val networkProperties = mutableMapOf<String, NetworkProperty>()
    private val networkInterfaces =
        MutableSharedFlow<List<String>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val networkCallback: NetworkCallback by lazy {
        object : NetworkCallback() {
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                Log.d(TAG, "[onLinkPropertiesChanged][$network]${linkProperties.interfaceName}")
                scope.launch {
                    val interfaces = withContext(singleDispatcher) {
                        val id = "$network"
                        networkProperties[id] =
                            NetworkProperty(network, linkProperties.interfaceName)
                        networkProperties.values.map(NetworkProperty::interfaceName)
                    }
                    networkInterfaces.emit(interfaces.filterNotNull())
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "[onLost][$network")
                scope.launch {
                    val interfaces = withContext(singleDispatcher) {
                        val id = "$network"
                        networkProperties.remove(id)
                        networkProperties.values.map(NetworkProperty::interfaceName)
                    }
                    networkInterfaces.emit(interfaces.filterNotNull())
                }
            }
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .removeCapability(NET_CAPABILITY_NOT_RESTRICTED)
            .removeCapability(NET_CAPABILITY_TRUSTED)
            .removeCapability(NET_CAPABILITY_NOT_VPN)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun getAvailableInterfaces(): Flow<List<String>> =
        networkInterfaces.map { listOf("default") + it }.distinctUntilChanged()
}
