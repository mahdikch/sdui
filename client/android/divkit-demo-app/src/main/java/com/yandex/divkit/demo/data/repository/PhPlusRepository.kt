package com.yandex.divkit.demo.data.repository

import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import com.yandex.divkit.demo.data.local.PhPlusDBDao
import com.yandex.divkit.demo.data.remote.PhPlusRemoteDataSource
import ir.nrdc.arbaeeintraficcontrol.util.myPerformGetOperation

import javax.inject.Inject

class PhPlusRepository @Inject constructor(
    private val remoteDataSource: PhPlusRemoteDataSource,
    private val sharePref: SharePref,
    private val phPlusDBDao: PhPlusDBDao

) {
    fun phPlus(request: MutableMap<String,String>) = myPerformGetOperation("phPlus", true) {
        remoteDataSource.phPlus(request)
    }
//
    suspend fun insertItem(phPlusDB:PhPlusDB) {
        this.phPlusDBDao.insert(phPlusDB)
    }
    suspend fun insertList(phPlusDBs:List<PhPlusDB>) {
        this.phPlusDBDao.insertList(phPlusDBs)
    }
//
    fun getList(listId: String) = myPerformGetOperation("List", false) {
        remoteDataSource.getList(listId)
    }
//
//
    fun getByKey(key:String) = this.phPlusDBDao.getPhPlusBykey(key)
    fun getVtOfflineReports(key:String) = this.phPlusDBDao.getVtOfflineReports(key)
    fun getValueByKey(key:String) = this.phPlusDBDao.getValueByKey(key)
    fun getall() = this.phPlusDBDao.getAllBasicInfo()

    var tokenPh: String?
        get() = sharePref.tokenPh
        set(value) {
            sharePref.tokenPh = value
        }

    var keyPh: String?
        get() = sharePref.keyPh
        set(value) {
            sharePref.keyPh = value
        }

    var firstName: String?
        get() = sharePref.firstName
        set(value) {
            sharePref.firstName = value
        }

    var lastName: String?
        get() = sharePref.lastName
        set(value) {
            sharePref.lastName = value
        }

    var nationalId: String?
        get() = sharePref.nationalId
        set(value) {
            sharePref.nationalId = value
        }

    var phoneNumber: String?
        get() = sharePref.phoneNumber
        set(value) {
            sharePref.phoneNumber = value
        }

    var policeCode: String?
        get() = sharePref.policeCode
        set(value) {
            sharePref.policeCode = value
        }

    var fkProvinceId: String?
        get() = sharePref.fkProvinceId
        set(value) {
            sharePref.fkProvinceId = value
        }

    var userId: String?
        get() = sharePref.userId
        set(value) {
            sharePref.userId = value
        }

    var username: String?
        get() = sharePref.username
        set(value) {
            sharePref.username = value
        }

    var personId: String?
        get() = sharePref.personId
        set(value) {
            sharePref.personId = value
        }
//
//

}