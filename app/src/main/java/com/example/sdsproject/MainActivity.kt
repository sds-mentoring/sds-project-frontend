package com.example.sdsproject

import android.content.Context
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.example.sdsproject.ui.theme.SDSProjectTheme

import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.domain.enum.LoginBehavior
import com.navercorp.nid.oauth.util.NidOAuthCallback
import com.navercorp.nid.profile.domain.vo.NidProfile
import com.navercorp.nid.profile.util.NidProfileCallback

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

import okhttp3.OkHttpClient
import okhttp3.Request

import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Can this clientSecret be placed in the user application?
        NidOAuth.initialize(
            context = this,
            clientId = "mZgJxaEMILClmQ4Z9Z4n",
            clientSecret = "lGnr0KNcb8",
            clientName = "테스트 어플리케이션",
        )
        NidOAuth.behavior = LoginBehavior.DEFAULT

        setContent {
            SDSProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainScreen(this)
                }
            }
        }
    }
}

sealed class RequestState {
    data object Idle : RequestState()
    data object Loading : RequestState()
    data class Success(val body: String) : RequestState()
    data class Error(val body: String) : RequestState()
}

@Composable
fun MainScreen(context: Context) {
    var state by remember { mutableStateOf<RequestState>(RequestState.Idle) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is RequestState.Idle -> {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SendRequestButton(onClick = {
                        state = RequestState.Loading
                        coroutineScope.launch {
                            state = performRequest()
                        }
                    })
                    NaverLoginButton(onClick = {
                        state = RequestState.Loading
                        coroutineScope.launch {
                            when (val result = loginViaNaver(context)) {
                                is RequestState.Success -> state = getUserProfileFromNaver()
                                else -> state = result
                            }
                        }
                    })
                }
            }

            is RequestState.Loading -> {
                LoadingSpinner()
            }

            is RequestState.Success, is RequestState.Error -> {
                // Main screen still shows button behind dialog
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SendRequestButton(onClick = {})
                    NaverLoginButton(onClick = {})
                }
            }
        }
    }

    // Show dialog overlay for success/error
    when (val currentState = state) {
        is RequestState.Success -> {
            ResponseDialog(
                body = currentState.body,
                isSuccess = true,
                onClose = { state = RequestState.Idle }
            )
        }

        is RequestState.Error -> {
            ResponseDialog(
                body = currentState.body,
                isSuccess = false,
                onClose = { state = RequestState.Idle }
            )
        }

        else -> {}
    }
}

@Composable
fun SendRequestButton(onClick: () -> Unit) {
    val indigo = Color(0xFF3F51B5)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = indigo),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(56.dp)
            .width(220.dp)
    ) {
        Text(
            text = "요청 보내기",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview
@Composable
fun SendRequestButtonPreview() {
    SendRequestButton(onClick = {})
}

@Composable
fun NaverLoginButton(onClick: () -> Unit) {
    val naverGreen = Color(0xFF03A94D)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = naverGreen),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(56.dp)
            .width(220.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.naver_login),
            contentDescription = "네이버 로그인 버튼",
            modifier = Modifier.fillMaxHeight(),
            contentScale = ContentScale.FillHeight,
        )
    }
}

@Preview
@Composable
fun NaverLoginButtonPreview() {
    NaverLoginButton(onClick = {})
}

@Composable
fun LoadingSpinner() {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val indigo = Color(0xFF3F51B5)
    val indigoLight = Color(0xFFC5CAE9)

    Box(
        modifier = Modifier
            .size(64.dp)
            .rotate(rotation)
            .drawBehind {
                // Background track
                drawArc(
                    color = indigoLight,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                // Spinning arc
                drawArc(
                    color = indigo,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }
    )
}

@Composable
fun ResponseDialog(body: String, isSuccess: Boolean, onClose: () -> Unit) {
    val successBg = Color(0xFFDCEDC8) // light green
    val errorBg = Color(0xFFFFCDD2)   // light red
    val successIcon = Color(0xFF4CAF50) // green
    val errorIcon = Color(0xFFF44336)   // red

    val bgColor = if (isSuccess) successBg else errorBg
    val iconColor = if (isSuccess) successIcon else errorIcon
    val iconText = if (isSuccess) "\u2713" else "\u2717"

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Response box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .padding(16.dp)
            ) {
                Column {
                    // Icon row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(iconColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iconText,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = body,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFF333333),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Close button
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF333333)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .width(160.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBDBDBD),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Text(
                    text = "Close",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

suspend fun performRequest(): RequestState {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("http://10.0.2.2:3000/health")
                .get()
                .build()

            val json = Json { prettyPrint = true }
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            val formatted = try {
                val parsed = json.parseToJsonElement(body)
                json.encodeToString(parsed)
            } catch (e: Exception) {
                body
            }

            if (response.isSuccessful) {
                RequestState.Success(formatted)
            } else {
                RequestState.Error(body)
            }
        } catch (e: Exception) {
            RequestState.Error(e.message ?: "알 수 없는 에러가 발생하였습니다.")
        }
    }
}

suspend fun loginViaNaver(context: Context): RequestState =
    suspendCoroutine { continuation ->
        val callback = object : NidOAuthCallback {
            override fun onSuccess() =
                continuation.resume(RequestState.Success("네이버를 통한 로그인에 성공하였습니다."))

            override fun onFailure(errorCode: String, errorDesc: String) =
                continuation.resume(RequestState.Error("네이버를 통한 로그인에 실패하였습니다: $errorDesc"))
        }

        NidOAuth.requestLogin(context, callback)
    }

suspend fun getUserProfileFromNaver(): RequestState =
    suspendCoroutine { continuation ->
        val callback = object : NidProfileCallback<NidProfile> {
            override fun onSuccess(result: NidProfile) {
                val body = Json { prettyPrint = true }.encodeToString(
                    mapOf(
                        "id" to result.profile.id,
                        "name" to result.profile.name,
                        "email" to result.profile.email
                    )
                )

                continuation.resume(RequestState.Success(body))
            }

            override fun onFailure(errorCode: String, errorDesc: String) =
                continuation.resume(RequestState.Error("네이버를 통한 유저 프로필 조회에 실패하였습니다: $errorDesc"))
        }

        NidOAuth.getUserProfile(callback)
    }

