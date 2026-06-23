package com.arubr.smsvcodes.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.innertube.YouTube
import com.arubr.smsvcodes.innertube.utils.parseCookieString
import kotlinx.coroutines.launch

/**
 * Lets the user paste a YouTube/YouTube Music cookie string (copied from
 * browser dev tools while logged in) so InnerTube requests can authenticate
 * via SAPISIDHASH. This is what unblocks/relaxes the ANDROID_VR bot-wall and
 * reduces mid-playback 403s, since authenticated requests are treated far
 * less aggressively by YouTube's anti-bot checks than fully anonymous ones.
 *
 * No network calls are made here — the cookie is only parsed locally to
 * validate it contains a SAPISID entry (required for SAPISIDHASH auth to
 * activate; see InnerTube.ytClient). Saving applies it immediately to the
 * live YouTube/InnerTube singleton in addition to persisting it for the next
 * app launch.
 */
@Composable
fun AccountLoginScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember { PlayerPreferences(context) }

    val savedCookie by preferences.loginCookie.collectAsState(initial = "")
    val savedLoginEnabled by preferences.loginEnabled.collectAsState(initial = false)

    var cookieText by remember(savedCookie) { mutableStateOf(savedCookie) }
    var showClearDialog by remember { mutableStateOf(false) }

    val parsedCookieMap = remember(cookieText) {
        if (cookieText.isBlank()) emptyMap() else parseCookieString(cookieText.trim())
    }
    val hasSapisid = "SAPISID" in parsedCookieMap
    val cookieLooksValid = cookieText.isBlank() || hasSapisid
    val isCurrentlyLoggedIn = savedLoginEnabled && savedCookie.isNotBlank()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back))
                    }
                    Text(
                        text = stringResource(R.string.account_login_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.account_login_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                SettingsGroup {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyLoggedIn) Icons.Outlined.CheckCircle else Icons.Outlined.Person,
                            contentDescription = null,
                            tint = if (isCurrentlyLoggedIn) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = if (isCurrentlyLoggedIn) {
                                stringResource(R.string.account_login_status_active)
                            } else {
                                stringResource(R.string.account_login_status_inactive)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            item {
                SettingsGroup {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.account_login_cookie_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = cookieText,
                            onValueChange = { cookieText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            placeholder = { Text(stringResource(R.string.account_login_cookie_placeholder)) },
                            isError = !cookieLooksValid,
                            singleLine = false
                        )

                        if (!cookieLooksValid) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = stringResource(R.string.account_login_missing_sapisid),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        } else if (hasSapisid) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.account_login_sapisid_found),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsGroup {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.account_login_howto),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.account_login_sign_out))
                }
            }

            item {
                Button(
                    onClick = {
                        val trimmed = cookieText.trim()
                        if (trimmed.isBlank()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.account_login_empty_error),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        if (!hasSapisid) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.account_login_missing_sapisid),
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            preferences.setLoginCookie(trimmed)
                            preferences.setLoginEnabled(true)
                            YouTube.cookie = trimmed
                            YouTube.useLoginForBrowse = true
                            Toast.makeText(
                                context,
                                context.getString(R.string.account_login_saved),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = cookieLooksValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.btn_save))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.account_login_sign_out)) },
            text = { Text(stringResource(R.string.account_login_sign_out_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        preferences.setLoginCookie("")
                        preferences.setLoginEnabled(false)
                        YouTube.cookie = null
                        YouTube.useLoginForBrowse = false
                        cookieText = ""
                        showClearDialog = false
                        Toast.makeText(
                            context,
                            context.getString(R.string.account_login_signed_out),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(stringResource(R.string.account_login_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}