package dev.octoshrimpy.quik.feature.settings.autodelete

import android.content.Context
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import dev.octoshrimpy.quik.common.base.QkPresenter
import dev.octoshrimpy.quik.interactor.DeleteOldMessages
import dev.octoshrimpy.quik.service.AutoDeleteService
import dev.octoshrimpy.quik.util.Preferences
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class AutoDeleteCategoriesPresenter @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val deleteOldMessages: DeleteOldMessages
) : QkPresenter<AutoDeleteCategoriesView, AutoDeleteCategoriesState>(AutoDeleteCategoriesState()) {

    init {
        disposables += prefs.autoDeleteNotifications.asObservable()
            .subscribe { enabled -> newState { copy(notificationsEnabled = enabled) } }

        disposables += prefs.autoDeleteVerificationCodes.asObservable()
            .subscribe { enabled -> newState { copy(verificationCodesEnabled = enabled) } }
    }

    override fun bindIntents(view: AutoDeleteCategoriesView) {
        super.bindIntents(view)

        view.notificationsClickIntent
            .autoDisposable(view.scope())
            .subscribe {
                val enabled = !prefs.autoDeleteNotifications.get()
                prefs.autoDeleteNotifications.set(enabled)
                onCategoryToggled(enabled)
            }

        view.verificationCodesClickIntent
            .autoDisposable(view.scope())
            .subscribe {
                val enabled = !prefs.autoDeleteVerificationCodes.get()
                prefs.autoDeleteVerificationCodes.set(enabled)
                onCategoryToggled(enabled)
            }
    }

    /**
     * 任意一个分类开关打开时，都要保证后台的周期性清理任务在跑；全部关闭
     * （包括原有按天数删除的旧开关）时才取消任务。打开某个开关时立即跑一次，
     * 不用等到明天的定时任务才生效。
     */
    private fun onCategoryToggled(justEnabled: Boolean) {
        val anyEnabled = prefs.autoDelete.get() > 0 ||
            prefs.autoDeleteNotifications.get() ||
            prefs.autoDeleteVerificationCodes.get()

        when (anyEnabled) {
            true -> {
                AutoDeleteService.scheduleJob(context)
                if (justEnabled) deleteOldMessages.execute(Unit)
            }
            false -> AutoDeleteService.cancelJob(context)
        }
    }

}
