package com.example.keysmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_editkey.*

class EditKeyActivity : AppCompatActivity() {
    var mainPwd:String=""
    var key_id:Int = 0
    //数据库
    private lateinit var kmdb:KMDatabase
    private lateinit var keyDao:KeyDao
    private lateinit var exfieldDao: ExfieldDao
    private lateinit var keyEntity:KeyEntity //本活动对应的钥匙数据类
    private lateinit var exfieldArr:Array<ExfieldEntity> //钥匙对应的所有自定义字段
    //字符串数组
    var titles = arrayListOf("钥匙名","账户","密码","备用密码","链接","笔记","创建时间")
    var values = ArrayList<String>(7) //现在是空数组
    lateinit var adapter: EditableKAdapter //View中使用的Adapter
    lateinit var btnSavekey: Button
    lateinit var btnCreatePwd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editkey) //绑定视图层
        //绑定监听器
        btnSavekey = this.findViewById(R.id.addkeyBtn)
        btnSavekey.setOnClickListener(View.OnClickListener { saveKey() })
        btnCreatePwd = this.findViewById(R.id.createkeyBtn)
        btnCreatePwd.setOnClickListener(View.OnClickListener { createPwd() })

        val intent = getIntent()
        mainPwd = intent.getStringExtra("mainPwd") //获取主密码
        key_id = intent.getIntExtra("key_id",0) //获取钥匙id
        //获取数据库连接以及操作接口
        kmdb = KMDatabase.create(this)
        keyDao = kmdb.keyDao()
        exfieldDao = kmdb.exfieldDao()
        keyEntity = keyDao.getById(key_id) //获取钥匙对象
        //构造数组
        val encvalues = KeyToArray(keyEntity) //从钥匙对象构造一个字符串数组
        for (i in titles.indices){
            values.add(AESCrypt.decrypt(encvalues[i],mainPwd))
        }
        //取出自定义字段
        exfieldArr = exfieldDao.getByKey(key_id) //取出所有属于这个钥匙的自定义字段
        for (item in exfieldArr){
            titles.add(AESCrypt.decrypt(item.title, mainPwd))
            values.add(AESCrypt.decrypt(item.content, mainPwd))
        }
        showEditKey()
    }

    fun showEditKey() {  //使用adapter绘制布局
        val layoutManager = LinearLayoutManager(this)
        recyclerViewEditKey.layoutManager = layoutManager
        adapter = EditableKAdapter(titles, values)
        recyclerViewEditKey.adapter = adapter
    }

    //为界面添加菜单选项
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_editable, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem):Boolean{ //为钥匙添加字段
        return when (item.itemId) {
            R.id.add_exfield-> { //此处添加字段，需要处理视图层
                adapter.addItem(adapter.itemCount) //在最后添加一个空项目
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //保存钥匙信息到数据库,注册到按钮
    fun saveKey(){
        //保存信息时限制账户名不能为空
        if (values[0]=="") {
            Toast.makeText(this, "钥匙名不能为空", Toast.LENGTH_SHORT).show() //圆角错误提示
            return
        }
        //values中存储着recycle view 里的数据, 需要加密
        var encvalues = Array<String>(7){""}
        for (i in encvalues.indices){
            encvalues[i] = AESCrypt.encrypt(values[i], mainPwd) //对每一项进行加密
        }
        keyEntity = ArrayToKey(keyEntity, encvalues) //加密后的数据填充到keyEntity里边
        keyDao.update(keyEntity) //此处与添加钥匙不同,且无需获取keyid
        //接着处理自定义字段, 将已有的自定义字段删除，将现有的插入数据库，
        //这种处理方法不一定是最好的
        exfieldDao.deleteArr(exfieldArr)
        var tmpentity: ExfieldEntity
        for (i in encvalues.size..values.lastIndex){
            var tmpStrArr = Array<String>(2){""} //临时字符串数组,用于填充自定义字段
            tmpStrArr[0] = AESCrypt.encrypt(titles[i], mainPwd)
            tmpStrArr[1] = AESCrypt.encrypt(values[i], mainPwd)
            tmpentity = ExfieldEntity(key_id = keyEntity.id) //与本钥匙绑定
            tmpentity = ArrayToExfield(tmpentity, tmpStrArr) //将数据填充到实体对象中
            exfieldDao.insert(tmpentity)
        }
        finish()
    }

    //生成随机密码,注册到按钮
    fun createPwd(){
        var itemView = findViewById<RecyclerView>(R.id.recyclerViewEditKey)
            .findViewHolderForAdapterPosition(2)
            ?.itemView //钥匙编辑界面第三项输入密码
        values[2] = Util.getRandomString(10) //生成随机口令并填入
        if (itemView != null) {
            itemView.findViewById<EditText>(R.id.value).setText(values[2])
        }
    }
}