package com.denreyes.githubusers.repository.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class ApiConnector {
	private var builder: Retrofit.Builder
	private var retrofit: Retrofit
	private var logging: HttpLoggingInterceptor
	private var httpClient: OkHttpClient.Builder

	private constructor() {
		val baseUrl: String = GithubApi.instance!!.baseUrl
		val connectionTimeout: Long = GithubApi.instance!!.connectionTimeout
		val writeTimeout: Long = GithubApi.instance!!.writeTimeout
		val readTimeout: Long = GithubApi.instance!!.readTimeout
		val gson = GsonBuilder().setPrettyPrinting().create()
		builder = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create(gson))
		retrofit = builder.build()
		logging = HttpLoggingInterceptor().setLevel(Level.BODY)
		httpClient = OkHttpClient.Builder().connectTimeout(connectionTimeout, TimeUnit.MINUTES).writeTimeout(writeTimeout, TimeUnit.MINUTES).readTimeout(readTimeout, TimeUnit.MINUTES)
	}

	private constructor(connectionTimeout: Long, writeTimeout: Long, readTimeout: Long) {
		val baseUrl: String = GithubApi.instance!!.baseUrl
		val gson = GsonBuilder().setPrettyPrinting().create()
		builder = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create(gson))
		retrofit = builder.build()
		logging = HttpLoggingInterceptor().setLevel(Level.BODY)
		httpClient = OkHttpClient.Builder().connectTimeout(connectionTimeout, TimeUnit.MINUTES).writeTimeout(writeTimeout, TimeUnit.MINUTES).readTimeout(readTimeout, TimeUnit.MINUTES)
	}

	fun <S> createService(serviceClass: Class<S>?): S {
		if (!httpClient.interceptors().contains(logging)) {
			httpClient.addInterceptor(logging)
			val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
				override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
				override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
				override fun getAcceptedIssuers(): Array<X509Certificate> {
					return arrayOf<X509Certificate>()
				}

			})
			try {
				val sslContext = SSLContext.getInstance("SSL")
				sslContext.init(null as Array<KeyManager?>?, trustAllCerts, SecureRandom())
				val sslSocketFactory = sslContext.socketFactory
				httpClient.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
				httpClient.hostnameVerifier(HostnameVerifier { hostname, session -> true })
			} catch (var5: NoSuchAlgorithmException) {
				var5.printStackTrace()
			} catch (var6: KeyManagementException) {
				var6.printStackTrace()
			}
			builder.client(httpClient.build())
			retrofit = builder.build()
		}
		return retrofit.create(serviceClass)
	}

	companion object {
		var instance: ApiConnector? = null
			private set

		fun init() {
			instance = ApiConnector()
		}

		fun init(connectionTimeout: Long, writeTimeout: Long, readTimeout: Long) {
			instance = ApiConnector(connectionTimeout, writeTimeout, readTimeout)
		}
	}
}
