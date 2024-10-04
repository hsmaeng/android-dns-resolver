package com.maengs.dns.di

import android.net.ConnectivityManager
import com.maengs.dns.data.DnsResolverRepository
import com.maengs.dns.data.DnsResolverRepositoryImpl
import com.maengs.dns.ui.DnsResolverViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { androidContext().getSystemService(ConnectivityManager::class.java) }
    singleOf(::DnsResolverRepositoryImpl) {
        bind<DnsResolverRepository>()
        createdAtStart()
    }
    viewModelOf(::DnsResolverViewModel)
}
