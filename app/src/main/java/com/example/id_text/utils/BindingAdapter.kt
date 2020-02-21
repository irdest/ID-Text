package com.example.id_text.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.id_text.R
import com.example.id_text.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout

//关联tableLayout和viewpager
@BindingAdapter("binding:initTabAndPage")
fun TabLayout.initPager(initTabAndPage: Boolean) {
    if (initTabAndPage) {
        val count = this.tabCount
        val tableTitles = mutableListOf<String>()

        for (i in 0 until count) {
            tableTitles.add(this.getTabAt(i)?.text.toString())
        }

        val viewpager = this.rootView.findViewById<ViewPager>(R.id.view_pager)
        viewpager.adapter = ViewPagerAdapter(count, false, tableTitles)
        this.setupWithViewPager(viewpager)
    }
}
