package com.example.keysmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ShowKeyAdapter (private val titles:ArrayList<String>, //加了private val后 ， 参数就变成了类的成员变量
                      private val values:ArrayList<String>,
                      private val context:Context): RecyclerView.Adapter<ShowKeyAdapter.ViewHolder>() {
    lateinit var viewHolder:ViewHolder

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val Title = view.findViewById<TextView>(R.id.title_show)
        val Value = view.findViewById<TextView>(R.id.value_show)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_showkey, parent, false)//绑定viewholder的表示层
        viewHolder = ViewHolder(view)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.Title.text = titles[position]
        holder.Value.text = values[position]
        val listener = View.OnClickListener { // 将当前字段的value 转到粘贴板上
            //获取value到粘贴板
            var tmpStr = values[position]
            var str: ClipData = ClipData.newPlainText("Label", tmpStr)
            var cm: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(str)
            if(tmpStr == ""){
                Toast.makeText(context,"无数据", Toast.LENGTH_SHORT).show()
            } else {//简单的提示已复制的消息框
                Toast.makeText(context,"已复制到粘贴板：\n"+tmpStr, Toast.LENGTH_SHORT).show()
            }
        }
        holder.Title.setOnClickListener(listener)
        holder.Value.setOnClickListener(listener)
    }

    override fun getItemCount(): Int {
        return values.size
    }
}