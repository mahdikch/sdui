package com.yandex.divkit.demo.utils

import com.yandex.divkit.demo.ui.activity.MehdiViewModel

object VehicleWizardDraftResolver {
    private data class DraftMapping(
        val page: String,
        val idKey: String,
    )

    private val SCREEN_TO_DRAFT = mapOf(
        // Accident flow (current_accident_id)
        "Accidents/compromisefirst" to DraftMapping("page1", "current_accident_id"),
        "Accidents/unCompromisefirst" to DraftMapping("page1", "current_accident_id"),
        "Accidents/vehicleInformationEntry" to DraftMapping("page2", "current_accident_id"),
        "Accidents/unConpromised/VehicleInformationEntry" to DraftMapping("page2", "current_accident_id"),
        "Accidents/exportreasoninginference" to DraftMapping("page3", "current_accident_id"),
        "Accidents/finalregistration" to DraftMapping("page4", "current_accident_id"),
        "Accidents/uncompromising/submitimage" to DraftMapping("page3", "current_accident_id"),
        "Accidents/uncompromising/pedestrianinformation" to DraftMapping("page4", "current_accident_id"),
        "Accidents/uncompromising/organizationlegalentities" to DraftMapping("page5", "current_accident_id"),
        "Accidents/uncompromising/submit" to DraftMapping("page6", "current_accident_id"),
        "Accidents/uncompromising/additionalinformation" to DraftMapping("page7", "current_accident_id"),
        "Accidents/uncompromising/expertreasoning" to DraftMapping("page8", "current_accident_id"),
        "Accidents/unCompromise/draw" to DraftMapping("page9", "current_accident_id"),

        // Vehicle wizard (current_vehicle_id / passenger / pedestrian / organization)
        "Accidents/vehicleInformation" to DraftMapping("page1", "current_vehicle_id"),
        "Accidents/driverprofile" to DraftMapping("page2", "current_vehicle_id"),
        "Accidents/insuranceinformation" to DraftMapping("page3", "current_vehicle_id"),
        "Accidents/uncompromising/vehicleInformation" to DraftMapping("page1", "current_vehicle_id"),
        "Accidents/uncompromising/driverprofile" to DraftMapping("page2", "current_vehicle_id"),
        "Accidents/uncompromising/passengerdetails" to DraftMapping("page3", "current_vehicle_id"),
        "Accidents/uncompromising/insuranceInformation" to DraftMapping("page4", "current_vehicle_id"),
        "Accidents/uncompromising/addpassenger" to DraftMapping("page1", "current_passenger_id"),
        "Accidents/uncompromising/pedestrianspecification" to DraftMapping("page1", "current_pedestrian_id"),
        "Accidents/uncompromising/organizationblame" to DraftMapping("page1", "current_organization_id"),
    )

    /**
     * Returns the DB key to load screen content from.
     * When a saved draft exists for the current step/entity, use it instead of the template.
     */
    fun resolveContentKey(screenKey: String, mehdiViewModel: MehdiViewModel?): String {
        val mapping = SCREEN_TO_DRAFT[screenKey] ?: return screenKey
        val entityId = mehdiViewModel?.getValueByKey(mapping.idKey)?.value.orEmpty()
        if (entityId.isEmpty()) return screenKey

        val draftKey = "draft/$entityId/${mapping.page}"
        val draft = mehdiViewModel?.getValueByKey(draftKey)?.value
        return if (!draft.isNullOrBlank()) draftKey else screenKey
    }
}
