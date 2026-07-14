import os

# ConnectComponents.kt
cc = "app/src/main/java/com/mitron/connect/ui/components/ConnectComponents.kt"
with open(cc, "r") as f:
    text = f.read()
text = text.replace("Icons.Filled.ChevronRight", "androidx.compose.material.icons.filled.KeyboardArrowRight")
with open(cc, "w") as f:
    f.write(text)

# ProfileScreen.kt
ps = "app/src/main/java/com/mitron/connect/ui/screens/profile/ProfileScreen.kt"
with open(ps, "r") as f:
    text = f.read()
if "import androidx.compose.ui.graphics.graphicsLayer" not in text:
    text = text.replace("package com.mitron.connect.ui.screens.profile", "package com.mitron.connect.ui.screens.profile\nimport androidx.compose.ui.graphics.graphicsLayer")
with open(ps, "w") as f:
    f.write(text)

# DailyBriefingScreen.kt
db = "app/src/main/java/com/mitron/connect/ui/screens/briefing/DailyBriefingScreen.kt"
with open(db, "r") as f:
    text = f.read()
text = text.replace("AnimatedVisibility(visible = true) {", "")
text = text.replace("onToggle = { viewModel.toggleCompleted(item.id) }\n                            )", "onToggle = { viewModel.toggleCompleted(item.id) }\n                            )\n")
# Need to remove the extra brace that was closing AnimatedVisibility
# We know it was right after the BriefingRow
text = text.replace("""                            )
                        }
                    }""", """                            )
                    }""")
with open(db, "w") as f:
    f.write(text)

# TimelineScreen.kt
ts = "app/src/main/java/com/mitron/connect/ui/screens/timeline/TimelineScreen.kt"
with open(ts, "r") as f:
    text = f.read()
text = text.replace("var selectedTab by remember { mutableStateOf(0) }", "var selectedTab by remember { mutableStateOf(0) }\n    val context = androidx.compose.ui.platform.LocalContext.current")
with open(ts, "w") as f:
    f.write(text)

# ConnectColorScheme.kt
color_s = "app/src/main/java/com/mitron/connect/ui/theme/ConnectColorScheme.kt"
if os.path.exists(color_s):
    os.remove(color_s)
