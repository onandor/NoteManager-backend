package com.onandor.models

import java.util.*

data class PasswordPair(
    val deviceId: UUID,
    val oldPassword: String,
    val newPassword: String
)