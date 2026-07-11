package com.mitron.connect.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand ────────────────────────────────────────────────────────────────────
// Connect blue as the primary brand — modern and matches the home screen
val BrandBlue       = Color(0xFF2D31FA)   // Blue — CTAs, FABs, active states
val BrandBlueDeep   = Color(0xFF0900DB)   // Pressed / darker variant
val OnBrandBlue     = Color(0xFFFFFFFF)

// ─── Accent (links, chips, secondary highlights) ─────────────────────────────
val AccentTealLight    = Color(0xFF0B8F84)
val AccentTealDark     = Color(0xFF00A884)
val AccentTealBgLight  = Color(0xFFD7F5F0)
val AccentTealBgDark   = Color(0xFF0B2E2B)

// ─── AI / Pro (Ollama assistant tab, premium badges) ─────────────────────────
val AiIndigo       = Color(0xFF6C63FF)
val AiIndigoBgLight= Color(0xFFEEEDFF)
val AiIndigoBgDark = Color(0xFF1E1B45)

// ─── Semantic status ──────────────────────────────────────────────────────────
val SuccessGreen    = Color(0xFF25D366)   // WhatsApp read-tick green
val SuccessBgLight  = Color(0xFFD9F7E7)
val SuccessBgDark   = Color(0xFF0C2A1A)
val DangerRed       = Color(0xFFE53935)
val DangerBgLight   = Color(0xFFFFEAE9)
val DangerBgDark    = Color(0xFF3A0F0F)
val WarnAmber       = Color(0xFFFFB300)
val WarnBgLight     = Color(0xFFFFF3CD)
val WarnBgDark      = Color(0xFF2E2000)

// ─── Light neutrals ───────────────────────────────────────────────────────────
val SurfaceLight1      = Color(0xFFFFFFFF)
val SurfaceLight2      = Color(0xFFF0F2F5)   // Chat list background (WhatsApp grey)
val SurfaceLight3      = Color(0xFFE9EDEF)   // Pressed row / dividers
val ChatBubbleMeLight  = Color(0xFFD9FDD3)   // My message bubble (WhatsApp green-tint)
val ChatBubbleOtherLight = Color(0xFFFFFFFF) // Other's bubble
val TextPrimaryLight   = Color(0xFF111B21)   // WhatsApp near-black
val TextSecondaryLight = Color(0xFF54656F)   // Subtitle grey
val TextMutedLight     = Color(0xFF8696A0)   // Timestamp / hints
val BorderLight        = Color(0xFFE9EDEF)
val BorderStrongLight  = Color(0xFFD1D7DB)
val BorderStrongerLight= Color(0xFFB2BBBF)

// ─── Dark neutrals (OLED-first) ───────────────────────────────────────────────
val SurfaceDark1       = Color(0xFF111B21)   // WhatsApp dark main
val SurfaceDark2       = Color(0xFF1F2C34)   // Card / surface dark
val SurfaceDark3       = Color(0xFF2A3942)   // Elevated surfaces
val ChatBubbleMeDark   = Color(0xFF005C4B)   // My bubble dark
val ChatBubbleOtherDark= Color(0xFF1F2C34)   // Other's bubble dark
val TextPrimaryDark    = Color(0xFFE9EDEF)
val TextSecondaryDark  = Color(0xFF8696A0)
val TextMutedDark      = Color(0xFF667781)
val BorderDark         = Color(0xFF2A3942)
val BorderStrongDark   = Color(0xFF3B4A54)
val BorderStrongerDark = Color(0xFF4D5D67)
