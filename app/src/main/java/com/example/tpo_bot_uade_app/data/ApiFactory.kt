package com.example.tpo_bot_uade_app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Agrega "http://" si falta y "/" al final, que Retrofit exige.
fun normalizarUrl(ip: String): String {
    var url = ip.trim()
    if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
    if (!url.endsWith("/")) url += "/"
    return url
}

// Timeouts cortos: si el backend no responde queremos enterarnos rápido.
fun crearApi(baseUrl: String): RobotApi {
    val cliente = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    return Retrofit.Builder()
        .baseUrl(normalizarUrl(baseUrl))
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RobotApi::class.java)
}
