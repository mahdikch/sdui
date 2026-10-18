package com.yandex.divkit.demo.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.yandex.divkit.demo.data.entities.PhPlusDB
import com.yandex.divkit.demo.data.remote.Resource
import com.yandex.divkit.demo.data.repository.PhPlusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.util.HashMap
import javax.inject.Inject

//@HiltViewModel
class MehdiViewModel @Inject constructor(
    private val repository: PhPlusRepository
) : ViewModel() {
    private val _phPlusRequest = MutableLiveData<Pair<String, HashMap<String, String>>>()

    fun setphPlusRequest(phId: String,request: HashMap<String, String>) {
        _phPlusRequest.postValue(Pair(phId, request))
    }

    private val _phPlus = _phPlusRequest.switchMap { (phId, request) ->
        repository.phPlus(phId,request)
    }

    val phPlus: LiveData<Resource<Map<String, String>>> = _phPlus

    suspend fun uploadFilesAndCallService(
        phId: String,
        requestScreenJson: String,
        files: List<MultipartBody.Part>,
    ): Resource<Map<String, String>> = withContext(Dispatchers.IO) {
        repository.phPlusWithFiles(phId, requestScreenJson, files)
    }


//    fun insertListToDb(phPlusDBs: List<PhPlusDB>) = viewModelScope.launch(Dispatchers.IO) {
//        repository.insertList(phPlusDBs)
//    }

//    fun getPage(key: String) = viewModelScope.launch(Dispatchers.IO) {
//        repository.getByKey(key)
//    }
    fun getPage(key: String) = repository.getByKey(key).asLiveData()
    fun getVtOfflineReports(key: String) = repository.getVtOfflineReports(key).asLiveData()
    fun getJson(key: String) = repository.getByKey(key).asLiveData()
    fun getVariable(key: String) = repository.getByKey(key).asLiveData()
    fun getRequestParam(key: String) = repository.getByKey(key).asLiveData()
    fun getValueByKey(key: String) = repository.getValueByKey(key)
    fun getOfflineReportsListSync(pattern: String) = repository.getOfflineReportsListSync(pattern)
    fun getpatch(key: String) = repository.getByKey(key).asLiveData()
    fun getall() = repository.getall().asLiveData()

    fun deleteItemFromDb(key: String)  {
        repository.deleteItemFromDb(key)
    }

    fun deleteAccidentArchiveAndDrafts(
        archiveKey: String,
        accidentId: String,
    ) {
        repository.deleteAccidentArchiveAndDrafts(archiveKey, accidentId)
    }

    fun upsertAccidentArchiveEntry(
        username: String,
        archivePath: String,
        accidentId: String,
        archiveKey: String,
        json: String,
    ) {
        runBlocking(Dispatchers.IO) {
            repository.upsertAccidentArchiveEntry(username, archivePath, accidentId, archiveKey, json)
        }
    }

    fun upsertVehicleArchiveEntry(
        username: String,
        accidentId: String,
        vehicleId: String,
        archiveKey: String,
        json: String,
    ) {
        runBlocking(Dispatchers.IO) {
            repository.upsertVehicleArchiveEntry(username, accidentId, vehicleId, archiveKey, json)
        }
    }

    fun upsertPassengerArchiveEntry(
        username: String,
        vehicleId: String,
        passengerId: String,
        archiveKey: String,
        json: String,
    ) {
        runBlocking(Dispatchers.IO) {
            repository.upsertPassengerArchiveEntry(username, vehicleId, passengerId, archiveKey, json)
        }
    }

    fun upsertPedestrianArchiveEntry(
        username: String,
        accidentId: String,
        pedestrianId: String,
        archiveKey: String,
        json: String,
    ) {
        runBlocking(Dispatchers.IO) {
            repository.upsertPedestrianArchiveEntry(username, accidentId, pedestrianId, archiveKey, json)
        }
    }

    fun upsertOrganizationArchiveEntry(
        username: String,
        accidentId: String,
        organizationId: String,
        archiveKey: String,
        json: String,
    ) {
        runBlocking(Dispatchers.IO) {
            repository.upsertOrganizationArchiveEntry(username, accidentId, organizationId, archiveKey, json)
        }
    }

    fun upsertLivestockArchiveEntry(
        username: String,
        accidentId: String,
        livestockId: String,
        archiveKey: String,
        json: String,
    ) {
        runBlocking(Dispatchers.IO) {
            repository.upsertLivestockArchiveEntry(username, accidentId, livestockId, archiveKey, json)
        }
    }

    fun deleteLivestockArchive(archiveKey: String) {
        repository.deleteLivestockArchive(archiveKey)
    }

    fun deleteVehicleArchiveAndDrafts(
        archiveKey: String,
        vehicleId: String,
    ) {
        repository.deleteVehicleArchiveAndDrafts(archiveKey, vehicleId)
    }

    fun deletePassengerArchiveAndDrafts(
        archiveKey: String,
        passengerId: String,
    ) {
        repository.deletePassengerArchiveAndDrafts(archiveKey, passengerId)
    }

    fun deletePedestrianArchiveAndDrafts(
        archiveKey: String,
        pedestrianId: String,
    ) {
        repository.deletePedestrianArchiveAndDrafts(archiveKey, pedestrianId)
    }

    fun deleteOrganizationArchiveAndDrafts(
        archiveKey: String,
        organizationId: String,
    ) {
        repository.deleteOrganizationArchiveAndDrafts(archiveKey, organizationId)
    }
    fun insertItemToDb(phPlusDB: PhPlusDB) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertItem(phPlusDB)
        }
    }

    fun insertItemToDbSync(phPlusDB: PhPlusDB) {
        runBlocking(Dispatchers.IO) {
            repository.insertItem(phPlusDB)
        }
    }

    suspend fun insertBasicInfo(basicInfoList: List<PhPlusDB>) {
        this.repository.insertList(basicInfoList)
    }
}