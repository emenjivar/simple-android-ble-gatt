package com.emenjivar.simplebleclient.wifi

/**
 * @param ssid The network name displayed in WIFI settings.
 * @param rssi The strength indicator for the network
 */
data class WifiNetwork(
    val ssid: String,
    val rssi: Int
)

sealed class StateResult<out T> {
    object Idle : StateResult<Nothing>()
    object Loading : StateResult<Nothing>()
    class Success<out T>(val data: T) : StateResult<T>()
}