package com.emenjivar.simplebleclient.di

import android.content.Context
import com.emenjivar.simplebleclient.ble.BleNotifications
import com.emenjivar.simplebleclient.ble.BleNotificationsImp
import com.emenjivar.simplebleclient.ble.CustomBluetoothManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun providesBleNotifications(): BleNotifications = BleNotificationsImp()

    @Provides
    @Singleton
    fun provideCustomBluetoothManager(
        @ApplicationContext context: Context,
        bleNotifications: BleNotifications,
    ) = CustomBluetoothManager(
        context = context,
        bleNotifications = bleNotifications
    )
}