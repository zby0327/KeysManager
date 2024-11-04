package com.example.keysmanager

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EditableKAdapter (private var titles:ArrayList<String>, //类的成员变量
                        private var values:ArrayList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //item的类型
    val KEY_FIELD = 0
    val TIMEFIELD = 1
    val EX_FIELD = 2
    //定义Item类型
    override fun getItemViewType(position: Int): Int {
        if(position<6){
            return KEY_FIELD
        }else if(position == 6){
            return TIMEFIELD
        } else if(position>=7){
            return EX_FIELD
        }
        return super.getItemViewType(position)
    }
    //定义不同的holder
    inner class ViewHolder(view:View) : RecyclerView.ViewHolder(view) { //viewholder是adapter中的一个元素
        //从视图层获取元素
        val Title: TextView = view.findViewById(R.id.title) //修改title的
        val Value: EditText = view.findViewById(R.id.value) //修改value的
    }
    inner class TimeViewHolder(view:View) : RecyclerView.ViewHolder(view){
        val Title: TextView = view.findViewById(R.id.title)
        val Value: TextView = view.findViewById(R.id.value)
    }
    inner class ExfieldViewHolder(view:View) : RecyclerView.ViewHolder(view) {
        val ExTitle: EditText = view.findViewById(R.id.title)
        val ExValue: EditText = view.findViewById(R.id.value)
        val Btn: Button = view.findViewById(R.id.btnDelExf)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder { //绑定视图层
        var view: View
        if(viewType==KEY_FIELD){
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_editablek, parent, false)
            return ViewHolder(view)
        } else if(viewType==EX_FIELD) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_exfield, parent, false)
            return ExfieldViewHolder(view)
        } else{
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_timefield, parent, false)
            return TimeViewHolder(view)
        }
    }

    //用titles和values数组来初始化编辑页面(针对每一个position)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //构造监听器
        val valuewatcher = object:TextWatcher {
            override fun afterTextChanged(s: Editable?) { //文本内容改变了，需要交还回去。以便同步value
                if (s != null) { values[holder.adapterPosition] = s.toString() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        val titlewatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null) { titles[holder.adapterPosition] = s.toString() }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        }
        //根据不同类型的holder设置不同的监听器
        if(holder is ViewHolder){
            holder.Title.text = titles[position] //利用字符串数组设置标题
            holder.Value.setText(values[position])
            //为每个文本输入框添加编辑监听器
            holder.Value.onFocusChangeListener = View.OnFocusChangeListener(
                fun(_:View, hasFocus:Boolean){
                    if(hasFocus){ holder.Value.addTextChangedListener(valuewatcher) }
                    else{ holder.Value.removeTextChangedListener(valuewatcher) }
                })
        } else if (holder is TimeViewHolder){
            holder.Title.text = titles[position]
            holder.Value.text = values[position]
        } else if(holder is ExfieldViewHolder){
            holder.ExTitle.setText(titles[position])
            holder.ExValue.setText(values[position])
            //可以add的监听器应该及时remove
            holder.ExTitle.onFocusChangeListener = View.OnFocusChangeListener(
                fun (_:View,hasFocus:Boolean){
                    if(hasFocus){ holder.ExTitle.addTextChangedListener(titlewatcher) }
                    else{ holder.ExTitle.removeTextChangedListener(titlewatcher) }
                })
            holder.ExValue.onFocusChangeListener = View.OnFocusChangeListener(
                fun(_:View, hasFocus:Boolean){
                    if(hasFocus){ holder.ExValue.addTextChangedListener(valuewatcher) }
                    else{ holder.ExValue.removeTextChangedListener(valuewatcher) }
                })
            holder.Btn.setOnClickListener { rmvItem(holder.adapterPosition) }
        }

    }
    //获取条目的数量
    override fun getItemCount(): Int {
        return titles.size
    }

    //添加一个自定义字段.在Activity中调用
    fun addItem(position: Int){
        titles.add(position,"")
        values.add(position,"")//notifyDataSetChanged()
        notifyItemInserted(position)
    }
    //移除一个自定义字段
    fun rmvItem(position: Int){
        titles.removeAt(position)
        values.removeAt(position)
        notifyItemRemoved(position)
    }

}
