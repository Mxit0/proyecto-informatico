package com.example.marketelectronico.data.remote

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("token") val token: String
)