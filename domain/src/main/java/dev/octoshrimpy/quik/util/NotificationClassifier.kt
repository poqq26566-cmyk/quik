/*
 * This file is part of QUIK.
 *
 * QUIK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.octoshrimpy.quik.util

/**
 * Classifies an incoming SMS as belonging to the "Messages" tab (real, high priority,
 * person-to-person or time-critical verification codes) or the "Notifications" tab
 * (bulk/business/marketing traffic sent through domestic short-code / batch channels).
 *
 * Rules (in priority order):
 *
 *  1. If the message body contains a verification/OTP keyword -> Messages.
 *     These need to be seen immediately, no matter who sent them.
 *
 *  2. If the sender address is purely numeric:
 *       - looks like a normal personal mobile number (domestic 11-digit
 *         number starting with 1[3-9], or an international number with a
 *         leading '+') -> Messages (person-to-person).
 *       - otherwise (106xxxx batch/service channels, 5-8 digit short codes,
 *         400/800 service numbers, etc.) -> Notifications.
 *
 *  3. If the sender address is alphanumeric (an "alpha sender ID", the way
 *     international OTP gateways like Google / ChatGPT / WhatsApp / Discord
 *     identify themselves) -> Messages. These are treated as important,
 *     time-critical services.
 *
 *  4. Anything else defaults to Messages (never hide a conversation the
 *     classifier isn't confident about).
 */
object NotificationClassifier {

    // Common English + Chinese verification/OTP keywords.
    val VERIFICATION_KEYWORDS = listOf(
        "验证码", "校验码", "驗證碼", "动态密码", "動態密碼",
        "verification code", "verification-code", "security code",
        "one-time code", "one time code", "otp", "passcode", "auth code",
        "authentication code", "confirmation code", "login code"
    )

    /**
     * Returns true if the message body contains a verification/OTP keyword.
     * Shared with the "auto-delete verification codes" preference so both
     * features stay in sync on what counts as a verification code message.
     */
    fun isVerificationCode(body: String): Boolean {
        val normalizedBody = body.lowercase()
        return VERIFICATION_KEYWORDS.any { normalizedBody.contains(it.lowercase()) }
    }

    // Domestic batch/service channel prefixes. 106 covers the vast majority
    // of Chinese carrier/bank/enterprise bulk SMS gateways.
    private val SERVICE_PREFIXES = listOf("106", "1069", "1065", "1068")

    private val DIGITS_ONLY_REGEX = Regex("^[0-9+\\-\\s()]+$")
    private val DOMESTIC_MOBILE_REGEX = Regex("^1[3-9]\\d{9}$")

    /**
     * Returns true if this message should be filed under "Notifications"
     * rather than "Messages".
     */
    fun isNotification(address: String, body: String): Boolean {
        if (isVerificationCode(body)) {
            return false
        }

        val trimmedAddress = address.trim()
        val isDigitsOnly = trimmedAddress.isNotEmpty() && DIGITS_ONLY_REGEX.matches(trimmedAddress)

        if (!isDigitsOnly) {
            // Alphanumeric sender ID (e.g. "Google", "ChatGPT", "WhatsApp") ->
            // treated as an important international service -> Messages.
            return false
        }

        val digitsStripped = trimmedAddress.filter { it.isDigit() }
        val isInternational = trimmedAddress.startsWith("+") && digitsStripped.length >= 8
        val isDomesticMobile = DOMESTIC_MOBILE_REGEX.matches(digitsStripped)

        if (isInternational || isDomesticMobile) {
            // Regular personal phone number -> Messages.
            return false
        }

        if (SERVICE_PREFIXES.any { digitsStripped.startsWith(it) }) {
            return true
        }

        // Any other short/service-style numeric sender that isn't a normal
        // personal mobile number (e.g. 5-8 digit codes like 10086, 95588,
        // 400/800 numbers) is treated as a service/notification sender too.
        return digitsStripped.length in 3..9
    }
}
