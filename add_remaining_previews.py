import os

files_to_update = {
    "chats/ChatScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun ChatScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        ChatScreen(chatId = "preview", onBack = {})
    }
}""",
    "chats/ChatsTabContent.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun ChatsTabContentPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        ChatsTabContent(chats = emptyList(), onOpenChat = {}, onOpenSearch = {})
    }
}""",
    "companies/CompaniesTabContent.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun CompaniesTabContentPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        CompaniesTabContent(companies = emptyList(), onOpenCompany = {}, onOpenSearch = {})
    }
}""",
    "company/CompanyDetailScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun CompanyDetailScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        CompanyDetailScreen(companyId = "preview", onBack = {}, onOpenChat = {})
    }
}""",
    "events/EventsTabContent.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun EventsTabContentPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        EventsTabContent(events = emptyList(), onOpenSearch = {})
    }
}""",
    "health/HealthDashboardScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun HealthDashboardScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        HealthDashboardScreen(onBack = {})
    }
}""",
    "meetingsummary/MeetingSummaryScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun MeetingSummaryScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        MeetingSummaryScreen(contactId = "preview", onBack = {})
    }
}""",
    "people/PeopleTabContent.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun PeopleTabContentPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        PeopleTabContent(people = emptyList(), onOpenProfile = {}, onOpenSearch = {})
    }
}""",
    "search/SmartSearchScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun SmartSearchScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        SmartSearchScreen(onBack = {}, onOpenProfile = {})
    }
}""",
    "timeline/TimelineScreen.kt": """
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@androidx.compose.runtime.Composable
fun TimelineScreenPreview() {
    com.mitron.connect.ui.theme.ConnectAppTheme {
        TimelineScreen(contactId = "preview", onBack = {}, onOpenMeetingSummary = {})
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
            # Using real newlines, no escaped bug!
            with open(full_path, "a") as f:
                f.write("\n" + preview_code + "\n")
            print(f"Added preview to {file_path}")
        else:
            print(f"Preview already exists in {file_path}")
