package ru.skillbranch.skillarticles.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.dialog_chose_category.view.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.articles.ArticlesViewModel


private const val SELECTED_CATEGORIES = "SELECTED_CATEGORIES"

class ChoseCategoryDialog : DialogFragment() {
    private val viewModel: ArticlesViewModel by activityViewModels()
    private val selectedCategories = mutableListOf<String>()
    private val args: ChoseCategoryDialogArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        selectedCategories.clear()
        selectedCategories.addAll(
                savedInstanceState?.getStringArray(SELECTED_CATEGORIES) ?: args.selectedCategories
        )

        val categoryItems = args.categories.toList().map {
            it.toCategoryItem(selectedCategories.contains(it.categoryId))
        }

        val categoryAdapter = CategoryAdapter(
                requireContext(),
                categoryItems
        ) { view ->
            val box = view.findViewById<CheckBox>(R.id.ch_select)
            val categoryId = args.categories[view.tag as Int].categoryId
            if (box.isChecked) selectedCategories.remove(categoryId) else selectedCategories.add(categoryId)
            box.isChecked = !box.isChecked
        }

        val convertView = layoutInflater.inflate(R.layout.dialog_chose_category, null)
        val categoriesList = convertView.findViewById(R.id.categories_list) as ListView
        categoriesList.adapter = categoryAdapter
        val adb = AlertDialog.Builder(requireContext())
                .setTitle("Choose category")
                .setView(convertView)
                .setPositiveButton("Apply") { _, _ -> viewModel.applyCategories(selectedCategories) }
                .setNegativeButton("Reset") { _, _ -> viewModel.applyCategories(emptyList()) }
        return adb.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArray(SELECTED_CATEGORIES, selectedCategories.toTypedArray())
    }
}

private fun CategoryData.toCategoryItem(checked: Boolean): CategoryItem = CategoryItem(checked, icon, title, "(${articlesCount})")

data class CategoryItem(val checked: Boolean, val icon: String, val title: String, val count: String)

class CategoryAdapter(
        private val cxt: Context,
        private val items: List<CategoryItem>,
        private val listener: (view: View) -> Unit
) : ArrayAdapter<CategoryItem>(cxt, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(cxt)
                .inflate(R.layout.item_dialog_chose_category, parent, false)
        with(view) {
            setOnClickListener(listener)
            tag = position
        }

        val data = getItem(position) ?: return view

        with(view.findViewById<CheckBox>(R.id.ch_select)) {
            isClickable = false
            isChecked = data.checked
        }

        val iconView = view.findViewById<ImageView>(R.id.iv_icon)
        Glide.with(cxt)
                .load(data.icon)
                .apply(RequestOptions.circleCropTransform())
                .override(cxt.dpToIntPx(40))
                .into(iconView)

        view.findViewById<TextView>(R.id.tv_category).text = data.title
        view.findViewById<TextView>(R.id.tv_count).text = data.count

        return view
    }

    override fun getCount() = items.size
}
