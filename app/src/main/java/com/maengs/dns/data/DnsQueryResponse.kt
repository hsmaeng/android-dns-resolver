package com.maengs.dns.data

sealed class DnsQueryResponse(open val domain: String) {

    data class IpAddress(val address: String, val host: String, val canonicalHost: String)

    data class Success(
        override val domain: String,
        val code: Int,
        val addresses: List<IpAddress>
    ) : DnsQueryResponse(domain)

    data class Failure(
        override val domain: String,
        val error: Throwable
    ) : DnsQueryResponse(domain)
}
