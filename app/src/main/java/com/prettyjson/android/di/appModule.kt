package com.prettyjson.android.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.prettyjson.android.data.database.AppDatabase
import com.prettyjson.android.data.preferences.PreferencesManager
import com.prettyjson.android.data.preferences.PremiumManager
import com.prettyjson.android.data.billing.ProManager
import com.prettyjson.android.data.repository.DataBucketRepository
import com.prettyjson.android.data.repository.ReusableObjectRepository
import com.prettyjson.android.data.repository.SavedJsonRepository
import com.prettyjson.android.data.repository.UrlLoader
import com.prettyjson.android.ui.viewmodel.*

val appModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { SavedJsonRepository(get<AppDatabase>()) }
    single { ReusableObjectRepository(get<AppDatabase>()) }
    single { DataBucketRepository(get<AppDatabase>()) }
    single { PreferencesManager(androidContext()) }
    single { PremiumManager(androidContext()) }
    single { ProManager(androidContext(), get()) }
    single { UrlLoader() }
    
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { SavedJsonViewModel(get()) }
    viewModel { ReusableObjectViewModel(get()) }
    viewModel { DataBucketViewModel(get()) }
    viewModel { JsonBuilderViewModel(get()) }
    viewModel { PremiumViewModel(get()) }
    viewModel { ProViewModel(get()) }
}

