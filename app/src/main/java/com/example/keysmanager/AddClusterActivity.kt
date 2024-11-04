package com.example.keysmanager

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddClusterActivity : AppCompatActivity(){
    var mainPwd = ""
    var cluster_id = 0
    //数据库
    private lateinit var kmdb:KMDatabase
    lateinit var clusterDao:ClusterDao
    lateinit var clusterEntity: ClusterEntity
    lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?){
        //视图层
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addcluster)
        setTitle("请输入钥匙串名")
        getWindow().setGravity(Gravity.CENTER)  //getWindow().setLayout(900, 500); //加上了会超过手机屏幕大小
        btnConfirm = this.findViewById(R.id.confirmBtn)
        btnConfirm.setOnClickListener(View.OnClickListener { clickConfirm() })

        //获取数据库连接实例
        kmdb = KMDatabase.create(this)
        clusterDao = kmdb.clusterDao()
        val intent=getIntent()
        //获取主密码和父结点id
        mainPwd = intent.getStringExtra("mainPwd")
        cluster_id = intent.getIntExtra("cluster_id",1)
    }

    //确认按钮,新建钥匙串
    fun clickConfirm(){
        val editText = findViewById<EditText>(R.id.cluster_name)
        //构造钥匙串实体,钥匙串名需要加密
        clusterEntity = ClusterEntity(
            father_id = cluster_id ,
            cluster_name = AESCrypt.encrypt(editText.text.toString(), mainPwd),
            cluster_path = "" //暂时没有使用
        )
        clusterDao.insert(clusterEntity) //插入数据库
        finish()
    }
}