import os
import re

theme_ext = "app/src/main/java/com/mitron/connect/ui/theme/ThemeExtensions.kt"
with open(theme_ext, "r") as f:
    content = f.read()

# Add missing imports
imports = """
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.*
"""
content = re.sub(r"import kotlin.math.sin\n", f"import kotlin.math.sin\n{imports}", content)

# Rename conflicting objects
content = content.replace("object Spacing {", "object ThemeSpacing {")
content = content.replace("Spacing.", "ThemeSpacing.")
content = content.replace("object Shapes {", "object ThemeShapes {")
content = content.replace("Shapes.", "ThemeShapes.")

# Fix unresolved 'card' / 'button' in Elevation / Modifier
content = content.replace("Elevation.card()", "Elevation.elevatedCard()")
content = content.replace("Shapes.button", "ThemeShapes.md")
content = content.replace("Shapes.card", "ThemeShapes.xl")

with open(theme_ext, "w") as f:
    f.write(content)

components = "app/src/main/java/com/mitron/connect/ui/components/ConnectComponents.kt"
with open(components, "r") as f:
    c_content = f.read()

c_imports = """
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
"""
c_content = re.sub(r"package com.mitron.connect.ui.components\n", f"package com.mitron.connect.ui.components\n{c_imports}", c_content)
with open(components, "w") as f:
    f.write(c_content)

home_screen = "app/src/main/java/com/mitron/connect/ui/screens/home/HomeScreen.kt"
with open(home_screen, "r") as f:
    h_content = f.read()
h_content = re.sub(r"shape = .*,", "", h_content)
h_content = re.sub(r"colors = .*,", "", h_content)
h_content = re.sub(r"elevation = .*", "", h_content)
with open(home_screen, "w") as f:
    f.write(h_content)

home_feed = "app/src/main/java/com/mitron/connect/ui/screens/home/HomeFeedContent.kt"
with open(home_feed, "r") as f:
    hf_content = f.read()
hf_content = re.sub(r"shape = .*,", "", hf_content)
hf_content = re.sub(r"colors = .*,", "", hf_content)
hf_content = re.sub(r"border = .*", "", hf_content)
with open(home_feed, "w") as f:
    f.write(hf_content)

