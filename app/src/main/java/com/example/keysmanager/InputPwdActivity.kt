package com.example.keysmanager

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout

//输入主密码的活动
class InputPwdActivity : AppCompatActivity() {

    var mainPwd:String?=""
    var pwdVisible = false
    lateinit var btnConfirm: Button
    lateinit var btnMode: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inputpwd)
        var tmp: EditText = findViewById(R.id.mainPwd) //设置默认的 mainKey
        tmp.setText(mainPwd)
        btnConfirm = this.findViewById(R.id.confirmBtn)
        btnMode = this.findViewById(R.id.showMode)
        btnConfirm.setOnClickListener(View.OnClickListener { clickConfirm() })
        btnMode.setOnClickListener(View.OnClickListener { switchMode() })
    }

    fun clickConfirm(){ // click函数的参数必须是 x:View
        val intent= Intent()
        mainPwd =findViewById<EditText>(R.id.mainPwd).text.toString()
        intent.putExtra("mainPwd",mainPwd)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }

    override fun onResume(){
        super.onResume()
        val layout=findViewById<ConstraintLayout>(R.id.inputpwd_background)
        layout.setBackgroundResource(R.drawable.mpwd_background)
    }

    fun switchMode(){ // click函数的参数必须是 x:View
        val pwd= findViewById<EditText>(R.id.mainPwd)
        val mode_btn = findViewById<Button>(R.id.showMode)
        if(pwdVisible){ //如果可见,则设为不可见
            pwd.transformationMethod = PasswordTransformationMethod.getInstance() //隐藏密码
            mode_btn.text="显示"
        }else{//如果不可见,设为可见
            pwd.transformationMethod = HideReturnsTransformationMethod.getInstance() //显示密码
            mode_btn.text="隐藏"
        }
        pwdVisible = !pwdVisible
    }
}
