package com.durian.annotationdemo

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.SparseArray
import com.durian.annotation.Creator
import com.durian.annotation.Extra
import com.durian.annotationdemo.bean.User
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @TargetApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jump.setOnClickListener {
            val child = User("小明", 15, null)
            val parent = User("大明", 25, child)
            startNextActivity("小红", parent)
//            NextActivityCreator.create("小红", parent).start(this)
        }
    }

}
