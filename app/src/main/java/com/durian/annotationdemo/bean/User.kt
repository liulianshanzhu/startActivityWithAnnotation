package com.durian.annotationdemo.bean

import java.io.Serializable

/**
 * 说明：
 * @author 黄志敏
 * @since 2019/8/31 8:57
 */
class User: Serializable {
    constructor(name: String, age: Int, child: User?) {
        this.name = name
        this.age = age
        this.child = child
    }

    var name: String = ""

    var age: Int = 0

    var child: User? = null
}