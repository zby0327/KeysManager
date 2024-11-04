package com.example.keysmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class BackupActivity : AppCompatActivity() {
    private lateinit var kmdb:KMDatabase
    private lateinit var keyDao: KeyDao
    private lateinit var exfieldDao: ExfieldDao
    private lateinit var clusterDao: ClusterDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        kmdb = KMDatabase.create(this)
        keyDao = kmdb.keyDao()
        exfieldDao = kmdb.exfieldDao()
        clusterDao = kmdb.clusterDao()
        val viewDB = findViewById<EditText>(R.id.dbText)
        viewDB.text = (InitText())
        viewDB.setEnabled(false)
        viewDB.setFocusable(false)
        viewDB.setKeyListener(null)
        val btnClip = findViewById<Button>(R.id.clipBtn)
        btnClip.setOnClickListener(View.OnClickListener {
            var str: ClipData = ClipData.newPlainText("Label", viewDB.text.toString())
            var cm: ClipboardManager = this@BackupActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(str)
            if(viewDB.text.toString() == ""){
                Toast.makeText(this@BackupActivity,"无数据", Toast.LENGTH_SHORT).show()
            } else {//简单的提示已复制的消息框
                Toast.makeText(this@BackupActivity,"已复制到粘贴板", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun InitText() : Editable{
        var text = ""
        val keyEntityArr = keyDao.all()
        text = text + GetKey(keyEntityArr)
        val clusterEntityArr = clusterDao.all()
        text = text + GetCluster(clusterEntityArr)
        val exfieldEntityArr:Array<ExfieldEntity> = exfieldDao.all()
        text = text + GetExfield(exfieldEntityArr)
        return SpannableStringBuilder(text)
    }
    fun GetKey(keyEntityArr: Array<KeyEntity> ) : String{
        var keyText = ""
        for (i in keyEntityArr){
            var itemText = "{0,"
            itemText += i.id.toString()+','
            itemText += i.cluster_id.toString()+','
            itemText += i.name+','
            itemText += i.account+','
            itemText += i.pwd1+','
            itemText += i.pwd2+','
            itemText += i.url+','
            itemText += i.note+','
            itemText += i.createtime
            itemText += "}"
            keyText += itemText
        }
        return keyText
    }
    fun GetCluster(clusterEntityArr: Array<ClusterEntity>):String{
        var clusterText = ""
        for (i in clusterEntityArr){
            var itemText = "{1,"
            itemText += i.id.toString()+','
            itemText += i.father_id.toString()+','
            itemText += i.cluster_name+','
            itemText += i.cluster_path
            itemText += '}'
            clusterText+=itemText
        }
        return clusterText
    }
    fun GetExfield(exfieldEntityArr: Array<ExfieldEntity>):String{
        var exfieldText = ""
        for (i in exfieldEntityArr){
            var itemText = "{2,"
            itemText += i.id.toString()+','
            itemText += i.key_id.toString()+','
            itemText += i.title+','
            itemText += i.content
            itemText += '}'
            exfieldText += itemText
        }
        return exfieldText
    }
}
