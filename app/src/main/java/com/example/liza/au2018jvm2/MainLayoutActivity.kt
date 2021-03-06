package com.example.liza.au2018jvm2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.LinearLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick

class MainLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        linearLayout {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
        
            linearLayout {
                gravity = Gravity.CENTER
                orientation = LinearLayout.VERTICAL
            
                button(R.string.my_museums) {
                    id = Ids.myMuseumsBtn
                    onClick {
                        startActivity<MyMuseumsActivity>() }
                }.lparams(width = matchParent, height = wrapContent)
                button(R.string.all_museums) {
                    id = Ids.allMuseumsBtn
                    onClick { startActivity<AllMuseumsActivity>() }
                }.lparams(width = matchParent, height = wrapContent)
                button(R.string.museums_map) {
                    id = Ids.mapBtn
                    onClick { startActivity<MapActivity>() }
                }.lparams(width = matchParent, height = wrapContent)
            }.lparams(width = dip(0), height = matchParent) {
                weight = 1F
            }
        }
    }

    private object Ids {
        const val allMuseumsBtn = 1
        const val mapBtn = 2
        const val myMuseumsBtn = 3
    }
}