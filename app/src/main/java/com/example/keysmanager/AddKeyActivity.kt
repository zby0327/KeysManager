package com.example.keysmanager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_addkey.*

class AddKeyActivity : AppCompatActivity() {
    var mainPwd:String = ""
    var cluster_id:Int = 0 //所在的钥匙串的id
    //数据库
    private lateinit var kmdb:KMDatabase
    private lateinit var keyDao: KeyDao
    private lateinit var exfieldDao: ExfieldDao
    private lateinit var keyEntity: KeyEntity //将要构造的keyEntity
    //字符串数组
    var titles = arrayListOf("钥匙名","账户","密码","备用密码","链接","备注","创建时间")
    var values = ArrayList<String>(7) //现在是空数组,没有元素
    lateinit var adapter: EditableKAdapter //View中使用的Adapter
    lateinit var btnAddkey: Button
    lateinit var btnCreatePwd: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addkey)
        //绑定监听器
        btnAddkey = this.findViewById(R.id.addkeyBtn)
        btnAddkey.setOnClickListener(View.OnClickListener { saveKey() })
        btnCreatePwd = this.findViewById(R.id.createkeyBtn)
        btnCreatePwd.setOnClickListener(View.OnClickListener { createPwd() })
        //获取主密码以及钥匙串id
        val intent= getIntent()
        mainPwd = intent.getStringExtra("mainPwd")
        cluster_id = intent.getIntExtra("cluster_id",0)
        //获取数据库连接以及操作接口
        kmdb = KMDatabase.create(this)
        keyDao = kmdb.keyDao()
        exfieldDao = kmdb.exfieldDao()
        keyEntity = KeyEntity( cluster_id = cluster_id ) //构造一个钥匙对象
        //values将从视图层读取到文本
        for(i in titles.indices) { values.add("") }
        values[6] = Util.getnowDatetime() //创建时间
        showAddKey()
    }
    //绘制添加钥匙的视图层
    fun showAddKey() {  //使用adapter绘制布局
        val layoutManager = LinearLayoutManager(this)//StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        recyclerView3.layoutManager = layoutManager
        adapter = EditableKAdapter(titles, values)
        recyclerView3.adapter = adapter
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
        //处理钥匙固有字段
        var encvalues = Array<String>(7){""}
        for (i in encvalues.indices){
            encvalues[i] = AESCrypt.encrypt(values[i], mainPwd) //对每一项进行加密
        }
        keyEntity = ArrayToKey(keyEntity, encvalues) //加密后的数据填充到keyEntity里边
        keyDao.insert(keyEntity)
        //虽然插入后数据库中会生成id,但并不会更新到实体对象上,所以必须得到这个id
        //这个地方怎么获取到key的id呢? 暂时用创建时间作为条件进行查询,但这种方案不是最完美的.
        keyEntity = keyDao.getByTime(encvalues[6])
        //处理自定义字段
        var tmpentity:ExfieldEntity
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
        val tmp = findViewById<RecyclerView>(R.id.recyclerView3)
        var itemHolder = tmp.findViewHolderForAdapterPosition(2)
        var itemView = itemHolder?.itemView //钥匙编辑界面第三项输入密码
        //生成随机口令并填入
        values[2] = Util.getRandomString(10)
        if (itemView != null) {
            itemView.findViewById<EditText>(R.id.value).setText(values[2])
        }
    }
}