package com.example.keysmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.lang.Exception

class RestoreActivity : AppCompatActivity() {
    private lateinit var kmdb:KMDatabase
    private lateinit var keyDao: KeyDao
    private lateinit var exfieldDao: ExfieldDao
    private lateinit var clusterDao: ClusterDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)

        kmdb = KMDatabase.create(this)
        keyDao = kmdb.keyDao()
        exfieldDao = kmdb.exfieldDao()
        clusterDao = kmdb.clusterDao()
        val btnRestore = findViewById<Button>(R.id.restoreBtn)
        btnRestore.setOnClickListener(View.OnClickListener { Restore() })
    }

    fun Restore(){
        val viewDB = findViewById<EditText>(R.id.dbText)
        val dbString = viewDB.text.toString()
        if(dbString == ""){
            Toast.makeText(this@RestoreActivity,"无数据", Toast.LENGTH_SHORT).show();
            return ;
        }

        val exitList = exfieldDao.all()
        exfieldDao.deleteArr(exitList)//额外字段需要清空
        
        try{
            val itemList = dbString.split(Regex("[{}]"))
            for (itemString in itemList){
                try{
                    Save(itemString)
                }
                catch(x:Exception){
                    continue
                }
            }
        }
        catch(x:Exception){
            Toast.makeText(this@RestoreActivity,"还原失败，请重试", Toast.LENGTH_SHORT).show();
            return ;
        }
        Toast.makeText(this@RestoreActivity,"还原成功", Toast.LENGTH_SHORT).show();
        finish()
        return ;
    }

    fun Save(itemString:String){
        val valueList = itemString.split(Regex("[,]"))
        when (valueList[0].toString().toInt()){
            0 -> { //key
                SaveKey(itemString.substring(2))
            }
            1 -> { //cluster
                SaveCluster(itemString.substring(2))
            }
            2 -> { //exfield
                SaveExfield(itemString.substring(2))
            }
            else -> {

            }
        }
    }
    fun SaveKey(itemString:String){
        val valueList = itemString.split(Regex(","))
        var encvalues = Array<String>(9){""}
        if(valueList.size!=9)
            throw Exception()
        for (i in valueList.indices){
            encvalues[i] = valueList[i]
        }
        var keyEntity =KeyEntity(cluster_id = -1)
        keyEntity = ArrayToKey2(keyEntity, encvalues) //加密后的数据填充到keyEntity里边

        try {
            keyDao.insert(keyEntity)//插入数据库
        }
        catch (x:Exception){

        }
        keyDao.update(keyEntity)//更新数据库
    }
    fun SaveCluster(itemString:String ){
        val valueList = itemString.split(Regex(","))
        if(valueList.size!=4)
            throw Exception()
        val clusterEntity = ClusterEntity(
            id = valueList[0].toInt(),
            father_id = valueList[1].toInt() ,
            cluster_name = valueList[2],
            cluster_path = valueList[3] //暂时没有使用
        )
        try {
            clusterDao.insert(clusterEntity) //插入数据库
        }
        catch (x:Exception){

        }
        clusterDao.update(clusterEntity) //更新数据库
    }
    fun SaveExfield(itemString:String){
        val valueList = itemString.split(Regex(","))
        if(valueList.size!=4)
            throw Exception()
        var tmpStrArr = Array<String>(4){""} //临时字符串数组,用于填充自定义字段
        for(th in valueList.indices){
            tmpStrArr[th] = valueList[th]
        }
        var tmpentity = ExfieldEntity(key_id = -1)
        tmpentity = ArrayToExfield2(tmpentity,tmpStrArr)
        try {
            exfieldDao.insert(tmpentity)
        }
        catch (x:Exception){
            try{
                exfieldDao.update(tmpentity)
            }
            catch(y:Exception){
                Toast.makeText(this@RestoreActivity,"额外字段还原出错", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
