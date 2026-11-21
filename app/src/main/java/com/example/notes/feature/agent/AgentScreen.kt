package com.example.notes.feature.agent

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.R
import com.example.notes.core.designsystem.components.PulseAnimation
import org.koin.androidx.compose.koinViewModel

@Composable
fun AgentScreen(
    viewModel: AgentViewModel = koinViewModel(),
    navigatorToEditor: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AgentNavigationEvents.NavigateToEditor -> navigatorToEditor(event.noteId)
            }
        }
    }

    AgentScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(
    state: AgentUiState,
    onEvent: (AgentUiEvent) -> Unit
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            Log.d("VOICE", "is granted $isGranted")

            if (isGranted) {
                onEvent(AgentUiEvent.MicPressed)
            } else {
                Toast.makeText(
                    context,
                    "Please provide audio permission",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    )

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        if (!state.error.isNullOrEmpty())
            snackBarHostState.showSnackbar(message = state.error)
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.feature_agent_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
// Moving imePadding() to the agent Column: the whole column, list and field together, now shrinks by exactly the keyboard height.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(state.messages.asReversed()) { item ->
                    ChatMessageBubble(
                        message = item
                    )
                    Spacer(Modifier.size(16.dp))
                }
            }



            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 48f,
                                topEnd = 48f,
                                bottomStart = 0f,
                                bottomEnd = 48f
                            )
                        )
                        .background(Color.Gray)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    PulseAnimation()
                }
            }

            Spacer(Modifier.size(16.dp))

            if (state.isGemmaLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.feature_agent_gemma_loading))
                }
            } else if (state.isGemmaDownloading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.feature_agent_gemma_downloading))
                    Text(text = "${state.downloadedBytes}/${state.totalBytes}")
                }

            } else if (state.isGemmaReady && state.isGemmaDownloaded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = 50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = state.prompt,
                                onValueChange = { onEvent(AgentUiEvent.PromptChanged(it)) },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                decorationBox = { innerTextField ->
                                    if (state.prompt.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.feature_agent_text_field_placeholder),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.5f
                                                ),
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                    innerTextField()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                            )


                            IconButton(onClick = {
                                Log.d(
                                    "VOICE",
                                    "model is ready ${state.isVoiceReady} isListening ${state.isListening}"
                                )

                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                Log.d("VOICE", "has permission $hasPermission")


                                if (hasPermission) {
                                    onEvent(AgentUiEvent.MicPressed)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "send",
                                    tint = if (state.isListening) Color.Red
                                    else if (state.isVoiceReady) MaterialTheme.colorScheme.primary
                                    else {
                                        Color.Gray
                                    }
                                )
                            }
                        }
                    }

                    if (state.prompt.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onEvent(AgentUiEvent.PromptSend) },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else if (!state.isGemmaDownloaded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.feature_agent_gemma_is_not_downloaded))
                    TextButton(
                        onClick = { onEvent(AgentUiEvent.DownloadModel) },
                        content = {
                            Text(
                                stringResource(R.string.feature_agent_gemma_btn_action_download),
                                color = Color.Blue
                            )
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.feature_agent_gemma_is_not_loaded))
                    TextButton(
                        onClick = { onEvent(AgentUiEvent.LoadModel) },
                        content = {
                            Text(
                                stringResource(R.string.feature_agent_gemma_btn_action_load),
                                color = Color.Blue
                            )
                        }
                    )
                }
            }
        }
    }
}