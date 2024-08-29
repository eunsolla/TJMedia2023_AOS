package com.verse.app.di

import com.verse.app.gallery.core.GalleryProvider
import com.verse.app.gallery.internal.GalleryProviderImpl
import com.verse.app.repository.preferences.BasePref
import com.verse.app.repository.preferences.BasePrefImpl
import com.verse.app.repository.tcp.NettyClient
import com.verse.app.repository.tcp.NettyClientImpl
import com.verse.app.utility.*
import com.verse.app.utility.exo.ExoProvider
import com.verse.app.utility.exo.ExoProviderProviderImpl
import com.verse.app.utility.manager.LoginManager
import com.verse.app.utility.manager.LoginManagerImpl
import com.verse.app.utility.provider.DeviceProvider
import com.verse.app.utility.provider.DeviceProviderImpl
import com.verse.app.utility.provider.DispatcherProvider
import com.verse.app.utility.provider.DispatcherProviderImpl
import com.verse.app.utility.provider.FeedRefreshProvider
import com.verse.app.utility.provider.FeedRefreshProviderImpl
import com.verse.app.utility.provider.FileProvider
import com.verse.app.utility.provider.FileProviderImpl
import com.verse.app.utility.provider.NetworkConnectionProvider
import com.verse.app.utility.provider.NetworkConnectionProviderImpl
import com.verse.app.utility.provider.ResourceProvider
import com.verse.app.utility.provider.ResourceProviderImpl
import com.verse.app.utility.provider.SingPathProvider
import com.verse.app.utility.provider.SingPathProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 앱  모듈
 * Created by jhlee on 2023-02-01
 */
@InstallIn(SingletonComponent::class)
@Module
internal abstract class AppModule {

    @Singleton
    @Binds
    abstract fun bindBasePref(basePref: BasePrefImpl): BasePref

    @Binds
    abstract fun bindResourceProvider(resourceProvider: ResourceProviderImpl): ResourceProvider

    @Binds
    abstract fun bindDeviceProvider(deviceProvider: DeviceProviderImpl): DeviceProvider

    @Singleton
    @Binds
    abstract fun bindLoginManager(loginManager: LoginManagerImpl): LoginManager

    @Singleton
    @Binds
    abstract fun bindNetworkConnection(networkConnectionProvider: NetworkConnectionProviderImpl): NetworkConnectionProvider

    @Singleton
    @Binds
    abstract fun bindExoProvider(exoProvider: ExoProviderProviderImpl): ExoProvider

    @Singleton
    @Binds
    abstract fun bindFileProvider(fileProvider: FileProviderImpl): FileProvider

    @Binds
    abstract fun bindDispatcherProvider(dispatcherProvider: DispatcherProviderImpl): DispatcherProvider

    @Binds
    abstract fun bindSingPathProvider(singPathProvider: SingPathProviderImpl): SingPathProvider

    @Binds
    abstract fun bindGalleryProvider(impl: GalleryProviderImpl): GalleryProvider

    @Binds
    abstract fun bindWebLinkProvider(impl: WebLinkProviderImpl): WebLinkProvider

    @Singleton
    @Binds
    abstract fun bindTcpService(impl: NettyClientImpl): NettyClient

    @Singleton
    @Binds
    abstract fun bindFeedRefreshProvider(impl: FeedRefreshProviderImpl): FeedRefreshProvider

}