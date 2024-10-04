package com.maengs.dns.data

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.DnsResolver
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
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.InetAddress
import java.util.EnumSet
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

private const val TAG = "DnsResolverRepository"

interface DnsResolverRepository {
    fun getAvailableInterfaces(): Flow<List<String>>
    suspend fun query(request: DnsQueryRequest): DnsQueryResponse

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

    override suspend fun query(request: DnsQueryRequest): DnsQueryResponse {
        require(!request.nsTypes.isEmpty()) {
            "resource records should not be empty!"
        }

        val network = withContext(singleDispatcher) {
            runCatching {
                when (request.interfaceName) {
                    "default" -> null
                    else -> networkProperties.values.first { it.interfaceName == request.interfaceName }.network
                }
            }
        }.onFailure {
            Log.e(TAG, "${it.message}", it)
        }.getOrElse {
            return DnsQueryResponse.Failure(request.domain, it)
        }

        val nsType = when (request.nsTypes) {
            EnumSet.of(DnsResourceRecord.TYPE_A) -> DnsResourceRecord.TYPE_A.id
            EnumSet.of(DnsResourceRecord.TYPE_AAA) -> DnsResourceRecord.TYPE_AAA.id
            else -> null
        }

        return withTimeout(5.seconds) {
            query(network, request.domain, request.flags.toFlags(), nsType)
        }
    }

    private suspend fun query(
        network: Network?,
        domain: String,
        flags: Int,
        nsType: Int?
    ): DnsQueryResponse =
        suspendCoroutine { continuation ->
            val callback = object : DnsResolver.Callback<List<InetAddress>> {
                override fun onAnswer(answer: List<InetAddress>, rcode: Int) {
                    Log.d(TAG, "$rcode: $answer")
                    val list = answer.map {
                        DnsQueryResponse.IpAddress(
                            address = "$it.hostAddress",
                            host = it.hostName,
                            canonicalHost = it.canonicalHostName
                        )
                    }
                    val response = DnsQueryResponse.Success(domain, rcode, list)
                    continuation.resume(response)
                }

                override fun onError(error: DnsResolver.DnsException) {
                    Log.e(TAG, "${error.message}", error)
                    continuation.resume(DnsQueryResponse.Failure(domain, error))
                }
            }

            val dnsResolver = DnsResolver.getInstance()
            val executor = Dispatchers.IO.asExecutor()
            if (nsType == null) {
                dnsResolver.query(network, domain, flags, executor, null, callback)
            } else {
                dnsResolver.query(network, domain, nsType, flags, executor, null, callback)
            }
        }
}
