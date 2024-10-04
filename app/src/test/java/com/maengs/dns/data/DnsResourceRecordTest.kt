package com.maengs.dns.data

import android.net.DnsResolver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DnsResourceRecordTest : FunSpec({
    test("id should be a value of TYPE_* constant") {
        DnsResourceRecord.TYPE_A.id shouldBe DnsResolver.TYPE_A
        DnsResourceRecord.TYPE_AAA.id shouldBe DnsResolver.TYPE_AAAA
    }
})
