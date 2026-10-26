package com.yandex.divkit.demo.utils

import com.yandex.div.core.view2.Div2View
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONObject

object VehicleEntryCountHelper {

    private const val TAG = "VehicleEntryCountHelper"
    const val DRIVER_SIGNATURE_VARIABLE = "variable_compromising_driver_signature"
    const val DRIVER_SIGNATURE_ARCHIVE_FIELD = "driver_signature"

    fun sync(viewModel: MehdiViewModel?, div2View: Div2View?, accidentId: String? = null) {
        if (div2View == null) return

        val resolvedAccidentId = resolveAccidentId(viewModel, accidentId)
        if (resolvedAccidentId.isBlank()) {
            android.util.Log.w(TAG, "sync: could not resolve accident id")
            return
        }

        div2View.setVariable("current_accident_id", resolvedAccidentId)

        val requiredCount = resolveRequiredVehicleCount(viewModel, resolvedAccidentId)
        val addedCount = countArchivedVehicles(viewModel, resolvedAccidentId)
        val isCountComplete = requiredCount > 0 && addedCount == requiredCount
        val allHaveSignatures = isCountComplete &&
            allArchivedVehiclesHaveDriverSignature(viewModel, resolvedAccidentId)
        val canAddMore = requiredCount > 0 && addedCount < requiredCount

        android.util.Log.d(
            TAG,
            "sync: accidentId=$resolvedAccidentId required=$requiredCount added=$addedCount " +
                "countOk=$isCountComplete signaturesOk=$allHaveSignatures",
        )

        // Never overwrite with 0: on first-page load (before draft save) requiredCount is 0 and
        // would wipe the value the user is typing.
        if (requiredCount > 0) {
            div2View.setVariable("variable_vehicle_count", requiredCount.toString())
        }
        div2View.setVariable("vehicles_added_count", addedCount.toString())
        div2View.setVariable("val_validation_vehicles_count", if (isCountComplete) "true" else "false")
        div2View.setVariable(
            "val_validation_vehicles_signatures",
            if (allHaveSignatures) "true" else "false",
        )
        div2View.setVariable(
            "val_validation_vehicles_ready",
            if (isCountComplete && allHaveSignatures) "true" else "false",
        )
        div2View.setVariable("can_add_vehicle", if (canAddMore) "true" else "false")
    }

    fun resolveAccidentId(viewModel: MehdiViewModel?, preferred: String? = null): String {
        preferred?.takeIf { it.isNotBlank() }?.let { return it }

        dbValue(viewModel, "current_accident_id").takeIf { it.isNotBlank() }?.let { return it }

        return runCatching {
            viewModel?.getOfflineReportsListSync("draft/%/page1").orEmpty()
                .mapNotNull { row ->
                    val key = row.key ?: return@mapNotNull null
                    Regex("^draft/([^/]+)/page1$").find(key)?.groupValues?.getOrNull(1)
                }
                .maxByOrNull { accidentId ->
                    accidentId.substringBefore("_").toLongOrNull() ?: 0L
                }
        }.getOrNull().orEmpty()
    }

    fun resolveRequiredVehicleCount(viewModel: MehdiViewModel?, accidentId: String): Int {
        if (accidentId.isBlank()) return 0

        readDraftVariable(viewModel, "draft/$accidentId/page1", "variable_vehicle_count")
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { return it }

        dbValue(viewModel, "variable_vehicle_count/$accidentId")
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { return it }

        dbValue(viewModel, "variable_vehicle_count")
            .toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { return it }

        return 0
    }

    fun countArchivedVehicles(viewModel: MehdiViewModel?, accidentId: String): Int {
        if (accidentId.isBlank()) return 0
        val username = dbValue(viewModel, "userName")
        if (username.isBlank()) return 0
        val viewModelNonNull = viewModel ?: return 0
        return runCatching {
            viewModelNonNull.getOfflineReportsListSync("%$username/vehicles/$accidentId%").size
        }.getOrDefault(0)
    }

    fun allArchivedVehiclesHaveDriverSignature(
        viewModel: MehdiViewModel?,
        accidentId: String,
    ): Boolean {
        if (accidentId.isBlank()) return false
        val username = dbValue(viewModel, "userName")
        if (username.isBlank()) return false
        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/vehicles/$accidentId%").orEmpty()
        }.getOrDefault(emptyList())
        if (rows.isEmpty()) return false

        return rows.all { row ->
            val fromArchive = runCatching {
                JSONObject(row.value.orEmpty()).optString(DRIVER_SIGNATURE_ARCHIVE_FIELD)
            }.getOrDefault("")
            if (fromArchive.isNotBlank()) return@all true

            val vehicleId = extractVehicleId(row.value, row.key)
            if (vehicleId.isBlank()) return@all false
            val fromDraft = readDraftVariable(
                viewModel,
                "draft/$vehicleId/page2",
                DRIVER_SIGNATURE_VARIABLE,
            )
            fromDraft.isNotBlank()
        }
    }

    private fun extractVehicleId(archiveJson: String?, archiveKey: String?): String {
        if (!archiveJson.isNullOrBlank()) {
            runCatching {
                JSONObject(archiveJson).optString("vehicle_id")
            }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
        }
        // key format: {username}/vehicles/{accidentId}/{vehicleId}
        val key = archiveKey.orEmpty()
        val parts = key.split('/')
        return parts.lastOrNull().orEmpty()
    }

    fun canAddAnotherVehicle(
        viewModel: MehdiViewModel?,
        accidentId: String,
        vehicleId: String,
        archiveKey: String,
    ): Boolean {
        val isEdit = dbValue(viewModel, archiveKey).isNotBlank()
        if (isEdit) return true

        val resolvedAccidentId = resolveAccidentId(viewModel, accidentId)
        val requiredCount = resolveRequiredVehicleCount(viewModel, resolvedAccidentId)
        if (requiredCount <= 0) return true

        val addedCount = countArchivedVehicles(viewModel, resolvedAccidentId)
        return addedCount < requiredCount
    }

    private fun dbValue(viewModel: MehdiViewModel?, key: String): String {
        return runCatching {
            viewModel?.getValueByKey(key)?.value.orEmpty()
        }.getOrDefault("")
    }

    private fun readDraftVariable(
        viewModel: MehdiViewModel?,
        draftKey: String,
        variableName: String,
    ): String {
        val draftJson = dbValue(viewModel, draftKey)
        if (draftJson.isBlank()) return ""
        return runCatching {
            val root = JSONObject(draftJson)
            val card = root.optJSONObject("card") ?: root
            val variables = card.optJSONArray("variables")
                ?: root.optJSONArray("variables")
                ?: return ""
            for (i in 0 until variables.length()) {
                val variable = variables.getJSONObject(i)
                if (variable.optString("name") == variableName) {
                    return variable.opt("value")?.toString().orEmpty()
                }
            }
            ""
        }.getOrDefault("")
    }
}
