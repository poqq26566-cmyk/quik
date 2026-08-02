package dev.octoshrimpy.quik.feature.settings.autodelete

data class AutoDeleteCategoriesState(
    val notificationsEnabled: Boolean = false,
    val verificationCodesEnabled: Boolean = false
)
