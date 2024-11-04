package com.example.keysmanager

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object Util {
    fun getnowDatetime():String{
        val sdf:SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }
    fun getRandomString(length: Int) : String { //生成随机字符串 length控制字符串长度
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz1234567890" //备选字符
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    //按解锁（主密码正确）、未解锁排序，再按串、钥匙排序，再按字典序排序
    fun sortMainItems(itemList: ArrayList<MainItem>,mainPwd: String):ArrayList<MainItem>{
        val si = itemList.size
        for( i in itemList.indices){//冒泡排序
            var j=si-1
            while( j >i ){
                val error_name = "THIS_IS_ERROR_NAME_6131e"
                var str_pre:String //读取两作比较钥匙的名字
                try {
                    str_pre=AESCrypt.decrypt(itemList[j-1].name, mainPwd)
                }
                catch(x:Exception){
                    str_pre=error_name
                }
                var str_j: String
                try {
                    str_j=AESCrypt.decrypt(itemList[j].name, mainPwd)
                }
                catch(x:Exception){
                    str_j=error_name
                }

//                var huan= -1 //默认

                //第一层比较  解密成功与否
                if( str_pre == error_name && str_j!=error_name){
                    var tmp=itemList[j-1]
                    itemList[j-1]=itemList[j]
                    itemList[j]=tmp
                    j=j-1
                    continue
                }
                else if(str_pre!=error_name && str_j==error_name){
                    j=j-1
                    continue
                }
                //第二层比较  串、钥匙
                //串tag=1  钥匙tag=0
                if(itemList[j-1].tag==0 && itemList[j].tag==1) {
                    var tmp = itemList[j-1]
                    itemList[j-1] = itemList[j]
                    itemList[j] = tmp
                    j=j-1
                    continue
                }
                else if(itemList[j-1].tag==1 && itemList[j].tag==0){
                    j=j-1
                    continue
                }
                //第三层比较  字典序
                if(str_pre > str_j){
                    var tmp=itemList[j-1]
                    itemList[j-1]=itemList[j]
                    itemList[j]=tmp
                    j=j-1
                    continue
                }
                else {
                    j=j-1
                    continue
                }
            }
        }

        return itemList
    }

    //搜索选项
    fun Query(itemList: ArrayList<MainItem>, str:String,mainPwd: String, mainact:Context):ArrayList<MainItem>{  //先写个局部查询：查询当前目录下的钥匙、钥匙串。 可以考虑直接对全局进行查询
        Toast.makeText(mainact,"正在查询："+str, Toast.LENGTH_SHORT).show()
        var it = itemList.iterator()  //遍历keyList进行查询。如果是全局的查询，那么对getKeys进行修改就行了：比方说getKeys的参数如果是-1，那么就返回所有的钥匙（串）
        while(it.hasNext()){
            val now = it.next()
            var str_i:String //解密钥匙的名字
            try {
                str_i = AESCrypt.decrypt(now.name, mainPwd)
            } catch (x: Exception) {//解密失败直接删掉
                it.remove()
                continue
            }
            if(-1!=str_i.indexOf(str) ){
                now.st=str_i.indexOf(str)
                now.ed=str_i.indexOf(str)+str.length
                continue
            }
            else{
                it.remove()
            }
        }

        if(itemList.isEmpty()){ //如果是空的 可以给点提示
            Toast.makeText(mainact,"正在查询："+str+"\n"+"没有搜索结果", Toast.LENGTH_SHORT).show();
        }
        return itemList
    }
}

//    fun getnowTime():String{
//        val sdf:SimpleDateFormat = SimpleDateFormat("HH:mm:ss")
//        return sdf.format(Date())
//    }
//    fun getnowTimeDetail():String{
//        val sdf:SimpleDateFormat = SimpleDateFormat("HH:mm:ss.SSS")
//        return sdf.format(Date())
//      }