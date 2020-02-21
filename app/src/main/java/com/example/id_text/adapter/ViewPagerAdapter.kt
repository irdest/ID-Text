package com.example.id_text.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class ViewPagerAdapter(private val num: Int, private val enableDestroyItem: Boolean, private val title: List<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, item: Any): Boolean {
        return view == item
    }

    override fun getCount(): Int {
        return num
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return container.getChildAt(position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        if (enableDestroyItem) {
            container.removeView(item as View)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return title[position]
    }

}