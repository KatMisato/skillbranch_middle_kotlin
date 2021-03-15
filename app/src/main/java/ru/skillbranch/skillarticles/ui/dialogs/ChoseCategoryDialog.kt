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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
                categoryItems
        ) { view ->
            val box = view.findViewById<CheckBox>(R.id.ch_select)
            val categoryId = args.categories[view.tag as Int].categoryId
            if (box.isChecked) selectedCategories.remove(categoryId) else selectedCategories.add(categoryId)
            box.isChecked = !box.isChecked
        }

        val convertView = layoutInflater.inflate(R.layout.dialog_chose_category, null)
        val categoriesList = convertView.findViewById(R.id.categories_list) as RecyclerView
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
        private val items: List<CategoryItem>,
        private val listener: (view: View) -> Unit
) : RecyclerView.Adapter<CategoryVH>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CategoryVH {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_dialog_chose_category, viewGroup, false)
        return CategoryVH(view)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        holder.bind(items[position], position, listener)
    }

    override fun getItemCount() = items.size
}

class CategoryVH(private val containerView: View) : RecyclerView.ViewHolder(containerView) {
    private val chSelect: CheckBox = containerView.findViewById(R.id.ch_select)
    private val tvTitle: TextView = containerView.findViewById(R.id.tv_category)
    private val tvCount: TextView = containerView.findViewById(R.id.tv_count)
    private val iconView: ImageView = containerView.findViewById(R.id.iv_icon)

    fun bind(item: CategoryItem?,
             position: Int,
             listener: (view: View) -> Unit) {

        with(containerView) {
            setOnClickListener(listener)
            tag = position
        }

        with(chSelect) {
            isClickable = false
            isChecked = item?.checked == true
        }

        Glide.with(containerView.context)
                .load(item?.icon)
                .apply(RequestOptions.circleCropTransform())
                .override(containerView.context.dpToIntPx(40))
                .into(iconView)

        tvTitle.text = item?.title
        tvCount.text = item?.count
    }
}
