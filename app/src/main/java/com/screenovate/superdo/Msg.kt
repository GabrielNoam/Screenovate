package com.screenovate.superdo

data class Msg(val type: Msg.Type, val message: String, val status: Status? = Status.Info) {
    enum class Type { Snack, Dialog, Log }
    enum class Status { Info, Warning, Error }
}