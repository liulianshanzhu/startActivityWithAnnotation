package com.durian.annotationdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.durian.annotation.Creator
import com.durian.annotation.Extra
import com.durian.annotationdemo.bean.User
import com.durian.annotationdemo.utils.InjectUtil
import kotlinx.android.synthetic.main.activity_next.*

@Creator
class NextActivity : AppCompatActivity() {

    @Extra(false)
    var name: String = ""
    @Extra(true)
    var age: Int = 1
    @Extra(false)
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InjectUtil.bind(this)
        setContentView(R.layout.activity_next)

        Log.e("HZMDurian", "${intent.extras?.getString("name")}  $name")
        printUser(user)

    }

    private fun printUser(user: User?) {
        user?.let {
            log.text = "${log.text.toString()}${user.name},${user.age}\n"
            Log.d("HZMDurian", "user.name = ${it.name}  user.age = ${it.age}")
            printUser(user?.child)
        }?:Log.e("HZMDurian", "no user")

    }
}
