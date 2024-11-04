package com.example.keysmanager

//用于主活动中显示钥匙(串)列表的钥匙对象，tag区分钥匙和钥匙串：0是钥匙，1是钥匙串
data class MainItem (
    val id:Int,
    val name:String,
    val tag:Int,
    var st:Int = -1,
    var ed:Int = -1
)//st \ ed就是高亮显示的字??