package com.maengs.dns.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.maengs.dns.data.DnsResolverRepository
import kotlinx.coroutines.launch

class DnsResolverViewModel(private val repository: DnsResolverRepository) : ViewModel() {

    val networkInterfaces =
        repository.getAvailableInterfaces().asLiveData(viewModelScope.coroutineContext)

    fun query(networkInterface: String, domain: String) {
        viewModelScope.launch {
            Log.d(TAG, "query:$networkInterface, $domain")
        }
    }

    companion object {
        private const val TAG = "DnsResolverViewModel"
    }
}
