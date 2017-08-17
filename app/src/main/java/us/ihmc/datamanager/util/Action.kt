package us.ihmc.datamanager.util

import android.content.IntentFilter

/**
 * Action.java
 */
enum class Action private constructor(private val _code: Int, private val _signature: String) {
    HANDSHAKE(-1, "us.ihmc.android.aci.dspro.HANDSHAKE"),
    GET_DATA(0, "us.ihmc.android.aci.dspro.GET_DATA"),
    DATA_ARRIVED(1, "us.ihmc.android.aci.dspro.DATA_ARRIVED"),
    REQUEST_MORE_CHUNKS(2, "us.ihmc.android.aci.dspro.REQUEST_MORE_CHUNKS"),
    REQUEST_CUSTOM_CHUNK(3, "us.ihmc.android.aci.dspro.REQUEST_CUSTOM_CHUNK"),
    SERVICE_STARTED(4, "us.ihmc.android.aci.dspro.SERVICE_STARTED"),
    SERVICE_STOPPED(5, "us.ihmc.android.aci.dspro.SERVICE_STOPPED"),
    PATH(6, "us.ihmc.android.aci.dspro.PATH"),
    TRACK(7, "us.ihmc.android.aci.dspro.TRACK"),
    IMAGE(8, "us.ihmc.android.aci.dspro.IMAGE"),
    DOC(9, "us.ihmc.android.aci.dspro.DOC"),
    PPT(10, "us.ihmc.android.aci.dspro.PPT"),
    XLS(11, "us.ihmc.android.aci.dspro.XLS"),
    SET_POSITION(12, "us.ihmc.android.aci.dspro.SET_POSITION"),
    JSON(13, "us.ihmc.android.aci.dspro.JSON"),
    ADD_MESSAGE(14, "us.ihmc.android.aci.dspro.ADD_MESSAGE"),
    DISSEMINATE(15, "us.ihmc.android.aci.dspro.DISSEMINATE"),
    SHOW_DSPRO_TOOL(16, "com.atakmap.android.dspro.SHOW_DSPRO_TOOL"),
    METADATA_ARRIVED(17, "us.ihmc.android.aci.dspro.METADATA_ARRIVED"),
    VIDEO(18, "us.ihmc.android.aci.dspro.VIDEO"),
    SET_SELECTIVITY(19, "us.ihmc.android.aci.dspro.SET_SELECTIVITY"),
    LOAD_ROUTE(20, "us.ihmc.android.aci.dspro.LOAD_ROUTE"),
    REGISTER_PATH(21, "us.ihmc.android.aci.dspro.REGISTER_PATH"),
    SEARCH(22, "us.ihmc.android.aci.dspro.SEARCH"),
    VOI(23, "us.ihmc.android.aci.dspro.VOI");

    override fun toString(): String {
        return _signature
    }

    fun code(): Int {
        return _code
    }

    companion object {

        fun fromString(signature: String?): Action? {

            if (signature == "us.ihmc.android.aci.dspro.HANDSHAKE") {
                return HANDSHAKE
            } else if (signature == "us.ihmc.android.aci.dspro.GET_DATA") {
                return GET_DATA
            } else if (signature == "us.ihmc.android.aci.dspro.DATA_ARRIVED") {
                return DATA_ARRIVED
            } else if (signature == "us.ihmc.android.aci.dspro.REQUEST_MORE_CHUNKS") {
                return REQUEST_MORE_CHUNKS
            } else if (signature == "us.ihmc.android.aci.dspro.REQUEST_CUSTOM_CHUNK") {
                return REQUEST_CUSTOM_CHUNK
            } else if (signature == "us.ihmc.android.aci.dspro.SERVICE_STARTED") {
                return SERVICE_STARTED
            } else if (signature == "us.ihmc.android.aci.dspro.SERVICE_STOPPED") {
                return SERVICE_STOPPED
            } else if (signature == "us.ihmc.android.aci.dspro.PATH") {
                return PATH
            } else if (signature == "us.ihmc.android.aci.dspro.TRACK") {
                return TRACK
            } else if (signature == "us.ihmc.android.aci.dspro.IMAGE") {
                return IMAGE
            } else if (signature == "us.ihmc.android.aci.dspro.DOC") {
                return DOC
            } else if (signature == "us.ihmc.android.aci.dspro.PPT") {
                return PPT
            } else if (signature == "us.ihmc.android.aci.dspro.XLS") {
                return XLS
            } else if (signature == "us.ihmc.android.aci.dspro.SET_POSITION") {
                return SET_POSITION
            } else if (signature == "us.ihmc.android.aci.dspro.JSON") {
                return JSON
            } else if (signature == "us.ihmc.android.aci.dspro.ADD_MESSAGE") {
                return ADD_MESSAGE
            } else if (signature == "us.ihmc.android.aci.dspro.DISSEMINATE") {
                return DISSEMINATE
            } else if (signature == "us.ihmc.android.aci.dspro.SHOW_DSPRO_TOOL") {
                return SHOW_DSPRO_TOOL
            } else if (signature == "us.ihmc.android.aci.dspro.METADATA_ARRIVED") {
                return METADATA_ARRIVED
            } else if (signature == "us.ihmc.android.aci.dspro.VIDEO") {
                return VIDEO
            } else if (signature == "us.ihmc.android.aci.dspro.SET_SELECTIVITY") {
                return SET_SELECTIVITY
            } else if (signature == "us.ihmc.android.aci.dspro.LOAD_ROUTE") {
                return LOAD_ROUTE
            } else if (signature == "us.ihmc.android.aci.dspro.REGISTER_PATH") {
                return REGISTER_PATH
            } else if (signature == "us.ihmc.android.aci.dspro.SEARCH") {
                return SEARCH
            } else if (signature == "us.ihmc.android.aci.dspro.VOI") {
                return VOI
            } else {
                return null
            }
        }

        fun fromCode(code: Int): Action? {

            if (code == -1) {
                return HANDSHAKE
            } else if (code == 0) {
                return GET_DATA
            } else if (code == 1) {
                return DATA_ARRIVED
            } else if (code == 2) {
                return REQUEST_MORE_CHUNKS
            } else if (code == 3) {
                return REQUEST_CUSTOM_CHUNK
            } else if (code == 4) {
                return SERVICE_STARTED
            } else if (code == 5) {
                return SERVICE_STOPPED
            } else if (code == 6) {
                return PATH
            } else if (code == 7) {
                return TRACK
            } else if (code == 8) {
                return IMAGE
            } else if (code == 9) {
                return DOC
            } else if (code == 10) {
                return PPT
            } else if (code == 11) {
                return XLS
            } else if (code == 12) {
                return SET_POSITION
            } else if (code == 13) {
                return JSON
            } else if (code == 14) {
                return ADD_MESSAGE
            } else if (code == 15) {
                return DISSEMINATE
            } else if (code == 16) {
                return SHOW_DSPRO_TOOL
            } else if (code == 17) {
                return METADATA_ARRIVED
            } else if (code == 18) {
                return VIDEO
            } else if (code == 19) {
                return SET_SELECTIVITY
            } else if (code == 20) {
                return LOAD_ROUTE
            } else if (code == 21) {
                return REGISTER_PATH
            } else if (code == 22) {
                return SEARCH
            } else if (code == 23) {
                return VOI
            } else {
                return null
            }
        }

        val dsProFilter: IntentFilter
            get() {

                val filter = IntentFilter()

                for (a in Action.values()) {
                    filter.addAction(a.toString())
                }

                return filter
            }
    }
}