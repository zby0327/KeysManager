package com.example.keysmanager

import android.content.Context
import androidx.room.*
import androidx.room.Room.*


//定义三个数据表对应的Entity对象和Dao对象
//--------------钥匙串
@Entity //钥匙串Entity对象,不包含任何外键
data class ClusterEntity(
    @ColumnInfo //父结点id
    var father_id: Int,
    @ColumnInfo //钥匙串名
    var cluster_name: String = "",
    @ColumnInfo //钥匙串路径,作为辅助
    var cluster_path: String = "",
    @PrimaryKey(autoGenerate = true) //主键自动分配
    var id: Int = 0//钥匙串id
)
fun ClusterToArray(cluster:ClusterEntity):Array<String>{
    val arr = Array<String>(2){""}
    arr[0] = cluster.cluster_name
    arr[1] = cluster.cluster_path
    return arr
}
fun ArrayToCluster(cluster: ClusterEntity, arr:Array<String>):ClusterEntity{
    cluster.cluster_name = arr[0]
    cluster.cluster_path = arr[1]
    return cluster
}

@Dao //钥匙串的Dao接口
interface ClusterDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) //不允许插入已有钥匙串
    fun insert(cluster:ClusterEntity)
    @Delete
    fun delete(cluster: ClusterEntity)
    @Query("SELECT * FROM clusterentity WHERE id NOT IN (1)")
    fun allButRoot():Array<ClusterEntity>
    @Query("SELECT * FROM clusterentity")
    fun all():Array<ClusterEntity>
    @Query("SELECT * FROM clusterentity WHERE id = :id")
    fun getById(id:Int):ClusterEntity
    @Query("SELECT * FROM clusterentity WHERE father_id = :cluster_id AND id NOT IN (1)" ) //查找子钥匙串,将根钥匙串排除在外
    fun getByFId(cluster_id:Int):Array<ClusterEntity>
    @Update
    fun update(cluster: ClusterEntity) //只能更改除了主键以外的值
    @Update
    fun updateArr(clusterarr: Array<ClusterEntity>)
}


//--------------钥匙
/**
  (foreignKeys = [ForeignKey(
entity = ClusterEntity::class,
parentColumns = arrayOf("id"),
childColumns = arrayOf("cluster_id"))]
) //钥匙Entity对象
 */
@Entity
data class KeyEntity(
    @ColumnInfo //钥匙名
    var name : String = "",
    @ColumnInfo //账户
    var account : String = "",
    @ColumnInfo //密码1
    var pwd1 : String = "",
    @ColumnInfo //密码2
    var pwd2 : String = "",
    @ColumnInfo //url
    var url : String = "",
    @ColumnInfo //备注
    var note : String = "",
    @ColumnInfo //创建时间
    var createtime : String = "",
    @ColumnInfo //钥匙到钥匙串是多对一关系,因此钥匙串id是外键.
    var cluster_id : Int,
    @PrimaryKey(autoGenerate = true) //主键自动分配
    var id : Int = 0
)

fun KeyToArray(key:KeyEntity):Array<String>{
    val arr = Array<String>(7){""}
    arr[0] = key.name
    arr[1] = key.account
    arr[2] = key.pwd1
    arr[3] = key.pwd2
    arr[4] = key.url
    arr[5] = key.note
    arr[6] = key.createtime
    return arr
}
fun ArrayToKey(key:KeyEntity, arr:Array<String>):KeyEntity{
    key.name = arr[0]
    key.account = arr[1]
    key.pwd1 = arr[2]
    key.pwd2 = arr[3]
    key.url = arr[4]
    key.note = arr[5]
    key.createtime = arr[6]
    return key
}
fun ArrayToKey2(key:KeyEntity, arr:Array<String>):KeyEntity{
    key.id = arr[0].toInt()
    key.cluster_id = arr[1].toInt()
    key.name = arr[2]
    key.account = arr[3]
    key.pwd1 = arr[4]
    key.pwd2 = arr[5]
    key.url = arr[6]
    key.note = arr[7]
    key.createtime = arr[8]
    return key
}

@Dao //钥匙的Dao接口
interface KeyDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(key: KeyEntity)
    @Delete
    fun delete(key: KeyEntity)
    @Query("SELECT * FROM keyentity")
    fun all():Array<KeyEntity>
    @Query("SELECT * FROM keyentity WHERE id = :id")
    fun getById(id:Int):KeyEntity
    @Query("SELECT * FROM keyentity WHERE cluster_id= :cluster_id")
    fun getByFId(cluster_id:Int):Array<KeyEntity>
    @Query("SELECT * FROM keyentity WHERE createtime= :createtime")
    fun getByTime(createtime: String):KeyEntity
    @Update
    fun update(key: KeyEntity) //只能更改除了主键以外的值
    @Update
    fun updateArr(keyarr: Array<KeyEntity>)
}


//--------------自定义字段
/**
(foreignKeys = [ForeignKey(
entity = KeyEntity::class,
parentColumns = arrayOf("id"),
childColumns = arrayOf("key_id"))]
) //自定义字段Entity对象
**/
@Entity
data class ExfieldEntity(
    @ColumnInfo //自定义字段到钥匙是多对一关系,因此钥匙id是外键.
    var key_id : Int,
    @ColumnInfo
    var title: String = "",
    @ColumnInfo
    var content: String = "",
    @PrimaryKey(autoGenerate = true) //主键自动分配
    var id : Int = 0
)
fun ExfieldToArray(exfield:ExfieldEntity):Array<String>{
    val arr = Array<String>(2){""}
    arr[0] = exfield.title
    arr[1] = exfield.content
    return arr
}
fun ArrayToExfield(exfield: ExfieldEntity, arr:Array<String>):ExfieldEntity{
    exfield.title = arr[0]
    exfield.content = arr[1]
    return exfield
}
fun ArrayToExfield2(exfield: ExfieldEntity, arr:Array<String>):ExfieldEntity{
    exfield.id = arr[0].toInt()
    exfield.key_id = arr[1].toInt()
    exfield.title = arr[2]
    exfield.content = arr[3]
    return exfield
}

@Dao
interface ExfieldDao {
    @Insert
    fun insert(exfield: ExfieldEntity)
    @Delete
    fun delete(exfield: ExfieldEntity)
    @Delete
    fun deleteArr(exfieldArr: Array<ExfieldEntity>)
    @Query("SELECT * FROM exfieldentity WHERE key_id = :key_id ") //返回所有属于某个钥匙的自定义字段
    fun getByKey(key_id:Int):Array<ExfieldEntity>
    @Query("SELECT * FROM exfieldentity")
    fun all():Array<ExfieldEntity>
    @Update
    fun update(exfield: ExfieldEntity)
    @Update
    fun updateArr(exfieldArr: Array<ExfieldEntity>)
}


//----------------数据库对象
@Database(entities = [ClusterEntity::class, KeyEntity::class, ExfieldEntity::class], version = 1, exportSchema = false)
internal abstract class KMDatabase :RoomDatabase() {
    //获取操作数据库的接口
    abstract fun clusterDao(): ClusterDao
    abstract fun keyDao(): KeyDao
    abstract fun exfieldDao(): ExfieldDao

    //单例设计模式防止实例化
    companion object Factory {
        private var database: KMDatabase? = null
        fun create(ctx:Context) : KMDatabase { //设计一个封装后的函数,避免每次连接都要提供数据库名称
            if(database == null){
                database = databaseBuilder(ctx,
                KMDatabase::class.java,
                    "km-database").allowMainThreadQueries().build()
            }
            return database as KMDatabase
        }

    }
}




