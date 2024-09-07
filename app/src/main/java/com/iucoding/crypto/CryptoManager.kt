package com.iucoding.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // region attributes/instances
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_TYPE).apply {
            load(null)
        }
    }
    // endregion

    // region functions
    suspend fun encrypt(bytes: ByteArray, outputStream: OutputStream): String {
        return withContext(dispatcher) {
            val encryptCipher = getEncryptCipher()
            val encryptedBytes = encryptCipher.doFinal(bytes)
            outputStream.use {
                it.write(encryptCipher.iv.size)
                it.write(encryptCipher.iv)
                it.write(encryptedBytes.size)
                it.write(encryptedBytes)
            }
            encryptedBytes.decodeToString()
        }
    }

    suspend fun decrypt(inputStream: InputStream): String {
        return withContext(dispatcher) {
            inputStream.use {
                val ivSize = it.read()
                val iv = ByteArray(ivSize)
                it.read(iv)

                val encryptedBytesSize = it.read()
                val encryptedBytes = ByteArray(encryptedBytesSize)
                it.read(encryptedBytes)

                getDecryptCipherForIv(iv)
                    .doFinal(encryptedBytes)
                    .decodeToString()
            }
        }
    }
    // endregion


    // region private functions
    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getEncryptCipher(): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getKey())
        }
    }
    // endregion

    companion object {
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val KEY_ALIAS = "secret"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
