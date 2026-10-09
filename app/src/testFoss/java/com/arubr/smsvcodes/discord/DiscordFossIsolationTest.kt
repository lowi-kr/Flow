package com.arubr.smsvcodes.discord

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiscordFossIsolationTest {
    @Test
    fun `foss classpath excludes functional Discord implementation`() {
        val forbiddenClasses = listOf(
            "com.arubr.smsvcodes.discord.DiscordTokenStore",
            "com.arubr.smsvcodes.discord.DiscordAuthTokens",
            "com.arubr.smsvcodes.discord.DiscordPlaybackSource",
            "com.arubr.smsvcodes.discord.DiscordPresenceCoordinator",
            "com.arubr.smsvcodes.discord.KizzyDiscordPresenceTransport",
            "com.arubr.smsvcodes.discord.KizzyGatewayProtocol",
        )

        forbiddenClasses.forEach { className ->
            assertThat(runCatching { Class.forName(className) }.isFailure).isTrue()
        }
    }

    @Test
    fun `foss runtime reports Discord unavailable`() {
        assertThat(DiscordPresenceRuntime.settingsState.value.isAvailable).isFalse()
        assertThat(DiscordPresenceRuntime.settingsState.value.isEnabled).isFalse()
        assertThat(DiscordPresenceRuntime.settingsState.value.summary)
            .isEqualTo(DiscordSettingsSummary.UNAVAILABLE)
    }
}
