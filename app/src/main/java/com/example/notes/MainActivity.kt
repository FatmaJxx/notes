package com.example.notes

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import com.example.notes.core.designsystem.theme.notes.NotesTheme
import com.example.notes.feature.editor.navigation.NoteEditorNavKey
import com.example.notes.ui.NotesApp
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.invoke


class MainActivity : ComponentActivity() {
    var noteId by mutableStateOf<Long?>(null)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        noteId = resolveIntent(intent)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NotesApp(noteId = noteId)
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        noteId = resolveIntent(intent)
    }

    private fun resolveIntent(intent: Intent?): Long? {
        if (intent == null) return null

        val matchResult = NOTE_EDITOR_MATCHER.match(
            DeepLinkRequest(intent)
        )

        return matchResult?.key?.noteId
    }
}

private val NOTE_EDITOR_MATCHER = UriDeepLinkMatcher(
    uriPattern = DeepLinkUri("mynotes://noteEditor/{noteId}"),
    serializer = NoteEditorNavKey.serializer()
)