package com.maengs.dns.data

import android.net.DnsResolver

enum class DnsResourceRecord(val id: Int) {
    TYPE_A(DnsResolver.TYPE_A), TYPE_AAA(DnsResolver.TYPE_AAAA);
}
