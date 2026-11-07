package com.yandex.divkit.demo.utils

import com.yandex.divkit.demo.ui.activity.MehdiViewModel
import org.json.JSONArray
import org.json.JSONObject

/**
 * Builds the requestJson for primaryRegisterAccidents service
 * from draft pages and archived vehicle/passenger lists stored in PhPlusDB.
 */
object CompromisingAccidentJsonBuilder {

    private const val TAG = "CompromisingJsonBuilder"

    fun build(
        viewModel: MehdiViewModel?,
        liveVars: Map<String, String> = emptyMap(),
        context: android.content.Context? = null,
    ): String {
        val accidentId = liveVars["current_accident_id"]?.takeIf { it.isNotBlank() }
            ?: dbValue(viewModel, "current_accident_id").orEmpty()
        val username = dbValue(viewModel, "userName").orEmpty()

        android.util.Log.d(TAG, "build: accidentId=$accidentId username=$username liveVars=${liveVars.size}")

        val page1 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page1"))
        val page4Draft = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page4"))
        // Uncompromising final registration is saved as page11
        val page11Draft = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page11"))
        val page4 = page4Draft + page11Draft + liveVars
        val page3 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page3"))
        val page8 = extractDraftVariables(dbValue(viewModel, "draft/$accidentId/page8"))

        val accident = buildAccidentObject(
            page1,
            page3,
            page4,
            page8,
            accidentId,
            viewModel,
            username,
            context,
        )
        val vehicles = buildVehiclesArray(viewModel, username, accidentId)
        val pedestrians = buildPedestriansArray(viewModel, username, accidentId)
        val faultOrgan = buildFaultOrganObject(page4, viewModel, username, accidentId)

        val root = JSONObject()
        root.put("accident", accident)
        root.put("vehicles", vehicles)
        root.put("pedestrains", pedestrians)
        root.put("faultOrgan", faultOrgan)

        stringifyLeaves(root)
        val json = root.toString()
        android.util.Log.d(TAG, "built requestJson length=${json.length}")
        return json
    }

    private fun buildAccidentObject(
        page1: Map<String, String>,
        page3: Map<String, String>,
        page4: Map<String, String>,
        page8: Map<String, String>,
        accidentId: String,
        viewModel: MehdiViewModel?,
        username: String,
        context: android.content.Context? = null,
    ): JSONObject {
        val obj = JSONObject()

        obj.put("typeID", intOrNull(page1["variable_type_accident_code"]))
        val roadCode = page1["variable_road_code"]
            ?: dbValue(viewModel, "variable_road_code/$accidentId")
        val policeCenterId = resolvePoliceCenterId(page1, accidentId, viewModel, roadCode, context)
        if (policeCenterId == null) {
            android.util.Log.w(
                TAG,
                "policeCenterID missing for accidentId=$accidentId roadCode=$roadCode"
            )
        } else {
            android.util.Log.d(TAG, "policeCenterID=$policeCenterId for roadCode=$roadCode")
        }
        // Server requires this key — never omit it when we have a value.
        if (policeCenterId != null) {
            obj.put("policeCenterID", policeCenterId)
        }
        obj.put("roadID", intOrNull(roadCode))
        obj.put("professionCode", intOrNull(dbValue(viewModel, "policeCode")))
        obj.put("reportTypeID", -1)
        obj.putOpt("collectionTypeId", intOrNull(page1["variable_collection_type_code"]))
        obj.put("areaUsageIDs", page4["compromising_final_registration_region_usage_code"].orEmpty())
        obj.put("distanceFrom", intOrNull(page1["variable_distance_from_origin"]))

        val accDate = page1["variable_accident_date"].orEmpty()
        val accTime = normalizeTime(page1["variable_accident_time"].orEmpty())
        if (accDate.isNotBlank() && accTime.isNotBlank()) {
            obj.put("accidentDate", "$accDate $accTime")
        } else {
            obj.put("accidentDate", accDate)
        }

        val regDate = page1["variable_register_accident_date"].orEmpty()
        val regTime = normalizeTime(page1["variable_register_accident_time"].orEmpty())
        if (regDate.isNotBlank() && regTime.isNotBlank()) {
            obj.put("completionDate", "$regDate $regTime")
        } else {
            obj.put("completionDate", regDate)
        }

        val policeDate = page1["variable_police_date"].orEmpty()
            .ifBlank { dbValue(viewModel, "variable_police_date/$accidentId").orEmpty() }
        val policeTime = normalizeTime(
            page1["variable_police_time"].orEmpty()
                .ifBlank { dbValue(viewModel, "variable_police_time/$accidentId").orEmpty() },
        )
        obj.put(
            "policeArriveTime",
            when {
                policeDate.isNotBlank() && policeTime.isNotBlank() -> "$policeDate $policeTime"
                policeTime.isNotBlank() -> policeTime
                else -> policeDate
            },
        )

        obj.put(
            "notificationNo",
            page1["variable_news_number"].orEmpty()
                .ifBlank { dbValue(viewModel, "variable_news_number/$accidentId").orEmpty() },
        )

        obj.put(
            "expertOpinion",
            page3["compromising_reasoning_officer_theory"].orEmpty()
                .ifBlank {
                    dbValue(viewModel, "compromising_reasoning_officer_theory/$accidentId").orEmpty()
                },
        )

        obj.put(
            "analysis",
            page3["compromising_reasoning_officer_description"].orEmpty()
                .ifBlank {
                    dbValue(viewModel, "compromising_reasoning_officer_description/$accidentId")
                        .orEmpty()
                }
                .ifBlank { page8["variable_explanation"].orEmpty() },
        )

        obj.put("sceneStatusID", intOrNull(page1["ACCIDENT_SCENE_STATUS_code"]))
        obj.put("lightStatusID", intOrNull(page4["compromising_final_registration_lighting_status_code"]))

        parseCoordinates(page1["variable_geographic_coordinates_lat"].orEmpty(), obj, "x")
        parseCoordinates(page1["variable_geographic_coordinates_long"].orEmpty(), obj, "y")

        // نوع برخورد (page1) → collisionTypeID
        obj.put(
            "collisionTypeID",
            intOrNull(
                page1["variable_collection_type_code"]
                    .orEmpty()
                    .ifBlank { dbValue(viewModel, "variable_collection_type_code/$accidentId").orEmpty() },
            ),
        )
        // محل برخورد (final registration) → collisionStyleID
        obj.putOpt(
            "collisionStyleID",
            intOrNull(
                draftVal(
                    page4,
                    "compromising_final_registration_collision_place_code",
                    "uncompromising_final_registration_collision_place_code",
                ),
            ),
        )
        obj.put("vehicleReasonIDs", page4["compromising_final_registration_vehicle_factor_code"].orEmpty())
        obj.put("humanReasonIDs", page4["compromising_final_registration_human_factor_code"].orEmpty())
        obj.put("shoulderStatusID", intOrNull(page4["compromising_final_registration_shoulder_type_code"]))
        obj.put("shoulderWidth", intOrNull(page4["compromising_final_registration_shoulder_width"]))
        obj.put("roadDirectionID", intOrNull(page4["compromising_final_registration_road_effective_code"]))
        obj.put("positionID", intOrNull(page4["compromising_final_registration_road_position_code"]))
        obj.put("airStatusIDs", page4["compromising_final_registration_weather_status_code"].orEmpty())
        obj.put("roadSurfaceConditionIDs", page4["compromising_final_registration_road_surface_code"].orEmpty())
        obj.put("obstaclesViewIDs", page4["compromising_final_registration_sight_obstructions_code"].orEmpty())
        obj.put("roadDefectIDs", page4["compromising_final_registration_geometrical_of_road_code"].orEmpty())
        obj.put("roadMaxSpeed", intOrNull(page4["compromising_final_registration_location_speed"]))
        obj.put("roadSituationID", intOrNull(page4["compromising_final_registration_traffic_allowed_code"]))
        obj.put("roadTypeID", intOrNull(page4["compromising_final_registration_road_type_code"]))
        obj.put("roadLineNum", intOrNull(page4["compromising_final_registration_line_count"]))
        obj.put("roadWidth", intOrNull(page4["compromising_final_registration_main_road_width"]))
        obj.put("deceasedNumber", intOrNull(page1["variable_deceased_count"]) ?: 0)
        obj.put("injuredNumber", intOrNull(page1["variable_injured_count"]) ?: 0)
        obj.put("vehicleUse", intOrNull(page1["variable_vehicle_count"]) ?: 0)
        val livestockFatality = buildLivestockFatalityArray(viewModel, username, accidentId)
        obj.put("marfokMain", livestockFatality.length() > 0)
        obj.put("livestockFatality", livestockFatality)
        obj.put("seri", resolveSeri(viewModel, username, accidentId))

        return obj
    }

    /**
     * سازشی → "100" ، غیرسازشی → "200"
     */
    private fun resolveSeri(
        viewModel: MehdiViewModel?,
        username: String,
        accidentId: String,
    ): String {
        val processType = dbValue(viewModel, "process_type").orEmpty().trim()
        when {
            processType.equals("ghersazeshi", ignoreCase = true) ||
                processType.equals("nonsazeshi", ignoreCase = true) ||
                processType.contains("غیر") -> return "200"
            processType.equals("sazeshi", ignoreCase = true) -> return "100"
        }

        val archiveTitle = runCatching {
            val archiveJson = dbValue(viewModel, "$username/Accidents/$accidentId").orEmpty()
            JSONObject(archiveJson).optString("title")
        }.getOrDefault("")
        return if (archiveTitle.contains("غیر")) "200" else "100"
    }

    private fun buildLivestockFatalityArray(
        viewModel: MehdiViewModel?,
        username: String,
        accidentId: String,
    ): JSONArray {
        val arr = JSONArray()
        if (accidentId.isBlank()) return arr
        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/livestock/$accidentId%").orEmpty()
        }.getOrDefault(emptyList())
        for (row in rows) {
            val archive = parseArchive(row.value)
            val typeId = archive["type_code"].orEmpty()
            val count = archive["count"].orEmpty()
            if (typeId.isBlank() || count.isBlank()) continue
            arr.put(
                JSONObject().apply {
                    put("typeID", typeId)
                    put("fatalityCount", count)
                },
            )
        }
        return arr
    }

    private fun buildVehiclesArray(
        viewModel: MehdiViewModel?,
        username: String,
        accidentId: String,
    ): JSONArray {
        val arr = JSONArray()
        if (accidentId.isBlank()) return arr

        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/vehicles/$accidentId%").orEmpty()
        }.getOrDefault(emptyList())

        android.util.Log.d(TAG, "vehicles count=${rows.size}")

        for (row in rows) {
            val archive = parseArchive(row.value)
            val vehicleId = archive["vehicle_id"].orEmpty()
            if (vehicleId.isBlank()) continue

            val vPage1 = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page1"))
            val vPage2 = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page2"))
            val vPage3 = extractDraftVariables(dbValue(viewModel, "draft/$vehicleId/page3"))

            val vehicle = JSONObject()
            vehicle.put(
                "year",
                draftVal(
                    vPage1,
                    "variable_compromising_vehicle_year_manufacture",
                    "uncompromising_vehicle_assembling_signboard",
                ),
            )
            vehicle.put(
                "typeID",
                intOrNull(
                    draftVal(
                        vPage1,
                        "variable_compromising_vehicle_usage_code",
                        "uncompromising_vehicle_usage_type_code",
                    ),
                ),
            )
            putPlateParts(vehicle, vPage1)
            vehicle.put("vin", vPage1["uncompromising_vehicle_vin"].orEmpty())
            vehicle.put(
                "chassisNo",
                draftVal(vPage1, "variable_chassis_number", "uncompromising_vehicle_chassis_number"),
            )
            vehicle.put(
                "motorNo",
                draftVal(
                    vPage1,
                    "variable_compromising_engine_number",
                    "variable_compromising_vehicle_engin_no",
                    "uncompromising_engine_number",
                    "uncompromising_vehicle_motor_number",
                ),
            )
            vehicle.put(
                "faultStatusID",
                if (FaultDriversLoader.isAtFault(vPage1, archive)) 2219 else 2282,
            )
            vehicle.put(
                "systemID",
                intOrNull(
                    draftVal(
                        vPage1,
                        "variable_system_code",
                        "uncompromising_vehicle_system_code",
                    ),
                ),
            )
            vehicle.put("efficiencyID", intOrNull(vPage1["uncompromising_vehicle_performance_code"]))
            vehicle.putOpt("numberingCorrespondID", intOrNull(vPage1["uncompromising_vehicle_numbering_correspond_code"]))
            vehicle.putOpt("resolveUp60Date", vPage1["uncompromising_vehicle_resolve_up60_date"])
            vehicle.put(
                "plaqueTypeID",
                intOrNull(
                    draftVal(
                        vPage1,
                        "variable_compromising_vehicle_plate_type_code",
                        "uncompromising_vehicle_plate_type_code",
                    ),
                ),
            )
            vehicle.put("companyID", intOrNull(vPage1["uncompromising_vehicle_manufacturer_code"]))
            vehicle.put(
                "systemTypeID",
                intOrNull(
                    draftVal(
                        vPage1,
                        "variable_type_code",
                        "uncompromising_vehicle_tip_code",
                    ),
                ),
            )
            vehicle.put(
                "colorID",
                intOrNull(
                    draftVal(
                        vPage1,
                        "variable_vehicle_body_color_code",
                        "uncompromising_vehicle_color_code",
                    ),
                ),
            )
            vehicle.put("statusIDs", vPage1["uncompromising_vehicle_status_code"].orEmpty())
            vehicle.put(
                "safetyEquipmentIDs",
                draftVal(
                    vPage1,
                    "variable_compromising_vehicle_safety_equipment_code",
                    "uncompromising_vehicle_safety_equipment_code",
                ),
            )
            vehicle.putOpt("organID", intOrNull(vPage1["uncompromising_vehicle_organ_code"]))
            vehicle.put("damageCost", intOrNull(vPage3["compromising_insurance_approximate_damage"]) ?: 0)
            vehicle.put("cargoTypeID", intOrNull(vPage1["uncompromising_vehicle_cargo_nature_code"]))
            vehicle.put("cargoWeight", intOrNull(vPage1["uncompromising_vehicle_cargo_weight"]) ?: 0)
            vehicle.put(
                "maxDamageSectionIDs",
                draftVal(
                    vPage1,
                    "variable_compromising_vehicle_damage_parts_code",
                    "uncompromising_vehicle_damage_parts_code",
                ),
            )
            vehicle.put("insuranceNo", vPage3["compromising_insurance_number"].orEmpty())
            vehicle.put("insuranceCoID", intOrNull(vPage3["compromising_insurance_company_code"]))
            vehicle.put("insuranceDate", vPage3["compromising_insurance_expire_date"].orEmpty())
            vehicle.put("bodyInsuranceNo", vPage3["compromising_insurance_body_number"].orEmpty())
            vehicle.put("bodyInsuranceCoID", intOrNull(vPage3["compromising_insurance_body_company_code"]))
            vehicle.put("bodyInsuranceDate", vPage3["compromising_insurance_body_expire_date"].orEmpty())
            vehicle.putOpt("technicalInspectionID", intOrNull(vPage1["uncompromising_vehicle_technical_inspection_code"]))
            vehicle.putOpt("productTypeID", intOrNull(vPage1["uncompromising_vehicle_product_type_code"]))
            vehicle.put("insuranceWarrantyLimit", optString(vPage3["compromising_insurance_financial_commitment"]))
            vehicle.putOpt("roadSignIDs", vPage1["uncompromising_vehicle_road_sign_code"])
            vehicle.putOpt("motionDirectionID", intOrNull(vPage1["uncompromising_vehicle_direct_path_code"]))
            vehicle.putOpt("manoeuvreID", intOrNull(vPage1["uncompromising_vehicle_maneuver_code"]))
            vehicle.putOpt("remainEffectIDs", vPage1["uncompromising_vehicle_remaining_effectiv_code"])
            vehicle.put("damageSectionOther", vPage1["uncompromising_vehicle_damage_section_other"].orEmpty())
            putVehicleExtraFields(vehicle, vPage1)

            val driver = buildDriverObject(vPage2, vPage1, viewModel, accidentId)
            vehicle.put("driver", driver)

            val passengers = buildPassengersForVehicle(viewModel, username, vehicleId)
            vehicle.put("passengers", passengers)

            arr.put(vehicle)
        }
        return arr
    }

    private fun buildDriverObject(
        driverDraft: Map<String, String>,
        vehicleDraft: Map<String, String> = emptyMap(),
        viewModel: MehdiViewModel? = null,
        accidentId: String = "",
    ): JSONObject {
        val d = JSONObject()
        d.put("address", draftVal(driverDraft, "variable_compromising_driver_address", "compromising_driver_address"))
        d.put("firstName", draftVal(driverDraft, "variable_compromising_driver_name", "compromising_driver_name"))
        d.put("lastName", draftVal(driverDraft, "variable_compromising_driver_family", "compromising_driver_family"))
        d.put("telephone", draftVal(driverDraft, "variable_compromising_driver_phone", "compromising_driver_phone"))
        d.put(
            "nationalCode",
            draftVal(driverDraft, "variable_compromising_driver_national_code", "compromising_driver_national_code"),
        )
        d.put(
            "fatherName",
            draftVal(
                driverDraft,
                "variable_father_name",
                "variable_compromising_driver_father_name",
                "compromising_driver_father_name",
            ),
        )
        d.put("age", intOrNull(draftVal(driverDraft, "variable_compromising_driver_age", "compromising_driver_age")) ?: 0)
        d.put(
            "sexID",
            intOrNull(draftVal(driverDraft, "variable_compromising_driver_sex_code", "compromising_driver_sex_code")),
        )
        d.put(
            "educationID",
            intOrNull(
                draftVal(driverDraft, "variable_compromising_driver_education_code", "compromising_driver_education_code"),
            ),
        )
        d.putOpt(
            "jobTypeID",
            intOrNull(draftVal(driverDraft, "variable_compromising_driver_job_code", "compromising_driver_job_code")),
        )
        val dependencyRaw = draftVal(
            driverDraft,
            "variable_compromising_vehicle_affiliation_code",
            "compromising_vehicle_affiliation_code",
            "variable_compromising_driver_affiliation_code",
        ).ifBlank {
            draftVal(
                vehicleDraft,
                "variable_compromising_vehicle_affiliation_code",
                "compromising_vehicle_affiliation_code",
            )
        }.ifBlank {
            if (accidentId.isBlank()) ""
            else dbValue(viewModel, "variable_compromising_vehicle_affiliation_code/$accidentId")
        }
        d.put("dependencyPersonID", intOrNull(dependencyRaw) ?: JSONObject.NULL)
        d.put(
            "licenceDate",
            draftVal(
                driverDraft,
                "variable_compromising_driver_certificate_date",
                "compromising_driver_certificate_date",
            ),
        )
        d.put(
            "firstLicenceNO",
            draftVal(
                driverDraft,
                "variable_compromising_driver_first_certificate_number",
                "compromising_driver_first_certificate_number",
            ),
        )
        d.put(
            "firstLicenceDate",
            draftVal(
                driverDraft,
                "variable_compromising_driver_first_certificate_date",
                "compromising_driver_first_certificate_date",
            ),
        )
        d.put(
            "licenceNO",
            draftVal(
                driverDraft,
                "variable_compromising_driver_certification_no",
                "compromising_driver_certification_no",
            ),
        )
        d.putOpt(
            "injuryTypeID",
            intOrNull(draftVal(driverDraft, "variable_compromising_driver_status_code", "compromising_driver_status_code")),
        )
        d.put(
            "totalReasonID",
            intOrNull(
                draftVal(
                    driverDraft,
                    "variable_compromising_driver_total_cause_code",
                    "compromising_driver_total_cause_code",
                ),
            ),
        )
        d.put("statusIDs", JSONObject.NULL)
        d.put(
            "juridicalReasonID",
            intOrNull(
                draftVal(
                    driverDraft,
                    "variable_compromising_driver_judicial_cause_code",
                    "compromising_driver_judicial_cause_code",
                ),
            ),
        )
        d.putOpt(
            "transferTypeID",
            intOrNull(
                draftVal(
                    driverDraft,
                    "variable_compromising_driver_injured_transfer_code",
                    "compromising_driver_injured_transfer_code",
                ),
            ),
        )
        d.put(
            "ambulanceCode",
            draftVal(driverDraft, "variable_compromising_driver_ambulance_code", "compromising_driver_ambulance_code"),
        )
        d.put(
            "hospitalName",
            draftVal(driverDraft, "variable_compromising_driver_hospital_name", "compromising_driver_hospital_name"),
        )
        d.putOpt(
            "licenceTypeID",
            intOrNull(
                draftVal(
                    driverDraft,
                    "variable_compromising_driver_certificate_type_code",
                    "compromising_driver_certificate_type_code",
                ),
            ),
        )
        d.put(
            "statusInLicenceSysIDs",
            draftVal(
                driverDraft,
                "variable_compromising_driver_certificate_status_code",
                "compromising_driver_certificate_status_code",
            ).ifBlank { null },
        )
        d.putOpt(
            "licencePlaceID",
            intOrNull(
                draftVal(
                    driverDraft,
                    "variable_compromising_driver_information_compliance_code",
                    "compromising_driver_information_compliance_code",
                ),
            ),
        )
        d.put(
            "safetyBeltCapStatusID",
            intOrNull(draftVal(driverDraft, "variable_compromising_driver_belt_code", "compromising_driver_belt_code")),
        )
        d.putOpt(
            "reactionTypeID",
            intOrNull(
                draftVal(driverDraft, "variable_compromising_driver_reaction_code", "compromising_driver_reaction_code"),
            ),
        )
        d.putOpt(
            "licenceCorrespondID",
            intOrNull(
                draftVal(
                    driverDraft,
                    "variable_compromising_driver_information_compliance_code",
                    "compromising_driver_information_compliance_code",
                ),
            ),
        )
        d.put(
            "violationTypeIDs",
            draftVal(
                driverDraft,
                "variable_compromising_driver_violation_type_code",
                "compromising_driver_violation_type_code",
            ),
        )

        putVehicleExtraFields(d, vehicleDraft)
        return d
    }

    /**
     * Extra vehicle-page fields shared by both `vehicle` and `driver` server objects.
     * Several exist only on the uncompromising flow.
     */
    private fun putVehicleExtraFields(target: JSONObject, vehicleDraft: Map<String, String>) {
        target.putOpt(
            "illegalCargoTypeID",
            intOrNull(draftVal(vehicleDraft, "uncompromising_vehicle_unlawful_cargo_type_code")),
        )
        target.putOpt(
            "cargoStatusID",
            intOrNull(draftVal(vehicleDraft, "uncompromising_vehicle_cargo_nature_code")),
        )
        target.putOpt(
            "fireExtinguisherId",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_fire_extinguisher_capsule_code",
                    "uncompromising_vehicle_fire_extinguisher_capsule_code",
                ),
            ),
        )
        target.putOpt(
            "gettingOnFireId",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_getting_on_fire_code",
                    "uncompromising_vehicle_getting_on_fire_code",
                ),
            ),
        )
        target.putOpt(
            "approximateSpeedId",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_collision_speed_code",
                    "uncompromising_vehicle_collision_speed_code",
                ),
            ),
        )
        target.putOpt(
            "collisionAngleId",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_collision_angle_code",
                    "uncompromising_vehicle_collision_angle_code",
                ),
            ),
        )
        target.putOpt(
            "airbagId",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_air_bag_activity_code",
                    "uncompromising_vehicle_air_bag_activity_code",
                ),
            ),
        )
        target.putOpt(
            "airbagCount",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_air_bag_count",
                    "uncompromising_vehicle_air_bag_count",
                ),
            ),
        )
        target.putOpt(
            "airbagActId",
            intOrNull(
                draftVal(
                    vehicleDraft,
                    "variable_compromising_vehicle_air_bag_performance_code",
                    "uncompromising_vehicle_air_bag_performance_code",
                ),
            ),
        )
        target.put(
            "airbagPlaceIds",
            draftVal(
                vehicleDraft,
                "variable_compromising_vehicle_air_bag_place_code",
                "uncompromising_vehicle_air_bag_place_code",
            ),
        )
    }

    private fun buildPassengersForVehicle(
        viewModel: MehdiViewModel?,
        username: String,
        vehicleId: String,
    ): JSONArray {
        val arr = JSONArray()
        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/passengers/$vehicleId%").orEmpty()
        }.getOrDefault(emptyList())

        for (row in rows) {
            val archive = parseArchive(row.value)
            val passengerId = archive["passenger_id"].orEmpty()
            if (passengerId.isBlank()) continue

            val pDraft = extractDraftVariables(dbValue(viewModel, "draft/$passengerId/page1"))
            val p = JSONObject()
            p.put("address", pDraft["variable_address"].orEmpty())
            p.put("firstName", pDraft["variable_name"].orEmpty())
            p.put("lastName", pDraft["variable_family_name"].orEmpty())
            p.put("telephone", pDraft["variable_phone"].orEmpty())
            p.put("nationalCode", pDraft["variable_national_code"].orEmpty())
            p.put("fatherName", pDraft["variable_father_name"].orEmpty())
            p.put("age", intOrNull(pDraft["variable_age"]) ?: 0)
            p.put("sexID", intOrNull(pDraft["variable_sex_code"]))
            p.put("educationID", intOrNull(pDraft["variable_education_code"]))
            p.putOpt("jobTypeID", intOrNull(pDraft["variable_job_type_code"]))
            p.put("dependencyPersonID", JSONObject.NULL)
            p.put("statusID", JSONObject.NULL)
            p.put("injuryTypeID", intOrNull(pDraft["variable_injury_type_code"]))
            p.putOpt("totalReasonID", intOrNull(pDraft["variable_total_reason_code"]))
            p.put("faultStatusID", intOrNull(pDraft["variable_fault_status_code"]))
            p.put("injuredSectionIDs", pDraft["variable_injured_section_code"].orEmpty())
            p.putOpt("transferTypeID", intOrNull(pDraft["variable_transfer_type_code"]))
            p.put("ambulanceCode", pDraft["variable_ambulance_code"].orEmpty())
            p.put("hospitalName", pDraft["variable_hospital_name"].orEmpty())
            p.putOpt("saftyBeltCapID", intOrNull(pDraft["variable_safety_belt_code"]))
            p.put(
                "chkUnknown",
                boolFromDraft(
                    pDraft,
                    "variable_check_box_unknown",
                    "uncompromising_passenger_check_box_unknown",
                    "check_box_unknown",
                    "chkUnknown",
                ),
            )
            arr.put(p)
        }
        return arr
    }

    private fun buildPedestriansArray(
        viewModel: MehdiViewModel?,
        username: String,
        accidentId: String,
    ): JSONArray {
        val arr = JSONArray()
        if (accidentId.isBlank()) return arr

        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/pedestrians/$accidentId%").orEmpty()
        }.getOrDefault(emptyList())

        for (row in rows) {
            val archive = parseArchive(row.value)
            val pedestrianId = archive["pedestrian_id"].orEmpty()
            if (pedestrianId.isBlank()) continue

            val pDraft = extractDraftVariables(dbValue(viewModel, "draft/$pedestrianId/page1"))
            val p = JSONObject()
            p.put("address", pDraft["uncompromising_pedestrian_address"].orEmpty())
            p.put("firstName", pDraft["uncompromising_pedestrian_name"].orEmpty())
            p.put("lastName", pDraft["uncompromising_pedestrian_family_name"].orEmpty())
            p.put("telephone", pDraft["uncompromising_pedestrian_phone"].orEmpty())
            p.put("nationalCode", pDraft["uncompromising_pedestrian_national_code"].orEmpty())
            p.put("fatherName", pDraft["uncompromising_pedestrian_father_name"].orEmpty())
            p.put("age", intOrNull(pDraft["uncompromising_pedestrian_age"]) ?: 0)
            p.put("sexID", intOrNull(pDraft["uncompromising_pedestrian_sex_code"]))
            p.put("educationID", intOrNull(pDraft["uncompromising_pedestrian_education_code"]))
            p.putOpt("jobTypeID", intOrNull(pDraft["uncompromising_pedestrian_job_type_code"]))
            p.put("dependencyPersonID", JSONObject.NULL)
            p.put("injuryTypeID", intOrNull(pDraft["uncompromising_pedestrian_injury_type_code"]))
            p.putOpt("totalReasonID", intOrNull(pDraft["uncompromising_pedestrian_total_reason_code"]))
            p.put("faultStatusID", intOrNull(pDraft["uncompromising_pedestrian_fault_status_code"]))
            p.put("injuredSectionIDs", intOrNull(pDraft["uncompromising_pedestrian_injured_section_code"]))
            p.putOpt("clotheColorID", intOrNull(pDraft["uncompromising_pedestrian_clothe_color_code"]))
            p.putOpt("pedestrainStatusID", intOrNull(pDraft["uncompromising_pedestrian_status_code"]))
            p.putOpt("passageUtilIDs", intOrNull(pDraft["uncompromising_pedestrian_passage_util_code"]))
            p.putOpt("pathMovementID", intOrNull(pDraft["uncompromising_pedestrian_path_movement_code"]))
            p.putOpt("encounterDistance", intOrNull(pDraft["uncompromising_pedestrian_encounter_distance"]))
            p.putOpt("encounterTime", doubleOrNull(pDraft["uncompromising_pedestrian_encounter_time"]))
            p.putOpt("averageSpeed", intOrNull(pDraft["uncompromising_pedestrian_average_speed"]))
            p.putOpt("thrownDistance", intOrNull(pDraft["uncompromising_pedestrian_thrown_distance"]))
            p.putOpt("passagePlaceID", intOrNull(pDraft["uncompromising_pedestrian_passage_place_code"]))
            p.putOpt("juridicalReasonID", intOrNull(pDraft["uncompromising_pedestrian_juridical_reason_code"]))
            p.putOpt("transferTypeID", intOrNull(pDraft["uncompromising_pedestrian_transfer_type_code"]))
            p.put("ambulanceCode", pDraft["uncompromising_pedestrian_ambulance_code"].orEmpty())
            p.put("hospitalName", pDraft["uncompromising_pedestrian_hospital_name"].orEmpty())
            p.put(
                "chkUnknown",
                boolFromDraft(
                    pDraft,
                    "uncompromising_pedestrian_check_box_unknown",
                    "check_box_unknown",
                    "chkUnknown",
                ),
            )
            arr.put(p)
        }
        return arr
    }

    private fun buildFaultOrganObject(
        page4: Map<String, String>,
        viewModel: MehdiViewModel?,
        username: String,
        accidentId: String,
    ): JSONObject {
        val obj = JSONObject()
        val rows = runCatching {
            viewModel?.getOfflineReportsListSync("%$username/organizations/$accidentId%").orEmpty()
        }.getOrDefault(emptyList())

        if (rows.isNotEmpty()) {
            val archive = parseArchive(rows.first().value)
            val orgId = archive["organization_id"].orEmpty()
            val orgDraft = if (orgId.isNotBlank()) {
                extractDraftVariables(dbValue(viewModel, "draft/$orgId/page1"))
            } else emptyMap()

            obj.putOpt("totalReasonID", intOrNull(orgDraft["uncompromising_organization_total_reason_code"]))
            obj.putOpt("juridicalReasonID", intOrNull(orgDraft["uncompromising_organization_juridical_reason_code"]))
            obj.putOpt("faultOrganId", intOrNull(orgDraft["uncompromising_organization_fault_organ_code"]))
        }
        return obj
    }

    private fun putPlateParts(vehicle: JSONObject, vehicleDraft: Map<String, String>) {
        vehicle.put(
            "plateA",
            draftVal(
                vehicleDraft,
                "variable_compromising_vehicle_plate_one",
                "variable_plateA",
                "uncompromising_vehicle_plate_one",
            ),
        )
        vehicle.put(
            "plateB",
            draftVal(
                vehicleDraft,
                "variable_plateB",
                "uncompromising_vehicle_plate_alphabet",
            ),
        )
        vehicle.put(
            "plateC",
            draftVal(
                vehicleDraft,
                "variable_compromising_vehicle_plate_three",
                "variable_plateC",
                "uncompromising_vehicle_plate_three",
            ),
        )
        vehicle.put(
            "plateD",
            draftVal(
                vehicleDraft,
                "variable_compromising_vehicle_plate_four",
                "variable_plateD",
                "uncompromising_vehicle_plate_four",
            ),
        )
        vehicle.put(
            "plateType",
            draftVal(
                vehicleDraft,
                "plateType",
                "variable_compromising_vehicle_plate_type_code",
                "uncompromising_vehicle_plate_type_code",
            ),
        )
    }

    /** e.g. 9:02 → 09:02 */
    private fun normalizeTime(time: String): String {
        val trimmed = time.trim()
        if (trimmed.isBlank()) return trimmed
        val parts = trimmed.split(':')
        if (parts.size < 2) return trimmed
        return parts.joinToString(":") { part ->
            val digits = part.filter { it.isDigit() }
            val number = digits.toIntOrNull()
            if (number != null) number.toString().padStart(2, '0') else part
        }
    }

    private fun draftVal(map: Map<String, String>, vararg names: String): String =
        DraftVariables.run { map.valueOf(*names) }

    private fun boolFromDraft(map: Map<String, String>, vararg names: String): Boolean {
        return when (draftVal(map, *names).trim().lowercase()) {
            "true", "1", "yes" -> true
            else -> false
        }
    }

    private fun parseCoordinates(value: String, obj: JSONObject, prefix: String) {
        val decimal = value.toDoubleOrNull() ?: return
        val degrees = decimal.toInt()
        val minutesFull = (decimal - degrees) * 60
        val minutes = minutesFull.toInt()
        val seconds = ((minutesFull - minutes) * 60).toInt()
        obj.put("${prefix}PositionDegree", degrees)
        obj.put("${prefix}PositionMinute", minutes)
        obj.put("${prefix}PositionSecond", seconds)
    }

    private fun stringifyLeaves(value: Any?): Any {
        return when {
            value == null || value == JSONObject.NULL -> ""
            value is JSONObject -> {
                val keys = value.keys().asSequence().toList()
                for (key in keys) {
                    value.put(key, stringifyLeaves(value.opt(key)))
                }
                value
            }
            value is JSONArray -> {
                for (i in 0 until value.length()) {
                    value.put(i, stringifyLeaves(value.opt(i)))
                }
                value
            }
            value is String -> value
            else -> value.toString()
        }
    }

    /**
     * policeCenterID = Id of the parent org-tree node of the selected road.
     * Sources (in order): page1 draft, DB key, org-tree lookup by road code.
     */
    private fun resolvePoliceCenterId(
        page1: Map<String, String>,
        accidentId: String,
        viewModel: MehdiViewModel?,
        roadCode: String?,
        context: android.content.Context? = null,
    ): Int? {
        intOrNull(page1["variable_policeCenterId"])?.let { return it }
        intOrNull(dbValue(viewModel, "variable_policeCenterId/$accidentId"))?.let { return it }
        val treeJson = dbValue(viewModel, "org_tree_json")
        val fromTree = OrgTreeParentResolver.findParentIdByCode(treeJson, roadCode, context)
        return intOrNull(fromTree)
    }

    private fun intOrNull(value: String?): Int? {
        if (value.isNullOrBlank() || value == "null" || value == "empty") return null
        return value.replace(",", "").trim().toIntOrNull()
    }

    private fun doubleOrNull(value: String?): Double? {
        if (value.isNullOrBlank() || value == "null" || value == "empty") return null
        return value.replace(",", "").trim().toDoubleOrNull()
    }

    private fun optString(value: String?): Any {
        if (value.isNullOrBlank() || value == "null" || value == "empty") return JSONObject.NULL
        return value
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
            obj.keys().forEach { key -> map[key] = obj.opt(key)?.toString().orEmpty() }
            map
        }.getOrDefault(emptyMap())
    }
}
