package com.mitron.connect.data

import com.mitron.connect.data.model.AccentColor
import com.mitron.connect.data.model.BriefingItem
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.data.model.ChatPreview
import com.mitron.connect.data.model.Company
import com.mitron.connect.data.model.Contact
import com.mitron.connect.data.model.EventPerson
import com.mitron.connect.data.model.MeetingSummary
import com.mitron.connect.data.model.TimelineEvent
import com.mitron.connect.data.model.TimelineIcon

/** In-memory sample content mirroring the hifi wireframe's own placeholder data. */
object SampleData {

    val contacts = listOf(
        Contact(
            id = "aditi",
            initials = "AR",
            name = "Aditi Rao",
            title = "VP Partnerships",
            company = "Nimbus Robotics",
            email = "aditi.rao@nimbusrobotics.ai",
            phone = "+91 98450 11223",
            linkedin = "linkedin.com/in/aditirao",
            score = 82,
            daysSinceContact = 3,
            sharedInterests = listOf("healthcare AI"),
            mutualsCount = 3,
            nextAction = "Send the updated proposal before Friday's board update.",
            color = AccentColor.ACCENT,
        ),
        Contact(
            id = "karan",
            initials = "KS",
            name = "Karan Shah",
            title = "Investor",
            company = "ABC Robotics",
            email = "karan.shah@abcrobotics.com",
            phone = "+91 98200 33445",
            linkedin = "linkedin.com/in/karanshah",
            score = 78,
            daysSinceContact = 1,
            sharedInterests = listOf("robotics", "Series B"),
            mutualsCount = 5,
            nextAction = "Loop in the solutions engineer before the next demo.",
            color = AccentColor.NEUTRAL,
        ),
        Contact(
            id = "priya",
            initials = "PM",
            name = "Priya Menon",
            title = "Investor",
            company = "Northwind Capital",
            email = "priya.menon@northwindcap.com",
            phone = "+91 90000 12121",
            linkedin = "linkedin.com/in/priyamenon",
            score = 64,
            daysSinceContact = 12,
            sharedInterests = listOf("fintech"),
            mutualsCount = 2,
            nextAction = "Send festival wishes follow-up with a meeting ask.",
            color = AccentColor.SUCCESS,
        ),
    )

    val chats = listOf(
        ChatPreview("chat_aditi", "aditi", "AR", "Aditi Rao", "Nimbus Robotics", "Proposal follow-up sent", "91", AccentColor.ACCENT, overdue = true),
        ChatPreview("chat_karan", "karan", "KS", "Karan Shah", "ABC Robotics", "Demo completed, contract next", "78", AccentColor.NEUTRAL, overdue = false),
        ChatPreview("chat_priya", "priya", "PM", "Priya Menon", "Northwind Capital", "Festival wishes exchanged", "64", AccentColor.SUCCESS, overdue = false),
    )

    val companies = listOf(
        Company(
            id = "abc-robotics",
            name = "ABC Robotics",
            descriptor = "Robotics · Series B",
            funding = "$48M",
            employees = 210,
            openDeals = 2,
            contactsInside = "Karan Shah, Divya Nair +2 more",
            recentNews = "Closed Series B led by Northwind Capital.",
            color = AccentColor.NEUTRAL,
        ),
        Company(
            id = "nimbus-robotics",
            name = "Nimbus Robotics",
            descriptor = "Healthcare AI · Series A",
            funding = "$22M",
            employees = 85,
            openDeals = 1,
            contactsInside = "Aditi Rao +1 more",
            recentNews = "Piloting enterprise rollout for Q3.",
            color = AccentColor.ACCENT,
        ),
        Company(
            id = "northwind-capital",
            name = "Northwind Capital",
            descriptor = "Venture capital · fintech focus",
            funding = "$1.2B AUM",
            employees = 40,
            openDeals = 0,
            contactsInside = "Priya Menon",
            recentNews = "Announced a new $200M fund.",
            color = AccentColor.SUCCESS,
        ),
    )

    val eventPeople = listOf(
        EventPerson("rohan", "RS", "Rohan Seth", "Founder · SaaS, seed", AccentColor.ACCENT),
        EventPerson("meera", "MI", "Meera Iyer", "Investor · fintech, Series A", AccentColor.NEUTRAL),
        EventPerson("tarun", "TJ", "Tarun Joshi", "Recruiter · hiring engineers", AccentColor.SUCCESS),
    )

    val timeline = listOf(
        TimelineEvent(label = "Met at AWS Summit", icon = TimelineIcon.LOCATION, color = AccentColor.ACCENT),
        TimelineEvent(label = "Scanned QR", icon = TimelineIcon.QR, color = AccentColor.NEUTRAL),
        TimelineEvent(label = "Shared presentation", icon = TimelineIcon.FILE, color = AccentColor.ACCENT),
        TimelineEvent(label = "Coffee meeting", icon = TimelineIcon.COFFEE, color = AccentColor.SUCCESS),
        TimelineEvent(label = "Proposal sent", icon = TimelineIcon.SEND, color = AccentColor.ACCENT),
        TimelineEvent(label = "Demo completed", icon = TimelineIcon.CHECK, color = AccentColor.SUCCESS, isMeeting = true),
    )

    val meetingSummary = MeetingSummary(
        summary = "Discussed rollout timeline for the Q3 pilot and pricing for the enterprise tier.",
        painPoint = "onboarding time",
        budget = "$40-60k",
        actionItems = "Send proposal · Loop in solutions engineer",
    )

    val briefing = listOf(
        BriefingItem("5 important follow-ups today", AccentColor.DANGER),
        BriefingItem("Rahul's birthday tomorrow", AccentColor.ACCENT),
        BriefingItem("3 investors viewed your profile", AccentColor.PRO),
        BriefingItem("ABC Robotics raised funding", AccentColor.SUCCESS),
    )

    val suggestedSearches = listOf("founders in Bengaluru", "healthcare AI", "waiting for reply")

    val searchResults = listOf(
        EventPerson("karan", "KS", "Karan Shah", "ABC Robotics · investor", AccentColor.NEUTRAL),
        EventPerson("priya", "PM", "Priya Menon", "Northwind Capital · investor", AccentColor.SUCCESS),
    )

    val initialAiMessages = listOf(
        ChatMessage(text = "Who should I follow up with today?", fromUser = true),
        ChatMessage(
            text = "Aditi Rao (proposal pending) and 4 others need a reply this week.",
            fromUser = false,
            isAiLabeled = true,
        ),
        ChatMessage(text = "Draft a follow-up to Aditi", fromUser = true),
    )

    fun contactById(id: String): Contact? = contacts.find { it.id == id }
    fun companyById(id: String): Company? = companies.find { it.id == id }
}
