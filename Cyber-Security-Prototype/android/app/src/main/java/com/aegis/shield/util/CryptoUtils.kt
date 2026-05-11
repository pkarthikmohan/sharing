package com.aegis.shield.util

import java.security.SecureRandom

private val secureRandom = SecureRandom()

fun generateSixDigitPin(): String = secureRandom.nextInt(1_000_000).toString().padStart(6, '0')
