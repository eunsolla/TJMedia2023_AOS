package com.verse.app.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData

/**
 * Description : 네트워크 상태 체크
 *
 * Created by jhlee on 2023-01-01
 */
class NetworkConnection(
    private val context: Context
): LiveData<Boolean>() {

    private var manager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var callBack: ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()

        postValue(updateConnection())

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                manager.registerDefaultNetworkCallback(connectivityManagerCallback())
            }
            else -> {
                val request = NetworkRequest.Builder().build()
                manager.registerNetworkCallback(request, connectivityManagerCallback())
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        manager.unregisterNetworkCallback(callBack)
    }

    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        callBack = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                postValue(false)
            }
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }

        }
        return callBack
    }

    private fun updateConnection(): Boolean {
        val networkCapabilities = manager.activeNetwork ?: return false
        val actNw = manager.getNetworkCapabilities(networkCapabilities) ?: return false
        val  result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }
}