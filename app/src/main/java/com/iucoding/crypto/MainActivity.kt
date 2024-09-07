package com.iucoding.crypto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iucoding.crypto.ui.theme.CryptoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cryptoManager = CryptoManager()
        enableEdgeToEdge()
        setContent {
            CryptoTheme {
                var messageToEncrypt by remember {
                    mutableStateOf("")
                }
                var messageToDecrypt by remember {
                    mutableStateOf("")
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    TextField(
                        value = messageToEncrypt,
                        onValueChange = { messageToEncrypt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Encrypt string") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        val scope = rememberCoroutineScope()
                        Button(onClick = {
                            scope.launch(Dispatchers.IO) {
                                val bytes = messageToEncrypt.encodeToByteArray()
                                val file = File(filesDir, FILE_NAME)
                                if (!file.exists()) {
                                    file.createNewFile()
                                }
                                val fos = FileOutputStream(file)
                                messageToDecrypt = try {
                                    cryptoManager.encrypt(
                                        bytes = bytes,
                                        outputStream = fos
                                    )
                                } catch (e: Exception) {
                                    e.localizedMessage.orEmpty()
                                }
                            }
                        }) {
                            Text(text = "Encrypt")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            scope.launch(Dispatchers.IO) {
                                val file = File(filesDir, FILE_NAME)
                                val fis = FileInputStream(file)
                                messageToEncrypt = try {
                                    cryptoManager.decrypt(
                                        inputStream = fis
                                    )
                                } catch (e: Exception) {
                                    e.localizedMessage.orEmpty()
                                }
                            }
                        }) {
                            Text(text = "Decrypt")
                        }
                    }
                    Text(text = messageToDecrypt)
                }
            }
        }
    }

    companion object {
        private const val FILE_NAME = "secret.txt"
    }
}
