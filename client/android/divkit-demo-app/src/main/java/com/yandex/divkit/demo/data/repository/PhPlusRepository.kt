package com.yandex.divkit.demo.data.repository

import com.yandex.divkit.demo.data.SharePref
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.entities.req.RequestPhPlus
import com.yandex.divkit.demo.data.local.PhPlusDBDao
import com.yandex.divkit.demo.data.remote.PhPlusRemoteDataSource
import ir.nrdc.arbaeeintraficcontrol.util.myPerformGetOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

import javax.inject.Inject

class PhPlusRepository @Inject constructor(
    private val remoteDataSource: PhPlusRemoteDataSource,
    private val sharePref: SharePref,
    private val phPlusDBDao: PhPlusDBDao

) {

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun phPlus(phId:String,request: HashMap<String,String>) = myPerformGetOperation("phPlus", true) {
        remoteDataSource.phPlus(phId,request)
    }

    /** Direct/suspend call for in-sheet paged lists (avoids MehdiActivity global observer). */
    suspend fun phPlusDirect(phId: String, request: HashMap<String, String>) =
        remoteDataSource.phPlus(phId, request)

    suspend fun phPlusRawBody(phId: String, jsonBody: String) =
        remoteDataSource.phPlusRawBody(phId, jsonBody)

    suspend fun phPlusWithFiles(
        phId: String,
        requestScreenJson: String,
        files: List<okhttp3.MultipartBody.Part>,
    ) = remoteDataSource.phPlusWithFiles(phId, requestScreenJson, files)
//
suspend fun insertItem(phPlusDB: PhPlusDB) {
        phPlusDBDao.insert(phPlusDB)
    }
    fun insertItemAsync(phPlusDB: PhPlusDB) {
        repoScope.launch {
            phPlusDBDao.insert(phPlusDB)
        }
    }
    fun deleteItemFromDb(key: String) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(key)
        }
    }

    fun deleteAccidentArchiveAndDrafts(
        archiveKey: String,
        accidentId: String,
    ) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(archiveKey)
            if (accidentId.isBlank()) return@launch

            listOf("page1", "page2", "page3", "page4").forEach { page ->
                phPlusDBDao.deleteItemFromDb("draft/$accidentId/$page")
            }
            phPlusDBDao.getOfflineReportsList("%/pedestrians/$accidentId/%").forEach { row ->
                val rowKey = row.key ?: return@forEach
                phPlusDBDao.deleteItemFromDb(rowKey)
                val pedestrianId = runCatching {
                    JSONObject(row.value.orEmpty()).optString("pedestrian_id")
                }.getOrDefault("")
                if (pedestrianId.isNotBlank()) {
                    phPlusDBDao.deleteItemFromDb("draft/$pedestrianId/page1")
                }
            }
            phPlusDBDao.getOfflineReportsList("%/organizations/$accidentId/%").forEach { row ->
                val rowKey = row.key ?: return@forEach
                phPlusDBDao.deleteItemFromDb(rowKey)
                val organizationId = runCatching {
                    JSONObject(row.value.orEmpty()).optString("organization_id")
                }.getOrDefault("")
                if (organizationId.isNotBlank()) {
                    phPlusDBDao.deleteItemFromDb("draft/$organizationId/page1")
                }
            }
        }
    }

    suspend fun upsertAccidentArchiveEntry(
        username: String,
        archivePath: String,
        accidentId: String,
        archiveKey: String,
        json: String,
    ) {
        phPlusDBDao.insert(PhPlusDB(null, archiveKey, json))
        phPlusDBDao.getOfflineReportsList("%$username/$archivePath%").forEach { row ->
            val rowKey = row.key ?: return@forEach
            if (rowKey == archiveKey) return@forEach
            val rowAccidentId = runCatching {
                JSONObject(row.value.orEmpty()).optString("accident_id")
            }.getOrDefault("")
            if (rowAccidentId == accidentId) {
                phPlusDBDao.deleteItemFromDb(rowKey)
            }
        }
    }

    suspend fun upsertVehicleArchiveEntry(
        username: String,
        accidentId: String,
        vehicleId: String,
        archiveKey: String,
        json: String,
    ) {
        phPlusDBDao.insert(PhPlusDB(null, archiveKey, json))
        phPlusDBDao.getOfflineReportsList("%$username/vehicles/$accidentId%").forEach { row ->
            val rowKey = row.key ?: return@forEach
            if (rowKey == archiveKey) return@forEach
            val rowVehicleId = runCatching {
                JSONObject(row.value.orEmpty()).optString("vehicle_id")
            }.getOrDefault("")
            if (rowVehicleId == vehicleId) {
                phPlusDBDao.deleteItemFromDb(rowKey)
            }
        }
    }

    suspend fun upsertPassengerArchiveEntry(
        username: String,
        vehicleId: String,
        passengerId: String,
        archiveKey: String,
        json: String,
    ) {
        phPlusDBDao.insert(PhPlusDB(null, archiveKey, json))
        phPlusDBDao.getOfflineReportsList("%$username/passengers/$vehicleId%").forEach { row ->
            val rowKey = row.key ?: return@forEach
            if (rowKey == archiveKey) return@forEach
            val rowPassengerId = runCatching {
                JSONObject(row.value.orEmpty()).optString("passenger_id")
            }.getOrDefault("")
            if (rowPassengerId == passengerId) {
                phPlusDBDao.deleteItemFromDb(rowKey)
            }
        }
    }

    suspend fun upsertPedestrianArchiveEntry(
        username: String,
        accidentId: String,
        pedestrianId: String,
        archiveKey: String,
        json: String,
    ) {
        phPlusDBDao.insert(PhPlusDB(null, archiveKey, json))
        phPlusDBDao.getOfflineReportsList("%$username/pedestrians/$accidentId%").forEach { row ->
            val rowKey = row.key ?: return@forEach
            if (rowKey == archiveKey) return@forEach
            val rowPedestrianId = runCatching {
                JSONObject(row.value.orEmpty()).optString("pedestrian_id")
            }.getOrDefault("")
            if (rowPedestrianId == pedestrianId) {
                phPlusDBDao.deleteItemFromDb(rowKey)
            }
        }
    }

    suspend fun upsertOrganizationArchiveEntry(
        username: String,
        accidentId: String,
        organizationId: String,
        archiveKey: String,
        json: String,
    ) {
        phPlusDBDao.insert(PhPlusDB(null, archiveKey, json))
        phPlusDBDao.getOfflineReportsList("%$username/organizations/$accidentId%").forEach { row ->
            val rowKey = row.key ?: return@forEach
            if (rowKey == archiveKey) return@forEach
            val rowOrganizationId = runCatching {
                JSONObject(row.value.orEmpty()).optString("organization_id")
            }.getOrDefault("")
            if (rowOrganizationId == organizationId) {
                phPlusDBDao.deleteItemFromDb(rowKey)
            }
        }
    }

    suspend fun upsertLivestockArchiveEntry(
        username: String,
        accidentId: String,
        livestockId: String,
        archiveKey: String,
        json: String,
    ) {
        phPlusDBDao.insert(PhPlusDB(null, archiveKey, json))
        phPlusDBDao.getOfflineReportsList("%$username/livestock/$accidentId%").forEach { row ->
            val rowKey = row.key ?: return@forEach
            if (rowKey == archiveKey) return@forEach
            val rowLivestockId = runCatching {
                JSONObject(row.value.orEmpty()).optString("livestock_id")
            }.getOrDefault("")
            if (rowLivestockId == livestockId) {
                phPlusDBDao.deleteItemFromDb(rowKey)
            }
        }
    }

    fun deleteLivestockArchive(archiveKey: String) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(archiveKey)
        }
    }

    fun deleteVehicleArchiveAndDrafts(
        archiveKey: String,
        vehicleId: String,
    ) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(archiveKey)
            if (vehicleId.isBlank()) return@launch
            listOf("page1", "page2", "page3", "page4").forEach { page ->
                phPlusDBDao.deleteItemFromDb("draft/$vehicleId/$page")
            }
            phPlusDBDao.getOfflineReportsList("%/passengers/$vehicleId/%").forEach { row ->
                val rowKey = row.key ?: return@forEach
                phPlusDBDao.deleteItemFromDb(rowKey)
                val passengerId = runCatching {
                    JSONObject(row.value.orEmpty()).optString("passenger_id")
                }.getOrDefault("")
                if (passengerId.isNotBlank()) {
                    phPlusDBDao.deleteItemFromDb("draft/$passengerId/page1")
                }
            }
        }
    }

    fun deletePassengerArchiveAndDrafts(
        archiveKey: String,
        passengerId: String,
    ) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(archiveKey)
            if (passengerId.isBlank()) return@launch
            phPlusDBDao.deleteItemFromDb("draft/$passengerId/page1")
        }
    }

    fun deletePedestrianArchiveAndDrafts(
        archiveKey: String,
        pedestrianId: String,
    ) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(archiveKey)
            if (pedestrianId.isBlank()) return@launch
            phPlusDBDao.deleteItemFromDb("draft/$pedestrianId/page1")
        }
    }

    fun deleteOrganizationArchiveAndDrafts(
        archiveKey: String,
        organizationId: String,
    ) {
        repoScope.launch {
            phPlusDBDao.deleteItemFromDb(archiveKey)
            if (organizationId.isBlank()) return@launch
            phPlusDBDao.deleteItemFromDb("draft/$organizationId/page1")
        }
    }
    suspend fun insertList(phPlusDBs:List<PhPlusDB>) {
        phPlusDBDao.insertList(phPlusDBs)
    }
    fun insertListAsync(phPlusDBs: List<PhPlusDB>) {
        if (phPlusDBs.isEmpty()) return
        repoScope.launch {
            phPlusDBDao.insertList(phPlusDBs)
        }
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
    fun getOfflineReportsListSync(pattern: String): List<PhPlusDB> =
        runBlocking(Dispatchers.IO) {
            phPlusDBDao.getOfflineReportsList(pattern)
        }
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