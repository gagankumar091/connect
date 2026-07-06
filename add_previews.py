import os

files_to_update = {
    "welcome/WelcomeScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun WelcomeScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        WelcomeScreen(onGetStarted = {}, onSignIn = {})
    }
}""",
    "welcome/SignInScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun SignInScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        SignInScreen(onSignInSuccess = {}, onBack = {})
    }
}""",
    "welcome/SignUpScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun SignUpScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        SignUpScreen(onSignUpSuccess = {}, onBack = {})
    }
}""",
    "home/HomeScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun HomeScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        HomeScreen(
            onOpenProfile = {},
            onOpenCompany = {},
            onOpenChat = {},
            onOpenSmartSearch = {},
            onOpenBusinessCard = {},
            onOpenHealth = {}
        )
    }
}""",
    "businesscard/BusinessCardScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun BusinessCardScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        BusinessCardScreen(contactId = "preview", onBack = {})
    }
}""",
    "briefing/DailyBriefingScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun DailyBriefingScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        DailyBriefingScreen(onContinue = {})
    }
}""",
    "profile/ProfileScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun ProfileScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        ProfileScreen(
            contactId = "preview",
            onBack = {},
            onOpenTimeline = {},
            onOpenBusinessCard = {},
            onEditProfile = {}
        )
    }
}""",
    "profile/EditProfileScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun EditProfileScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        EditProfileScreen(onBack = {})
    }
}""",
    "aiassistant/AiAssistantScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun AiAssistantScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        AiAssistantScreen()
    }
}"""
}

base_dir = "/home/randomx/Videos/mitron/app/src/main/java/com/mitron/connect/ui/screens"

for file_path, preview_code in files_to_update.items():
    full_path = os.path.join(base_dir, file_path)
    if os.path.exists(full_path):
        with open(full_path, "r") as f:
            content = f.read()
            
        if "@androidx.compose.ui.tooling.preview.Preview" not in content:
            with open(full_path, "a") as f:
                f.write("\\n" + preview_code + "\\n")
            print(f"Added preview to {file_path}")
        else:
            print(f"Preview already exists in {file_path}")
