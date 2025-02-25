@file:Suppress("FunctionName")

package com.yandex.div.compose.interop

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.DivParsingEnvironment
//import com.yandex.div.internal.util.IOUtils
import com.yandex.div.json.ParsingErrorLogger
//import com.yandex.div.picasso.PicassoDivImageLoader
import com.yandex.div2.DivData
//import com.yandex.divkit.sample.SampleDivActionHandler

import org.json.JSONObject

private val noopReset: (View) -> Unit = {}

@Composable
@Preview
public fun App(

) {
    MaterialTheme {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("battery level is: ${battryManager.getBatteryLevel()}")
//        }
        LoginScreen { username, password ->
            println("Preview Login Clicked: Username = $username, Password = $password")
        }
//        val data = IOUtils.toString(LocalDivContext.current.assets.open("application/demo.json"))
//        val divJson = JSONObject(data)
//        val templatesJson = divJson.optJSONObject("templates")
//        val cardJson = divJson.getJSONObject("card")
//         val environment = DivParsingEnvironment(ParsingErrorLogger.ASSERT).apply {
//            if (templatesJson != null) parseTemplates(templatesJson)
//        }
//        val divData = DivData(environment, cardJson)
//
//
//        DivView (divData, DivDataTag.INVALID )
    }
}


@Composable
public fun LoginScreen(onLoginClick: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Login"

                )

                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                // Login Button
                Button(
                    onClick = { onLoginClick(username, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
            }
        }
    }
}

@Composable
public fun DivView(
    data: DivData,
    tag: DivDataTag,
    modifier: Modifier = Modifier
) {
    val context: Div2Context = LocalDivContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            Div2View(context)
        },
        update = { view ->
            view.setData(data, tag)
        },
        onReset = noopReset,
        onRelease = { view ->
            view.cleanup()
        }
    )
}
