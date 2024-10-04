package com.maengs.dns.data

import android.net.DnsResolver.FLAG_EMPTY
import android.net.DnsResolver.FLAG_NO_CACHE_LOOKUP
import android.net.DnsResolver.FLAG_NO_CACHE_STORE
import android.net.DnsResolver.FLAG_NO_RETRY
import com.maengs.dns.data.DnsQueryFlag.NO_CACHE_LOOKUP
import com.maengs.dns.data.DnsQueryFlag.NO_CACHE_STORE
import com.maengs.dns.data.DnsQueryFlag.NO_RETRY
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.EnumSet

class DnsQueryFlagTest : FunSpec({

    test("id should be a value of FLAG_* constant") {
        NO_RETRY.id shouldBe FLAG_NO_RETRY
        NO_CACHE_STORE.id shouldBe FLAG_NO_CACHE_STORE
        NO_CACHE_LOOKUP.id shouldBe FLAG_NO_CACHE_LOOKUP
    }

    test("enum set test") {
        EnumSet.noneOf(DnsQueryFlag::class.java).toFlags() shouldBe FLAG_EMPTY
        EnumSet.allOf(DnsQueryFlag::class.java)
            .toFlags() shouldBe (FLAG_NO_RETRY or FLAG_NO_CACHE_STORE or FLAG_NO_CACHE_LOOKUP)

        EnumSet.of(NO_RETRY).toFlags() shouldBe FLAG_NO_RETRY
        EnumSet.of(NO_CACHE_STORE).toFlags() shouldBe FLAG_NO_CACHE_STORE
        EnumSet.of(NO_CACHE_LOOKUP).toFlags() shouldBe FLAG_NO_CACHE_LOOKUP

        EnumSet.of(NO_RETRY, NO_CACHE_STORE)
            .toFlags() shouldBe (FLAG_NO_RETRY or FLAG_NO_CACHE_STORE)
        EnumSet.of(NO_RETRY, NO_CACHE_LOOKUP)
            .toFlags() shouldBe (FLAG_NO_RETRY or FLAG_NO_CACHE_LOOKUP)
        EnumSet.of(NO_CACHE_STORE, NO_CACHE_LOOKUP)
            .toFlags() shouldBe (FLAG_NO_CACHE_STORE or FLAG_NO_CACHE_LOOKUP)

        EnumSet.of(NO_RETRY, NO_CACHE_STORE, NO_CACHE_LOOKUP)
            .toFlags() shouldBe (FLAG_NO_RETRY or FLAG_NO_CACHE_STORE or FLAG_NO_CACHE_LOOKUP)
    }
})
