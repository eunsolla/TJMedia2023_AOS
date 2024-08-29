package com.verse.app.di

import com.verse.app.utility.provider.SuperpoweredProvider
import com.verse.app.utility.provider.SuperpoweredProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Superpowered 모듈
 * Created by jhlee on 2023-02-01
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class SuperpoweredModule {
    @Singleton
    @Binds
    abstract fun bindSuperpoweredProvider(superpoweredProvider: SuperpoweredProviderImpl): SuperpoweredProvider

}
