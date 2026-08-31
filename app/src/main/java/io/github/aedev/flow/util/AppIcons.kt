package com.arubr.smsvcodes.util

object AppIcons {
    /** Component name prefix shared by every alias (the application id namespace). */
    const val NAMESPACE = "com.arubr.smsvcodes"

    /** The alias enabled by default in the manifest. Used as a safe fallback. */
    const val DEFAULT_SUFFIX = ".IconFlowRed"

    /** Every launcher alias suffix, in manifest declaration order. */
    val ALL_SUFFIXES = listOf(
        ".IconFlowRed",
        ".IconFlowLight",
        ".IconFlowPlay",
        ".IconAmoled",
        ".IconMonochrome",
        ".IconGhost",
        ".IconDynamic",
        ".IconMaterialSky",
        ".IconMaterialMint"
    )
}
