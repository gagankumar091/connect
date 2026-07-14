package com.mitron.connect.ui.theme

import androidx.compose.ui.unit.dp

// Spacing scale adapted from Meetup's space_* dimens (4/8/12/16/24/32/40/80),
// snapped to a clean 4dp grid.
object Spacing {
    val space4 = 4.dp
    val space8 = 8.dp
    val space12 = 12.dp
    val space16 = 16.dp
    val space24 = 24.dp
    val space32 = 32.dp
    val space48 = 48.dp
    
    // Legacy mapping (To be removed after full UI refactor)
    val xxs = space4
    val xs = space8
    val sm = space12
    val md = space16
    val lg = space24
    val xl = space32
    val xxl = 40.dp // Legacy
    val xxxl = 64.dp // Legacy
}

// Avatar size scale adapted from Meetup's avatar_tiny/small/medium/large dimens.
object AvatarSize {
    val tiny = 26.dp
    val small = 36.dp
    val medium = 48.dp
    val large = 72.dp
}
