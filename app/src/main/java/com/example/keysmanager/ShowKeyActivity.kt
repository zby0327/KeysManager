package com.example.keysmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_showkey.*

class ShowKeyActivity : AppCompatActivity() {
    var mainPwd:String=""
    var key_id:Int = 0
    //数据库
    private lateinit var kmdb:KMDatabase
    private lateinit var keyDao:KeyDao
    private lateinit var exfieldDao: ExfieldDao
    private lateinit var keyEntity:KeyEntity //本活动对应的钥匙数据类
    private lateinit var exfieldArr:Array<ExfieldEntity> //钥匙对应的所有自定义字段
    //构造字符串数组
    val TitleStr = arrayOf("钥匙名","账户","密码","备用密码","链接","备注","创建时间")
    var titles = ArrayList<String>()
    var values = ArrayList<String>()
    lateinit var Btn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showkey)
        //本活动是从主活动启动的,获取主密码和钥匙id
        val intent=getIntent()
        mainPwd = intent.getStringExtra("mainPwd")
        key_id = intent.getIntExtra("key_id",0)
        //数据库连接与操作接口实例化
        kmdb = KMDatabase.create(this)
        keyDao = kmdb.keyDao()
        exfieldDao = kmdb.exfieldDao()
        //为按钮注册监听器
        Btn = this.findViewById<FloatingActionButton>(R.id.fab)
        Btn.setOnClickListener(View.OnClickListener { EditKey() })
    }
    override fun onResume() {
        super.onResume()
        titles.clear() //清空数组
        values.clear()
        keyEntity = keyDao.getById(key_id)//获取本活动对应的钥匙数据类对象
        val keystrarr = KeyToArray(keyEntity) //将取出的钥匙转化成一个字符串数组
        //开始构造字符串数组
        for(i in TitleStr.indices){
            var tmpstr = AESCrypt.decrypt(keystrarr[i], mainPwd) //先解密
            if(tmpstr == "") { continue }
            values.add(tmpstr) //将已解密且不为空的字符串附加到values列表的末尾
            titles.add(TitleStr[i])
        }
        exfieldArr = exfieldDao.getByKey(key_id) //取出所有属于这个钥匙的自定义字段
        for (item in exfieldArr){
            titles.add(AESCrypt.decrypt(item.title, mainPwd))
            values.add(AESCrypt.decrypt(item.content, mainPwd))
        }
        //展示
        ShowKey()
    }

    fun ShowKey(){
        val layoutManager = LinearLayoutManager(this)
        recyclerView2.layoutManager =layoutManager
        val adapter = ShowKeyAdapter(titles, values,this)
        recyclerView2.adapter = adapter
    }


    //构造菜单选项,只有一个删除
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_showkey, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean{
        return when (item.itemId) {
            R.id.delete_key-> { // 删除当前的钥匙和自定义字段
                exfieldDao.deleteArr(exfieldArr)//先处理自定义字段
                keyDao.delete(keyEntity) //其实顺序无所谓.
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //启动编辑钥匙的活动,绑定到按钮
    fun EditKey() {
        val intent = Intent(this, EditKeyActivity::class.java)
        intent.putExtra("mainPwd", mainPwd) //把主密码传过去
        intent.putExtra("key_id", key_id) //需要把钥匙的id传递过去
        startActivity(intent)
    }
}