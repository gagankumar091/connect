package com.mitron.connect.data.model

import kotlinx.serialization.Serializable

/** Shared semantic color key used to tint avatars/tags across list items. */
@Serializable
enum class AccentColor { ACCENT, PRO, SUCCESS, DANGER, NEUTRAL }
