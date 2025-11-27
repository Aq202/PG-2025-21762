package com.example.public_transport_app.utils
const val apiUrl = "https://www.subeteya.online/"
//const val apiUrl = "http://192.168.1.92:3000/"
object roles {
    const val defaultUser = 0
    const val transportCompanyAdmin =  1
    const val driver = 2
    const val admin = 3
}
const val trackingTimeInterval:Long = 5000L
const val accuracyThresholdMeters = 30f
const val emitLowAccuracyAfterMs = 15_000L
const val maxUnitImages = 10
const val logServiceUrl = "https://s1475152.eu-nbg-2.betterstackdata.com/"
const val logServiceToken = "gW6zHWnFSz9en2AGwSpzoxNo"

const val deviceUpdateLocationInterval: Long = 1000L
const val updateNearbyActiveRunsInterval: Long = 5000L