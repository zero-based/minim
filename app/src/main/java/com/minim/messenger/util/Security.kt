package com.minim.messenger.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object Security {

    private const val ALGORITHM = "AES"
    private const val CIPHER = "AES/GCM/NoPadding"

    private val cipher: Cipher = Cipher.getInstance(CIPHER)
    private lateinit var secretKeySpec: SecretKeySpec
    private lateinit var ivParameterSpec: IvParameterSpec

    fun getRandomString(): String {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..16).map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun setKey(charset: String) {
        secretKeySpec = SecretKeySpec(charset.toByteArray(), ALGORITHM)
        val iv = ByteArray(16)
        val charArray = charset.toCharArray()
        (0 until charArray.size).forEach { iv[it] = charArray[it].toByte() }
        ivParameterSpec = IvParameterSpec(iv)
    }

    fun encrypt(text: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedValue = cipher.doFinal(text.toByteArray())
        return Base64.encodeToString(encryptedValue, Base64.NO_PADDING)
    }

    fun decrypt(encryptedText: String): String {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedByteValue = cipher.doFinal(Base64.decode(encryptedText, Base64.NO_PADDING))
        return String(decryptedByteValue)
    }

}