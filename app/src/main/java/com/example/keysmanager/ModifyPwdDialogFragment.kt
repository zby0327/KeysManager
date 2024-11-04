package com.example.keysmanager

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import java.lang.ClassCastException
import java.lang.IllegalStateException

class ModifyPwdDialogFragment : DialogFragment() {

    //设置监听器用于向父活动传递消息
    internal lateinit var listener: ModifyMainPwdDialogListener
    interface ModifyMainPwdDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, newPwd: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)
            //得到布局的inflater,并获取View
            val inflater = requireActivity().layoutInflater
            val tmplayout = inflater.inflate(R.layout.dialogfragment_modifypwd, null)
            //设置布局
            builder.setView(tmplayout).setMessage("所有已解密钥匙将被重新加密!")
                .setNegativeButton("取消", DialogInterface.OnClickListener{dialog, id -> })
                .setPositiveButton("确定",DialogInterface.OnClickListener{dialog, id ->
                    val newPwd = tmplayout.findViewById<EditText>(R.id.newmainpwd).text.toString()
                    listener.onDialogPositiveClick(this, newPwd)
            })
            builder.create()
        }?:throw IllegalStateException("act cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //确保父活动实现了接口
        try{
            listener = context as ModifyMainPwdDialogListener
        } catch (e :ClassCastException){
            throw ClassCastException((context.toString() + "必须实现接口"))
        }
    }
}