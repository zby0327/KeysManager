package com.example.keysmanager

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import javax.crypto.BadPaddingException

class MainAdapter(val itemList: List<MainItem>,
                      val context:Context,
                      val mainPwd:String) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val itemName = view.findViewById<TextView>(R.id.itemName)
    }

    //构造ViewHolder,定义其动作
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mainitem, parent, false)
        val viewHolder = ViewHolder(view)
        //定义点击mainItem之后的动作
        viewHolder.itemView.setOnClickListener(View.OnClickListener {
            //获取点击的item
            val position = viewHolder.adapterPosition
            val mainItem = itemList[position]
            //尝试解密判断主密码是否正确
            try{
                AESCrypt.decrypt(mainItem.name, mainPwd)
                //无错误，准备跳转：如果tag==0则跳转showKeyActivity，如果tag==1则跳转MainActivity

                when(mainItem.tag){
                    0->{
                        val intent = Intent(context, ShowKeyActivity::class.java)
                        intent.putExtra("mainPwd",mainPwd)
                        intent.putExtra("key_id",mainItem.id)
                        context.startActivity(intent)
                    }
                    1->{
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("mainPwd",mainPwd)
                        intent.putExtra("cluster_id",mainItem.id)
                        (context as MainActivity).startActivityForResult(intent,0) //需要得到传回的主密码
                    }
                }
            }catch(e:BadPaddingException){
                Toast.makeText(parent.context, "主密码错误，请于右上角菜单栏中重新输入主密码", Toast.LENGTH_SHORT).show() //圆角错误提示
            }
            })
        return viewHolder
    }
    //定义ViewHolder如何显示
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //展示钥匙
        val item = itemList[position]
        //钥匙名字和钥匙串名字在显示之前需要尝试进行解密
        try{
            if(item.st==-1)
                holder.itemName.text = AESCrypt.decrypt(item.name,mainPwd)  //只需解密名字
            else { //高亮显示关键字
                val spannableString :SpannableString=SpannableString(AESCrypt.decrypt(item.name,mainPwd))
                spannableString.setSpan( ForegroundColorSpan(Color.parseColor("#ff0000")), item.st, item.ed, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                holder.itemName.setText(spannableString)
            }
        }catch(e:BadPaddingException){//解密错误直接展示密文即可
            holder.itemName.text = item.name
        }
        //钥匙串和钥匙使用不同的显示图片
        when(item.tag){
            0->{ //钥匙//src="@drawable/key"  xml中已经是key.png了
            }
            1->{ //钥匙串
                holder.imageView.setImageResource( R.drawable.keys)
            }
        }
    }

    override fun getItemCount() = itemList.size

}