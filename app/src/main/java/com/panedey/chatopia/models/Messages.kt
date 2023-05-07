package com.panedey.chatopia.models

data class Messages @JvmOverloads constructor(
    val message: String? = "",
    val type: String? = "",
    val time: Long? = 0L,
    val seen:Boolean= false,
    val from:String? =""
)
