package com.yandex.divkit.demo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yandex.divkit.demo.data.entities.PhPlusDB
import kotlinx.coroutines.flow.Flow

@Dao
interface PhPlusDBDao {

    @Query("SELECT * FROM PhPlus")
    fun getAllBasicInfo(): Flow<List<PhPlusDB>>

    @Query("SELECT * FROM PhPlus WHERE `key`=:key")
    fun getPhPlusBykey(key: String): Flow<List<PhPlusDB>>

    @Query("SELECT * FROM PhPlus WHERE `key` LIKE :key")
    fun getVtOfflineReports(key: String): Flow<List<PhPlusDB>>

    @Query("SELECT * FROM PhPlus WHERE `key`=:key")
    fun getValueByKey(key: String):PhPlusDB

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(phPlus: PhPlusDB)

     @Query("DELETE FROM PhPlus WHERE `key` = :key")
     fun deleteItemFromDb(key: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(phPlus: List<PhPlusDB>)


}