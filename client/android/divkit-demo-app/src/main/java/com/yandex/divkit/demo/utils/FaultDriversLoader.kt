package com.yandex.divkit.demo.utils

import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONObject

data class FaultDriverItem(
    val driverName: String,
    val cause: String,
    val vehicleModel: String,
    val plate: String,
)

object FaultDriversLoader {
    private const val TAG = "FaultDriversLoader"

    fun load(viewModel: MehdiViewModel?, accidentId: String? = null): List<FaultDriverItem> {
        val resolvedAccidentId = VehicleEntryCountHelper.resolveAccidentId(viewModel, accidentId)
        val username = dbValue(viewModel, "userName")
        if (resolvedAccidentId.isBlank() || username.isBlank()) {
            android.util.Log.w(TAG, "load: missing accidentId=$resolvedAccidentId username=$username")
            return emptyList()
        }

        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/vehicles/$resolvedAccidentId%").orEmpty()
        }.getOrDefault(emptyList())

        android.util.Log.d(TAG, "load: accidentId=$resolvedAccidentId vehicles=${rows.size}")

        return rows.mapNotNull { row ->
            val archive = parseArchive(row.value)
            val vehicleId = archive["vehicle_id"].orEmpty()
            if (vehicleId.isBlank()) return@mapNotNull null

            val page1 = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page1"))
            val page2 = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page2"))
            if (!isAtFault(page1, archive)) return@mapNotNull null

            val driverName = firstNonBlank(
                archive["driver_dbshow"],
                listOf(
                    page2["variable_compromising_driver_name"],
                    page2["variable_compromising_driver_family"],
                ).filter { !it.isNullOrBlank() }.joinToString(" "),
            ).ifBlank { "راننده مقصر" }

            val cause = firstNonBlank(
                page2["variable_compromising_driver_violation_type"],
                page2["variable_compromising_driver_absolute_cause"],
                page2["variable_compromising_driver_judicial_cause"],
                page2["variable_compromising_driver_fault_reason"],
                page2["variable_compromising_driver_total_cause"],
            )

            val model = firstNonBlank(
                archive["model_dbshow"],
                page1["variable_compromising_vehicle_factory"],
                page1["uncompromising_vehicle_system"],
                page1["uncompromising_vehicle_tip"],
                page1["uncompromising_vehicle_factory"],
            )

            val plate = firstNonBlank(
                archive["plate_dbshow"],
                archive["title"],
                formatPlate(page1),
            )

            FaultDriverItem(
                driverName = driverName,
                cause = cause,
                vehicleModel = model,
                plate = plate,
            )
        }
    }

    fun isAtFault(page1: Map<String, String>, archive: Map<String, String> = emptyMap()): Boolean {
        // Prefer live vehicle draft (page1) over archive snapshot so fault-tab changes
        // show up on the reasoning page without requiring a full re-save to the list.
        val faultSelected = isSelectedTabColor(page1["variable_vehicle_fault_background_name"])
        val nonFaultSelected = isSelectedTabColor(page1["variable_vehicle_non_fault_background_name"])
        when {
            faultSelected && !nonFaultSelected -> return true
            nonFaultSelected && !faultSelected -> return false
        }

        page1.forEach { (key, value) ->
            val name = key.trim()
            if (
                name == "variable_validation_vehicle_select_fault_state" ||
                name == "validation_vehicle_select_fault_state" ||
                name == "validation_fault_state"
            ) {
                if (isTruthy(value)) return true
                if (isFalsy(value)) return false
            }
        }

        archive["is_at_fault"]?.let { stored ->
            if (isTruthy(stored)) return true
            if (isFalsy(stored)) return false
        }

        return faultSelected
    }

    private fun isTruthy(value: String?): Boolean {
        val v = value?.trim()?.lowercase().orEmpty()
        return v == "true" || v == "1"
    }

    private fun isFalsy(value: String?): Boolean {
        val v = value?.trim()?.lowercase().orEmpty()
        return v == "false" || v == "0"
    }

    private fun isSelectedTabColor(value: String?): Boolean {
        val normalized = value?.trim()?.removePrefix("#")?.lowercase().orEmpty()
        return normalized == "ffffff"
    }

    private fun formatPlate(vehicleDraft: Map<String, String>): String {
        val one = firstNonBlank(
            vehicleDraft["variable_compromising_vehicle_plate_one"],
            vehicleDraft["variable_plateA"],
            vehicleDraft["uncompromising_vehicle_plate_one"],
        )
        val alphabet = firstNonBlank(
            vehicleDraft["variable_plateB"],
            vehicleDraft["uncompromising_vehicle_plate_alphabet"],
        )
        val three = firstNonBlank(
            vehicleDraft["variable_compromising_vehicle_plate_three"],
            vehicleDraft["variable_plateC"],
            vehicleDraft["uncompromising_vehicle_plate_three"],
        )
        val four = firstNonBlank(
            vehicleDraft["variable_compromising_vehicle_plate_four"],
            vehicleDraft["variable_plateD"],
            vehicleDraft["uncompromising_vehicle_plate_four"],
        )
        return listOf(one, alphabet, three, four).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun dbValue(viewModel: MehdiViewModel?, key: String): String {
        return runCatching { viewModel?.getValueByKey(key)?.value }.getOrNull().orEmpty()
    }

    private fun extractDraftVariables(raw: String?): Map<String, String> = DraftVariables.extract(raw)

    private fun parseArchive(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { key -> map[key] = obj.opt(key)?.toString().orEmpty() }
            map
        }.getOrDefault(emptyMap())
    }

    private fun firstNonBlank(vararg values: String?): String {
        return values.firstOrNull { !it.isNullOrBlank() && it != "null" && it != "empty" }.orEmpty()
    }
}
