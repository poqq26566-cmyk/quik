package dev.octoshrimpy.quik.feature.settings.autodelete

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import dev.octoshrimpy.quik.R
import dev.octoshrimpy.quik.common.base.QkController
import dev.octoshrimpy.quik.databinding.AutoDeleteCategoriesControllerBinding
import dev.octoshrimpy.quik.injection.appComponent
import javax.inject.Inject

class AutoDeleteCategoriesController :
    QkController<AutoDeleteCategoriesControllerBinding, AutoDeleteCategoriesView, AutoDeleteCategoriesState, AutoDeleteCategoriesPresenter>(),
    AutoDeleteCategoriesView {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup): AutoDeleteCategoriesControllerBinding =
        AutoDeleteCategoriesControllerBinding.inflate(inflater, container, false)

    @Inject override lateinit var presenter: AutoDeleteCategoriesPresenter

    override val notificationsClickIntent by lazy { binding.notifications.clicks() }
    override val verificationCodesClickIntent by lazy { binding.verificationCodes.clicks() }

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        setTitle(R.string.auto_delete_categories_title)
        showBackButton(true)
        presenter.bindIntents(this)
    }

    override fun render(state: AutoDeleteCategoriesState) {
        binding.notifications.checkbox?.isChecked = state.notificationsEnabled
        binding.verificationCodes.checkbox?.isChecked = state.verificationCodesEnabled
    }

}
