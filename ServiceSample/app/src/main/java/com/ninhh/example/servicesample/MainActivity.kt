package com.ninhh.example.servicesample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.ninhh.example.servicesample.ui.theme.ServiceSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServiceSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val localContext = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(localContext, ForegroundServiceActivity::class.java)
            localContext.startActivity(intent)
        },
        modifier = Modifier.wrapContentSize()
    ){
        Text(text = "This is a button")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ServiceSampleTheme {
        Greeting("Android")
    }
}