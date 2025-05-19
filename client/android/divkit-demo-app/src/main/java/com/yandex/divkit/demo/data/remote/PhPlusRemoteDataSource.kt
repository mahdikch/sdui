package com.yandex.divkit.demo.data.remote


import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import javax.inject.Inject

class PhPlusRemoteDataSource @Inject constructor(
    private val phPlusService: PhPlusApi
) : MyBaseDataSource() {
    suspend fun phPlus(phId:String,request: HashMap<String,String>) =
        getResult { phPlusService.phPlus(phId,request) }
    suspend fun getList(listId: String) =
        getResult { phPlusService.getList(listId) }
}