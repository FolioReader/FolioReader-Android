package org.readium.r2.streamer.container

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.spec.SecretKeySpec

object AesHelper {

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    private fun getKeyDigest(myKey: String): ByteArray {
        val key = myKey.toByteArray(charset(BYTE_ENCODING))
        val sha = MessageDigest.getInstance(KEY_DIGEST_ENCODING)
        return sha.digest(key)
    }

    @Throws(UnsupportedEncodingException::class, NoSuchAlgorithmException::class)
    fun getAesKey(myKey: String): ByteArray {
        val secretKey = SecretKeySpec(getKeyDigest(myKey), ENCRYPTION_ALGORITHM)
        return secretKey.encoded
    }

    private const val BYTE_ENCODING = "UTF-8"
    private const val KEY_DIGEST_ENCODING = "SHA-256"
    const val ENCRYPTION_ALGORITHM = "AES"
}
