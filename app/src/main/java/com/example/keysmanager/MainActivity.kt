package com.example.keysmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import javax.crypto.BadPaddingException

class MainActivity : AppCompatActivity(),ModifyPwdDialogFragment.ModifyMainPwdDialogListener {
    var mainPwd:String="" //主密码
    var cluster_id:Int = 1 //钥匙串id //var class_id:Int=0 //钥匙串id
    var isinit:Boolean = false //是否是最初的主活动
    var delRootConfirm = 3 //删根串的确认按钮
    private var itemList = ArrayList<MainItem>()//用于展示的item列表  //private var keyList = ArrayList<Key>()

    //数据库连接,操作接口,实体
    private lateinit var kmdb:KMDatabase
    lateinit var clusterEntity: ClusterEntity //本活动对应的钥匙串
    lateinit var clusterDao:ClusterDao
    lateinit var keyDao:KeyDao
    lateinit var exfieldDao:ExfieldDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //获取clusterEntity
        kmdb = KMDatabase.create(this.applicationContext)//实例化数据库连接
        clusterDao = kmdb.clusterDao()
        keyDao = kmdb.keyDao()
        exfieldDao = kmdb.exfieldDao()
        try{//尝试获取意图以判断是否是最初的主活动
            val intent=getIntent()
            mainPwd = intent.getStringExtra("mainPwd")
            cluster_id = intent.getIntExtra("cluster_id",1) //class_id = intent.getIntExtra("class_id",0)
        }catch(e:Exception){
            isinit = true
        }
        if(isinit){//如果是最初的主活动,则跳转到输入密码的活动
            callInputPwd()
            //根钥匙串
            val tmpclstrarray:Array<ClusterEntity> = clusterDao.all()
            if(tmpclstrarray.size == 0){//如果是0说明是新用户,创建根钥匙串并插入数据库
                clusterEntity = ClusterEntity(1,
                    "root",
                    "/",
                    1)
                clusterDao.insert(clusterEntity)
            } else{//否则取出根钥匙串
                clusterEntity = clusterDao.getById(1)
            }
        } else { //如果不是启动应用时的活动,则直接到数据库中查询
            clusterEntity = clusterDao.getById(cluster_id)
        }

        //设置钥匙串名称
        val title = findViewById<TextView>(R.id.title)
        if(isinit){ // 根钥匙串单独处理
            title.setText("当前钥匙串： 根钥匙串")
        }else{
            title.setText("当前钥匙串： "+AESCrypt.decrypt(clusterEntity.cluster_name, mainPwd))
        }
    }
    override fun onResume() {
        super.onResume()
        itemList = getMainItems() //获取本活动中需要展示的item存入itemList
        showMainItems()
    }

    //启动输入主密码的活动
    fun callInputPwd(){
        val intent= Intent(this,InputPwdActivity::class.java)//intent.putExtra("mainPwd",mainPwd)//为什么要这一步?
        startActivityForResult(intent,0)
    }
    //从inputPwd活动获取主密码,或者从其他mainactivity中获取
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            mainPwd = data.getStringExtra("mainPwd")
        }
    }

    //展示Item相关函数
    //获取本活动中需要展示的item存入itemList
    fun getMainItems():ArrayList<MainItem>{//操作数据库获取Entity,保存到用于展示的数据结构中
        var itemList = ArrayList<MainItem>()
        val clusterEntityArr = clusterDao.getByFId(cluster_id) //获取到当前钥匙串下的钥匙串
        val keyEntityArr = keyDao.getByFId(cluster_id) //获取当前钥匙串下的钥匙
        for( i in clusterEntityArr){
            itemList.add(MainItem( i.id, i.cluster_name,1))// tag = 1 钥匙串
        }
        for( i in keyEntityArr){
            itemList.add(MainItem(i.id, i.name,0)) //tag = 0 钥匙
        }
        itemList = Util.sortMainItems(itemList, mainPwd)
        return itemList
    }
    //绑定recyclerView的adapter,通过recyclerView展示item
    fun showMainItems(){
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager =layoutManager
        val adapter = MainAdapter(itemList, this, mainPwd) //val adapter = KeyAdapter(itemList,this,mainPwd)
        recyclerView.adapter = adapter
    }


    //----------------------------右上角选项栏相关函数
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //将menu_main绑定到本活动中
        menuInflater.inflate(R.menu.menu_main, menu)
        //对搜索选项进行设置//搜索选项
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setQueryHint("输入名称的关键字")
        searchView.setOnCloseListener (object:SearchView.OnCloseListener{ //关闭搜索框
            override fun onClose(): Boolean {
                itemList = getMainItems()
                searchView.clearFocus()  //可以收起键盘
                searchView.onActionViewCollapsed()//关闭搜索栏
                Toast.makeText(this@MainActivity,"关闭查询", Toast.LENGTH_SHORT).show();
                showMainItems()
                return true
            }
        })
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query:String): Boolean {//点击查询按钮
                itemList = getMainItems()
                if (query != "") { itemList = Util.Query(itemList, query, mainPwd, this@MainActivity) }
                showMainItems()
                return true
            }
            override fun onQueryTextChange(newText:String): Boolean{ //更改搜索内容
                itemList = getMainItems()
                if(newText != "") { itemList = Util.Query(itemList, newText, mainPwd, this@MainActivity) }
                showMainItems()
                return true
            }
        })

        return true
    }


    //------------------------------指定各选项的动作
    override fun onOptionsItemSelected(item: MenuItem):Boolean{
        return when (item.itemId) {
            R.id.search ->{ true } //在onCreateOptionsMenu中绑定
            R.id.bug_test->{ // 按钮缩小  bug测试按钮  可否去掉?
                showMainItems()
                true
            }
            R.id.add_key-> { //新建钥匙
                add_key()
                true
            }

            //二级菜单
            R.id.add_keys -> { //添加钥匙串
                add_cluster()
                true
            }
            R.id.del_keys ->{ //删除当前的钥匙串
                if(cluster_id==1 &&  delRootConfirm >=1){
                    //简单的提示已复制的消息框
                    Toast.makeText(this@MainActivity, "删除根钥匙串会删除所有钥匙\n如果确定删除，请再点击删除按钮"+delRootConfirm.toString()+"次", Toast.LENGTH_SHORT).show()
                    delRootConfirm -= 1
                    //也可以定义一个新的函数用于清空根钥匙串
                } else if(delRootConfirm>=3){ //对于非根钥匙串只确认一次
                    Toast.makeText(this@MainActivity, "确定要删除吗？\n如果确定，请再点击删除按钮1次", Toast.LENGTH_SHORT).show()
                    delRootConfirm -= 1
                } else{
                    del_cluster(cluster_id)
                    if(cluster_id != 1){finish()} //只要删除的不是根串就停止当前activity
                    else{
                        itemList = getMainItems() //获取本活动中需要展示的item存入itemList
                        showMainItems()
                        delRootConfirm=3 //还原到3次确认
                    }
                }
                true
            }
            R.id.input_Pwd ->{ //输入主密码
                callInputPwd()
                true
            }
            R.id.modify_Pwd ->{ //修改主密码
                val modifyDialog = ModifyPwdDialogFragment()
                modifyDialog.show(supportFragmentManager, "ModifyPwdDialogFrag")
                true
            }
            R.id.backup ->{ //备份
                Backup()
                true
            }
            R.id.restore ->{//还原
                Restore()
                true
            }
            R.id.moreinfo ->{ //更多信息
                MoreInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //各选项对应的动作
    //新建钥匙
    fun add_key(){//启动添加钥匙的活动
        var intent= Intent(this,AddKeyActivity::class.java)
        intent.putExtra("mainPwd",mainPwd)
        intent.putExtra("cluster_id",cluster_id)
        startActivity(intent)
    }
    //添加钥匙串
    fun add_cluster() {
        var intent= Intent(this,AddClusterActivity::class.java)
        intent.putExtra("mainPwd",mainPwd)
        intent.putExtra("cluster_id",cluster_id)
        startActivity(intent)
    }
    //删除钥匙串
    fun del_cluster(cluster_id:Int){
        var keyEntityArr = keyDao.getByFId(cluster_id)
        for(i in keyEntityArr){
            keyDao.delete(i)
        }
        var clusterEntityArr = clusterDao.getByFId(cluster_id) //子钥匙串
        for(i in clusterEntityArr){
            del_cluster(i.id)
        }
        if(cluster_id!=1) { clusterDao.delete(clusterDao.getById(cluster_id)) }//不能删除根串
    }



    //修改主密码
    override fun onDialogPositiveClick(dialog: DialogFragment, newPwd: String){
        //获取到新的主密码,开始先解密后加密. 主要步骤是将三个表的密文全部取出,然后尝试解密,解密成功的重新加密,不成功的什么也不做
        //对于钥匙串,添加钥匙串的时候仅加密了钥匙串名
        var AllClusterArr = clusterDao.allButRoot()
        for (item in AllClusterArr) {
            try{
                var tmpStr = AESCrypt.decrypt(item.cluster_name, mainPwd) //尝试解密
                item.cluster_name = AESCrypt.encrypt(tmpStr, newPwd) //使用新密码加密
            }catch(e: BadPaddingException){
                //什么也不做
            }
        }
        clusterDao.updateArr(AllClusterArr) //写回数据库
        //对于钥匙
        var AllKeyArr = keyDao.all()
        for (item in AllKeyArr) {
            var tmpStrArr = KeyToArray(item) //将钥匙转化为字符数组
            try{
                var tmpStr:String
                for (i in tmpStrArr.indices){
                   tmpStr = AESCrypt.decrypt(tmpStrArr[i], mainPwd)
                   tmpStrArr[i] =  AESCrypt.encrypt(tmpStr, newPwd) //使用新密码加密
                }
                ArrayToKey(item, tmpStrArr)
            }catch(e: BadPaddingException){
                //什么也不做
            }
        }
        keyDao.updateArr(AllKeyArr) //写回数据库
        //对于额外字段
        //暂时未处理
        var AllexfieldArr = exfieldDao.all()
        for (item in AllexfieldArr) {
            var tmpStrArr = ExfieldToArray(item)//转化为字符数组
            try{
                var tmpStr:String
                for (i in tmpStrArr.indices){
                    tmpStr = AESCrypt.decrypt(tmpStrArr[i], mainPwd)
                    tmpStrArr[i] =  AESCrypt.encrypt(tmpStr, newPwd) //使用新密码加密
                }
                ArrayToExfield(item, tmpStrArr)
            }catch(e: BadPaddingException){
                //遇到未解密的,什么也不做
            }
        }
        exfieldDao.updateArr(AllexfieldArr)
        //最后将成员变量更新
        mainPwd = newPwd
        onResume()//刷新一下
    }

    //备份
    fun Backup(){
        //跳转到备份活动
        var intent= Intent(this,BackupActivity::class.java)
        startActivity(intent)
    }

    //还原
    fun Restore(){
        //跳转到还原活动
        var intent= Intent(this,RestoreActivity::class.java)
        startActivity(intent)
    }
    //更多信息
    fun MoreInfo(){
        val intent= Intent(this,AboutActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent()
        intent.putExtra("mainPwd",mainPwd)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
