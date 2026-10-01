package pe.edu.upeu.pharmamobil.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.data.remote.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest
import pe.edu.upeu.pharmamobil.domain.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

expect val platformModule: Module

// id real de la categoria "Medicamentos" creada en PharmaSoft (POST /api/v1/categorias)
private const val CATEGORIA_POR_DEFECTO = 1L

val networkModule = module {
    single<HttpClient> { crearHttpClient(get<HttpClientEngine>()) }
    single { ProductoApi(get()) }
}

val repositoryModule = module {
    single<ProductoRepository> {
        ProductoRepositorioRest(get(), CATEGORIA_POR_DEFECTO)
    }
}

val useCaseModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ActualizarProductoUseCase(get()) }
    factory { EliminarProductoUseCase(get()) }
}

val viewModelModule = module {
    viewModelOf(::ProductoViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            platformModule,
            networkModule,
            repositoryModule,
            useCaseModule,
            viewModelModule
        )
    }
}