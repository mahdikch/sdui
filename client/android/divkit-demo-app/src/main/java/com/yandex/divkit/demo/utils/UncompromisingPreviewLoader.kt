package com.yandex.divkit.demo.utils

import android.content.Context
import android.util.Base64
import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object UncompromisingPreviewLoader {
    private const val EMPTY = "—"

    private const val TAG = "UncompromisingPreview"

    fun load(context: Context, viewModel: MehdiViewModel?): Map<String, String> {
        val accidentId = dbValue(viewModel, "current_accident_id").orEmpty()
        val username = dbValue(viewModel, "userName").orEmpty()
        android.util.Log.d(TAG, "load: accidentId=$accidentId, username=$username, viewModel=${viewModel != null}")
        val out = linkedMapOf<String, String>()

        val page1Raw = dbValue(viewModel, "draft/$accidentId/page1")
        android.util.Log.d(TAG, "page1 raw length=${page1Raw?.length ?: -1}, isBlank=${page1Raw.isNullOrBlank()}")
        val page1 = extractDraftVariables(page1Raw)
        val page3 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page3"))
        val page6 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page6"))
        val page7 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page7"))
        val page8 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page8"))
        val page9 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page9"))
        android.util.Log.d(TAG, "page sizes: p1=${page1.size} p3=${page3.size} p6=${page6.size} p7=${page7.size} p8=${page8.size} p9=${page9.size}")

        out["preview_road"] = display(page1, "variable_road")
        out["preview_scene_status"] = display(page1, "ACCIDENT_SCENE_STATUS_title")
        out["preview_accident_date"] = display(page1, "variable_accident_date")
        out["preview_accident_time"] = display(page1, "variable_accident_time")
        out["preview_news_number"] = display(page1, "variable_news_number")
        out["preview_vehicle_count"] = display(page1, "variable_vehicle_count")
        out["preview_distance_from_origin"] = display(page1, "variable_distance_from_origin")
        out["preview_police_date"] = display(page1, "variable_police_date")
        out["preview_police_time"] = display(page1, "variable_police_time")
        out["preview_register_date"] = display(page1, "variable_register_accident_date")
        out["preview_register_time"] = display(page1, "variable_register_accident_time")
        out["preview_accident_type"] = display(page1, "variable_type_accident")
        out["preview_lat"] = display(page1, "variable_geographic_coordinates_lat")
        out["preview_long"] = display(page1, "variable_geographic_coordinates_long")

        out["preview_collision_place"] = display(page6, "compromising_final_registration_collision_place")
        out["preview_vehicle_factor"] = display(page6, "compromising_final_registration_vehicle_factor")
        out["preview_human_factor"] = display(page6, "compromising_final_registration_human_factor")
        out["preview_road_effective"] = display(page6, "compromising_final_registration_road_effective")
        out["preview_traffic_allowed"] = display(page6, "compromising_final_registration_traffic_allowed")
        out["preview_lighting_status"] = display(page6, "compromising_final_registration_lighting_status")
        out["preview_road_surface"] = display(page6, "compromising_final_registration_road_surface")
        out["preview_sight_obstructions"] = display(page6, "compromising_final_registration_sight_obstructions")
        out["preview_geometrical_of_road"] = display(page6, "compromising_final_registration_geometrical_of_road")
        out["preview_road_position"] = display(page6, "compromising_final_registration_road_position")
        out["preview_weather_status"] = display(page6, "compromising_final_registration_weather_status")
        out["preview_region_usage"] = display(page6, "compromising_final_registration_region_usage")
        out["preview_road_type"] = display(page6, "compromising_final_registration_road_type")
        out["preview_main_road_width"] = display(page6, "compromising_final_registration_main_road_width")
        out["preview_sub_road_width"] = display(page6, "compromising_final_registration_sub_road_width")
        out["preview_shoulder_type"] = display(page6, "compromising_final_registration_shoulder_type")
        out["preview_shoulder_width"] = display(page6, "compromising_final_registration_shoulder_width")
        out["preview_line_count"] = display(page6, "compromising_final_registration_line_count")
        out["preview_location_speed"] = display(page6, "compromising_final_registration_location_speed")

        out["preview_expert_code"] = display(page7, "uncompromising_additional_expert_code")
        out["preview_news_code"] = display(page7, "uncompromising_additional_news_code")
        out["preview_police_informed_date"] = display(page7, "uncompromising_additional_police_informed_date")
        out["preview_police_informed_time"] = display(page7, "uncompromising_additional_police_informed_time")
        out["preview_emergency_date"] = display(page7, "uncompromising_additional_emergency_informed_date")
        out["preview_emergency_time"] = display(page7, "uncompromising_additional_emergency_informed_time")
        out["preview_auxiliary_date"] = display(page7, "uncompromising_additional_auxiliary_informed_date")
        out["preview_auxiliary_time"] = display(page7, "uncompromising_additional_auxiliary_informed_time")
        out["preview_how_police_informed"] = display(page7, "uncompromising_additional_how_police_informed_time")
        out["preview_beginning_road_name"] = display(page7, "uncompromising_additional_beginning_road_name")
        out["preview_operant_status"] = display(page7, "uncompromising_additional_operant_status")
        out["preview_primitive_reasons"] = display(page7, "uncompromising_additional_primitive_reasons")
        out["preview_interface_reasons"] = display(page7, "uncompromising_additional_interface_reasons")
        out["preview_direct_reasons"] = display(page7, "uncompromising_additional_direct_reasons")
        out["preview_road_cover"] = display(page7, "uncompromising_additional_road_cover")
        out["preview_region_type"] = display(page7, "uncompromising_additional_region_type")
        out["preview_intersection_control"] = display(page7, "uncompromising_additional_intersection_control")
        out["preview_geometry_location"] = display(page7, "uncompromising_additional_geometry_location")
        out["preview_line"] = display(page7, "uncompromising_additional_line")
        out["preview_road_repair"] = display(page7, "uncompromising_additional_road_repair")
        out["preview_equipment_injuries"] = display(page7, "uncompromising_additional_equipment_injuries")
        out["preview_police_station"] = display(page7, "uncompromising_additional_police_station")

        out["preview_expert_explanation"] = display(page8, "variable_explanation")

        out["preview_images_damaged_points"] = countImages(page3["uncompromising_images_damaged_points"])
        out["preview_images_parties_final_status"] = countImages(page3["uncompromising_images_parties_final_status"])
        out["preview_images_scene_traces"] = countImages(page3["uncompromising_images_scene_traces"])
        out["preview_images_signs"] = countImages(page3["uncompromising_images_signs"])
        out["preview_images_markings_lights"] = countImages(page3["uncompromising_images_markings_lights"])
        out["preview_images_other_positions"] = countImages(page3["uncompromising_images_other_positions"])
        out["preview_images_panorama"] = countImages(page3["uncompromising_images_panorama"])
        out["preview_images_road_geometry"] = countImages(page3["uncompromising_images_road_geometry"])

        val sketchJson = page9["uncompromising_accident_sketch_json"].orEmpty()
        val sketchImage = page9["uncompromising_accident_sketch_image"].orEmpty()
        val sketchSound = page9["uncompromising_accident_sketch_sound"].orEmpty()
        out["uncompromising_accident_sketch_json"] = sketchJson
        out["uncompromising_accident_sketch_image"] = sketchImage
        out["uncompromising_accident_sketch_sound"] = sketchSound
        out["preview_sketch_status"] = if (sketchJson.isNotBlank() || sketchImage.isNotBlank()) {
            "ثبت شده"
        } else {
            "ثبت نشده"
        }
        out["preview_sketch_image_url"] = writeSketchPreviewFile(context, accidentId, sketchImage)

        out["preview_vehicles"] = buildVehiclesText(viewModel, username, accidentId)
        out["preview_pedestrians"] = buildArchiveListText(
            viewModel,
            "%$username/pedestrians/$accidentId%",
            emptyTitle = "عابری ثبت نشده است.",
        )
        out["preview_organizations"] = buildArchiveListText(
            viewModel,
            "%$username/organizations/$accidentId%",
            emptyTitle = "شخص حقوقی ثبت نشده است.",
        )
        out["current_accident_id"] = accidentId
        android.util.Log.d(TAG, "output keys=${out.keys}")
        out.forEach { (k, v) ->
            android.util.Log.d(TAG, "  $k = ${v.take(80)}")
        }
        return out
    }

    private fun buildVehiclesText(
        viewModel: MehdiViewModel?,
        username: String,
        accidentId: String,
    ): String {
        if (accidentId.isBlank()) return "وسیله‌ای ثبت نشده است."
        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/vehicles/$accidentId%").orEmpty()
        }.getOrDefault(emptyList())
        if (rows.isEmpty()) return "وسیله‌ای ثبت نشده است."

        return rows.mapIndexed { index, row ->
            val archive = parseArchive(row.value)
            val vehicleId = archive["vehicle_id"].orEmpty()
            val vehicleDraft = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page1"))
            val driverDraft = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page2"))
            val insuranceDraft = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page4"))
            val passengers = runCatching {
                viewModel?.getOfflineReportsListSync("%$username/passengers/$vehicleId%").orEmpty()
            }.getOrDefault(emptyList())
                .mapNotNull { parseArchive(it.value)["title"]?.takeIf { title -> title.isNotBlank() } }

            val plate = firstNonBlank(
                archive["title"],
                archive["plate_dbshow"],
                formatPlate(vehicleDraft),
            )
            val model = firstNonBlank(
                archive["model_dbshow"],
                vehicleDraft["uncompromising_vehicle_system"],
                vehicleDraft["uncompromising_vehicle_tip"],
            )
            val usage = firstNonBlank(
                archive["usage_dbshow"],
                vehicleDraft["uncompromising_vehicle_usage"],
            )
            val driver = firstNonBlank(
                archive["driver_dbshow"],
                listOf(
                    driverDraft["variable_compromising_driver_name"],
                    driverDraft["variable_compromising_driver_family"],
                ).filter { !it.isNullOrBlank() }.joinToString(" "),
            )
            val insurance = firstNonBlank(
                archive["insurance_dbshow"],
                insuranceDraft["uncompromising_insurance_company_name"],
            )
            val passengerText = if (passengers.isEmpty()) "ندارد" else passengers.joinToString("، ")

            buildString {
                append("وسیله ${index + 1}")
                append("\nپلاک: ").append(displayValue(plate))
                append("\nمدل: ").append(displayValue(model))
                append("\nکاربری: ").append(displayValue(usage))
                append("\nراننده: ").append(displayValue(driver))
                val nationalCode = driverDraft["variable_compromising_driver_national_code"].orEmpty()
                if (nationalCode.isNotBlank()) {
                    append("\nکد ملی راننده: ").append(nationalCode)
                }
                append("\nبیمه: ").append(displayValue(insurance))
                append("\nسرنشینان: ").append(passengerText)
            }
        }.joinToString("\n\n")
    }

    private fun buildArchiveListText(
        viewModel: MehdiViewModel?,
        pattern: String,
        emptyTitle: String,
    ): String {
        val rows = runCatching {
            viewModel?.getOfflineReportsListSync(pattern).orEmpty()
        }.getOrDefault(emptyList())
        if (rows.isEmpty()) return emptyTitle
        return rows.mapIndexed { index, row ->
            val archive = parseArchive(row.value)
            val title = archive["title"]?.takeIf { it.isNotBlank() } ?: "مورد ${index + 1}"
            val details = archive.filterKeys { it.endsWith("_dbshow") }
                .mapNotNull { (key, value) ->
                    if (value.isBlank()) return@mapNotNull null
                    val label = archive[key.replace("_dbshow", "_persian_lan")] ?: key.removeSuffix("_dbshow")
                    "$label: $value"
                }
            buildString {
                append(title)
                if (details.isNotEmpty()) {
                    append("\n")
                    append(details.joinToString("\n"))
                }
            }
        }.joinToString("\n\n")
    }

    private fun formatPlate(vehicleDraft: Map<String, String>): String {
        val one = vehicleDraft["uncompromising_vehicle_plate_one"].orEmpty()
        val alphabet = vehicleDraft["uncompromising_vehicle_plate_alphabet"].orEmpty()
        val three = vehicleDraft["uncompromising_vehicle_plate_three"].orEmpty()
        val four = vehicleDraft["uncompromising_vehicle_plate_four"].orEmpty()
        return listOf(one, alphabet, three, four).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun dbValue(viewModel: MehdiViewModel?, key: String): String? {
        return runCatching { viewModel?.getValueByKey(key)?.value }.getOrNull()
    }

    private fun extractDraftVariables(raw: String?): Map<String, String> = DraftVariables.extract(raw)

    private fun parseArchive(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(raw)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { key ->
                map[key] = obj.opt(key)?.toString().orEmpty()
            }
            map
        }.getOrDefault(emptyMap())
    }

    private fun countImages(raw: String?): String {
        if (raw.isNullOrBlank()) return "۰ تصویر"
        return runCatching {
            val count = when {
                raw.trim().startsWith("[") -> JSONArray(raw).length()
                raw.contains("base64") || raw.length > 40 -> 1
                else -> 0
            }
            "$count تصویر"
        }.getOrDefault("۰ تصویر")
    }

    private fun writeSketchPreviewFile(context: Context, accidentId: String, rawImage: String): String {
        if (rawImage.isBlank()) return "empty://"
        val base64 = extractBase64(rawImage) ?: return "empty://"
        return runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            if (bytes.isEmpty()) return@runCatching "empty://"
            val file = File(context.cacheDir, "uncompromising_preview_sketch_${accidentId.ifBlank { "tmp" }}.png")
            file.writeBytes(bytes)
            "file://${file.absolutePath}"
        }.getOrDefault("empty://")
    }

    private fun extractBase64(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("{")) {
            return runCatching {
                JSONObject(trimmed).optString("image").takeIf { it.isNotBlank() }
                    ?: JSONObject(trimmed).optString("base64").takeIf { it.isNotBlank() }
            }.getOrNull() ?: trimmed
        }
        val comma = trimmed.indexOf(",")
        if (trimmed.startsWith("data:image") && comma >= 0) {
            return trimmed.substring(comma + 1)
        }
        return trimmed
    }

    private fun display(source: Map<String, String>, key: String): String {
        return displayValue(source[key])
    }

    private fun displayValue(value: String?): String {
        val trimmed = value?.trim().orEmpty()
        return if (trimmed.isEmpty() || trimmed == "null" || trimmed == "empty") EMPTY else trimmed
    }

    private fun firstNonBlank(vararg values: String?): String {
        return values.firstOrNull { !it.isNullOrBlank() && it != "null" }.orEmpty()
    }
}
