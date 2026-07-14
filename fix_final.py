import os
import re

c_file = "app/src/main/java/com/mitron/connect/ui/components/ConnectComponents.kt"
with open(c_file, "r") as f:
    c_content = f.read()
c_content = re.sub(r"package com.mitron.connect.ui.components\n", "package com.mitron.connect.ui.components\nimport androidx.compose.material.icons.filled.Favorite\nimport androidx.compose.material.icons.filled.ChevronRight\n", c_content)
with open(c_file, "w") as f:
    f.write(c_content)

p_file = "app/src/main/java/com/mitron/connect/ui/screens/profile/ProfileScreen.kt"
with open(p_file, "r") as f:
    p_content = f.read()
p_content = p_content.replace(".androidx.compose.ui.graphics.graphicsLayer", ".graphicsLayer")
with open(p_file, "w") as f:
    f.write(p_content)

t_file = "app/src/main/java/com/mitron/connect/ui/screens/timeline/TimelineScreen.kt"
with open(t_file, "r") as f:
    t_content = f.read()
t_content = re.sub(r"TimelineScreen\([^)]*\)\s*\{", r"\g<0>\n    val context = androidx.compose.ui.platform.LocalContext.current", t_content)
with open(t_file, "w") as f:
    f.write(t_content)

h_file = "app/src/main/java/com/mitron/connect/ui/screens/home/HomeViewModel.kt"
with open(h_file, "r") as f:
    h_content = f.read()
h_content = h_content.replace("repository.getContacts(userLat, userLng)", "repository.getContacts()")
with open(h_file, "w") as f:
    f.write(h_content)

db_file = "app/src/main/java/com/mitron/connect/ui/screens/briefing/DailyBriefingScreen.kt"
with open(db_file, "r") as f:
    db_content = f.read()
db_content = db_content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.animation.*")
with open(db_file, "w") as f:
    f.write(db_content)

color_file = "app/src/main/java/com/mitron/connect/ui/theme/Color.kt"
with open(color_file, "r") as f:
    col_content = f.read()
if "data class ConnectColorScheme" in col_content:
    os.remove("app/src/main/java/com/mitron/connect/ui/theme/ConnectColorScheme.kt")

