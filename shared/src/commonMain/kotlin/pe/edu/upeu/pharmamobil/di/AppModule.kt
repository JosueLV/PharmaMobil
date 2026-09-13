package pe.edu.upeu.pharmamobil.di

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

expect val platformModule: Module

val repositoryModule = module {
    single<ProductoRepository> { ProductoRepositorioEnMemoria() }
}

val useCaseModule = module {
    factory { RegistrarProductoUseCase(get()) }
}

val viewModelModule = module {
    viewModelOf(::ProductoViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            platformModule,
            repositoryModule,
            useCaseModule,
            viewModelModule
        )
    }
}