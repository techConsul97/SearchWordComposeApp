package com.techconsul.searchwordapp.feature_dictionary.data.remote.dto


import com.google.gson.annotations.SerializedName

data class PhoneticDto(
    val audio: String,
    val text: String
)