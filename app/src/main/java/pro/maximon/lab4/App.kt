package pro.maximon.lab4

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pro.maximon.lab4.data.local.AppDatabase
import pro.maximon.lab4.data.repository.StudentRepository
import pro.maximon.lab4.data.repository.StudentRepositoryImpl
import pro.maximon.lab4.viewmodels.MainViewModel

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            single { AppDatabase.getInstance(get()) }
            single { get<AppDatabase>().studentDao() }
            single<StudentRepository> { StudentRepositoryImpl(get()) }

            viewModel { MainViewModel(get()) }
        }

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }
}