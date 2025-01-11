package com.yandex.divkit.demo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yandex.divkit.demo.data.entities.PhPlusDB

@Database(entities = [PhPlusDB::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun phPlusDBDao(): PhPlusDBDao

//
//    companion object {
//        private const val Database_NAME = "words.db"
//
//        /**
//         * As we need only one instance of db in our app will use to store
//         * This is to avoid memory leaks in android when there exist multiple instances of db
//         */
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getInstance(context: Context): AppDatabase {
//
//            synchronized(this) {
//                var instance = INSTANCE
//
//                if (instance == null) {
//                    instance = Room.databaseBuilder(
//                        context.applicationContext,
//                        AppDatabase::class.java,
//                        Database_NAME
//                    ).fallbackToDestructiveMigration()
//                        .allowMainThreadQueries()
//                        .build()
//
//                    INSTANCE = instance
//                }
//                return instance
//            }
//        }
//    }
}