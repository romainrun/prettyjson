package re.weare.app.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import re.weare.app.data.database.AppDatabase
import re.weare.app.data.preferences.PreferencesManager
import re.weare.app.data.repository.ReusableObjectRepository
import re.weare.app.data.repository.SavedJsonRepository
import re.weare.app.data.repository.UrlLoader
import re.weare.app.ui.viewmodel.*

val appModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { SavedJsonRepository(get<AppDatabase>()) }
    single { ReusableObjectRepository(get<AppDatabase>()) }
    single { PreferencesManager(androidContext()) }
    single { UrlLoader() }
    
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { SavedJsonViewModel(get()) }
    viewModel { ReusableObjectViewModel(get()) }
    viewModel { JsonBuilderViewModel(get()) }
}

