package com.maengs.dns.data

import java.util.EnumSet

data class DnsQueryRequest(
    val interfaceName: String,
    val domain: String,
    val nsTypes: EnumSet<DnsResourceRecord> = EnumSet.allOf(DnsResourceRecord::class.java),
    val flags: EnumSet<DnsQueryFlag> = EnumSet.noneOf(DnsQueryFlag::class.java)
)
