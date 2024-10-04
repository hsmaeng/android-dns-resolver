package com.maengs.dns.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.maengs.dns.data.DnsQueryRequest
import com.maengs.dns.data.DnsResolverRepository
import kotlinx.coroutines.launch

class DnsResolverViewModel(private val repository: DnsResolverRepository) : ViewModel() {

    val networkInterfaces =
        repository.getAvailableInterfaces().asLiveData(viewModelScope.coroutineContext)

    fun query(networkInterface: String, domain: String) {
        viewModelScope.launch {
            val request = DnsQueryRequest(networkInterface, domain)
            Log.d(TAG, "request:$request")
            val response = repository.query(request)
            Log.d(TAG, "response: $response")
        }
    }

    companion object {
        private const val TAG = "DnsResolverViewModel"
    }
}
