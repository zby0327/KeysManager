package com.example.keysmanager

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AESCrypt{
    fun encrypt(input:String,password:String): String {
        //对主密码进行散列形成定长的字节数组
        val md = MessageDigest.getInstance("MD5")
        val pwdMD = md.digest(password.toByteArray(Charsets.UTF_8)) //输入字节数组,返回固定长度为16的字节数组
        val keyspec: SecretKeySpec? = SecretKeySpec(pwdMD,"AES") //使用长度为16字节的数组作为AES加密的密钥
        //创建cipher对象
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keyspec)
        //加密后是字节数组
        val resByteArr = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
        //转换成字符串
        return android.util.Base64.encode(resByteArr, android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
    }
    fun decrypt(input:String,password:String): String {
        //对主密码进行散列形成定长的字节数组
        val md = MessageDigest.getInstance("MD5")
        val pwdMD = md.digest(password.toByteArray(Charsets.UTF_8))
        val keyspec: SecretKeySpec? = SecretKeySpec(pwdMD,"AES") //使用长度为16字节的数组作为AES加密的密钥
        //创建cipher对象
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keyspec)
        //先用Base64将数据库中的字符串转换成字节数组,然后解密
        val inputByteArr = android.util.Base64.decode(input.toByteArray(Charsets.UTF_8),android.util.Base64.DEFAULT)
        return cipher.doFinal(inputByteArr).toString(Charsets.UTF_8)
    }
}