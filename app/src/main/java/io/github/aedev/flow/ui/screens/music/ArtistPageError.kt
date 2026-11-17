package com.arubr.smsvcodes.ui.screens.music

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arubr.smsvcodes.R
import com.arubr.smsvcodes.ui.components.layout.topbar.FlowTopBar
import com.arubr.smsvcodes.ui.components.shared.FlowErrorState

/** The artist route when the page could not be loaded: the error with Retry, under a bar that leads back. */
@Composable
fun ArtistPageError(
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { FlowTopBar(title = "", onBack = onBackClick) },
    ) { padding ->
        FlowErrorState(
            error = stringResource(R.string.error_failed_to_load_artist),
            onRetry = onRetry,
            modifier = Modifier.padding(padding),
        )
    }
}
