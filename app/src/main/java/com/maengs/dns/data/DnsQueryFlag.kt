package com.maengs.dns.data

import android.net.DnsResolver
import java.util.EnumSet

enum class DnsQueryFlag(val id: Int) {
    NO_RETRY(DnsResolver.FLAG_NO_RETRY),
    NO_CACHE_STORE(DnsResolver.FLAG_NO_CACHE_STORE),
    NO_CACHE_LOOKUP(DnsResolver.FLAG_NO_CACHE_LOOKUP)
}

fun EnumSet<DnsQueryFlag>.toFlags(): Int = fold(0) { acc, flag ->
    acc or flag.id
}
