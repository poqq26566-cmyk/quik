package dev.octoshrimpy.quik.feature.settings.autodelete

import dev.octoshrimpy.quik.common.base.QkViewContract
import io.reactivex.Observable

interface AutoDeleteCategoriesView : QkViewContract<AutoDeleteCategoriesState> {

    val notificationsClickIntent: Observable<*>
    val verificationCodesClickIntent: Observable<*>

}
