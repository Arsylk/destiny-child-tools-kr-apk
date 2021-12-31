package com.arsylk.mammonsmite.model.pck

enum class PckEncryption(val key: Int) { KOREA(0), GLOBAL(1) }

data class PckEncryptionException(
    val key: PckEncryption,
    override val cause: Throwable?
): Throwable("Failed to process with encryption: $key", cause)