package com.yandex.divkit.demo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.yandex.divkit.demo.data.Constants
import com.yandex.divkit.demo.data.EncryptionConstant
import com.yandex.divkit.demo.data.local.AppDatabase
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import java.lang.reflect.Type
abstract class SingletonObjects {


    companion object {
//        private const val Database_NAME = "words.db"
        @Volatile
        private var DB_INSTANCE: AppDatabase? = null
        @Volatile
        private var GSON_INSTANCE: Gson? = null
        @Volatile
        private var OKHTTPCLIENT_INSTANCE: OkHttpClient? = null
        @Volatile
        private var RETROFIT_INSTANCE: Retrofit? = null
        @Volatile
        private var SHARED_PREF_INSTANCE: SharedPreferences? = null
        @Volatile
        private var ANDROID_ID_INSTANCE: String? = null
        class MapDeserializer : JsonDeserializer<Map<String, String>> {
            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): Map<String, String> {
                val result = mutableMapOf<String, String>()
                json.asJsonObject.entrySet().forEach {
                    result[it.key] = it.value.asString
                }
                return result
            }
        }
        fun getDbInstance(context: Context): AppDatabase {

            synchronized(this) {
                var dbInstance = DB_INSTANCE

                if (dbInstance == null) {
                    dbInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        EncryptionConstant.ConstantDataBase.DATABASE_NAME
                    ).fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()

                    DB_INSTANCE = dbInstance
                }
                return dbInstance
            }
        }
        fun getGsonInstance(): Gson? {

            synchronized(this) {
                var gsonInstance = GSON_INSTANCE

                if (gsonInstance == null) {
                    gsonInstance = GsonBuilder()
                        .registerTypeAdapter(
                            object : TypeToken<Map<String, String>>() {}.type,
                            MapDeserializer()
                        )
                        .create()

                    GSON_INSTANCE = gsonInstance
                }
                return gsonInstance
            }
        }
        fun getOkHttpClientInstance(): OkHttpClient? {

            synchronized(this) {
                var okHttpClientInstance = OKHTTPCLIENT_INSTANCE

                if (okHttpClientInstance == null) {
                    okHttpClientInstance = OkHttpClient.Builder()
                        .readTimeout(180, TimeUnit.SECONDS)
                        .connectTimeout(180, TimeUnit.SECONDS)
                        .build()

                    OKHTTPCLIENT_INSTANCE = okHttpClientInstance
                }
                return okHttpClientInstance
            }
        }
        fun retrofitInstance(): Retrofit? {

            synchronized(this) {
                var retrofitInstance = RETROFIT_INSTANCE

                if (retrofitInstance == null) {
                    retrofitInstance = Retrofit.Builder()
                        .client(getOkHttpClientInstance())
                        .baseUrl(Constants.BASE_URL)
                        .addConverterFactory(ScalarsConverterFactory.create()) // ← الزامی برای String
                        .addConverterFactory(GsonConverterFactory.create(getGsonInstance()))
                        .build()

                    RETROFIT_INSTANCE = retrofitInstance
                }
                return retrofitInstance
            }
        }
        fun sharedPreferencesInstance(context: Context): SharedPreferences? {

            synchronized(this) {
                var sharedPreferencesInstance = SHARED_PREF_INSTANCE

                if (sharedPreferencesInstance == null) {
                    sharedPreferencesInstance =  context.getSharedPreferences(
                        EncryptionConstant.ConstantSharedPreferences.SHARED_PREFS_NAME,
                        Context.MODE_PRIVATE
                    )

                    SHARED_PREF_INSTANCE = sharedPreferencesInstance
                }
                return sharedPreferencesInstance
            }
        }
        @SuppressLint("HardwareIds")
        fun androidIdInstance(context: Context): String? {

            synchronized(this) {
                var androidIdInstance = ANDROID_ID_INSTANCE

                if (androidIdInstance == null) {
                    androidIdInstance =   Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                    ANDROID_ID_INSTANCE = androidIdInstance
                }
                return androidIdInstance
            }
        }
    }
}