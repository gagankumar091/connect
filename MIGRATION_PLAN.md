# Mitron Connect - Complete UI Migration & Redesign Plan

## 🎯 Goal
Migrate all screens to new **Indigo/Violet SaaS Theme** with Material 3 + custom tokens, redesign every screen with modern patterns (glassmorphism, gradients, fluid animations, micro-interactions), and fix all feature bugs.

---

## 📋 Phase 1: Theme Infrastructure (Foundation)

### 1.1 Extend ConnectColorScheme with Modern Tokens
Add to `ConnectColorScheme.kt`:
- `glassSurface`, `glassBorder` - for glassmorphism cards
- `gradientPrimary`, `gradientSecondary` - for gradient backgrounds
- `elevation1`..`elevation5` - shadow tokens
- `scrim` - modal overlay
- `avatarBg[1..6]` - diverse avatar backgrounds
- `statusOnline`, `statusBusy`, `statusAway` - presence indicators

### 1.2 Create Theme Extensions (`ThemeExtensions.kt`)
```kotlin
// Glassmorphism modifiers
Modifier.glass(elevation: GlassElevation = GlassElevation.Medium)
Modifier.gradientBorder(colors: List<Color>, width: Dp = 1.dp)

// Semantic color accessors
MaterialTheme.colorScheme.connect.primaryGradient
ConnectTheme.colors.glassSurface

// Spacing shortcuts
val Spacing.xs = space4, .sm = space8, .md = space16, .lg = space24, .xl = space32, .xxl = space48

// Typography shortcuts
val Typography.displayLarge, .headlineLarge, .titleLarge, .bodyLarge, .labelLarge, etc.
```

### 1.3 Update ConnectComponents.kt
- Refactor all components to use theme tokens only
- Add new components: `GlassCard`, `GradientButton`, `AvatarGroup`, `StatusBadge`, `ShimmerLoader`, `EmptyState`, `SectionHeader`

---

## 📋 Phase 2: Screen Migration Order (Priority → Impact)

### Tier 1: Core Navigation & High-Traffic (Week 1)
| # | Screen | File | Est. Effort |
|---|--------|------|-------------|
| 1 | **MainActivity / Nav Host** | `MainActivity.kt` | 2h |
| 2 | **Home Screen** | `HomeScreen.kt` + `HomeFeedContent.kt` | 4h |
| 3 | **Chats Tab** | `ChatsTabContent.kt` | 3h |
| 4 | **Profile Screen** | `ProfileScreen.kt` | 3h |
| 5 | **People/Network Tab** | `PeopleTabContent.kt` | 3h |

### Tier 2: Feature Screens (Week 2)
| # | Screen | File | Est. Effort |
|---|--------|------|-------------|
| 6 | **Events Tab** | `EventsTabContent.kt` | 3h |
| 7 | **Event Detail** | `EventDetailScreen.kt` | 2h |
| 8 | **Smart Search** | `SmartSearchScreen.kt` | 3h |
| 9 | **Chat Screen** | `ChatScreen.kt` | 4h |
| 10 | **Timeline** | `TimelineScreen.kt` | 3h |
| 11 | **Business Card** | `BusinessCardScreen.kt` | 2h |
| 12 | **Settings/Edit Profile** | `EditProfileScreen.kt` | 2h |

### Tier 3: Specialized Screens (Week 3)
| # | Screen | File | Est. Effort |
|---|--------|------|-------------|
| 13 | **Welcome/Onboarding** | `WelcomeScreen.kt`, `SignInScreen.kt`, `SignUpScreen.kt` | 3h |
| 14 | **Calls** | `CallsListScreen.kt`, `CallScreens.kt` | 3h |
| 15 | **Scanner** | `ScannerScreen.kt` | 2h |
| 16 | **AI Assistant** | `AiAssistantScreen.kt` | 2h |
| 17 | **Health Dashboard** | `HealthDashboardScreen.kt` | 2h |
| 18 | **Notifications** | `NotificationsScreen.kt` | 2h |
| 19 | **Relationship Score** | `RelationshipScoreScreen.kt` | 2h |
| 20 | **Meeting Summary** | `MeetingSummaryScreen.kt` | 1h |
| 21 | **Daily Briefing** | `DailyBriefingScreen.kt` | 1h |
| 22 | **Company Detail** | `CompanyDetailScreen.kt` | 1h |

---

## 🎨 Phase 3: Redesign Patterns per Screen Type

### List/Feed Screens (Home, Chats, People, Events, Search)
- **Glassmorphism cards** with subtle borders
- **Staggered entrance animations** (0.3s spring)
- **Pull-to-refresh** with custom indicator
- **Empty states** with illustration + CTA
- **Shimmer loading** placeholders

### Detail Screens (Profile, Event Detail, Chat, Timeline)
- **Hero header** with parallax scroll
- **Glass app bar** that materializes on scroll
- **Sectioned layout** with `SectionHeader` component
- **Action bars** that float/anchor

### Modal/Bottom Sheet Screens (Scanner, Quick Actions)
- **Rounded top corners** (28dp)
- **Drag handle** with haptic feedback
- **Backdrop blur** (if API 31+)

### Onboarding/Welcome
- **Full-screen hero** with animated gradient
- **Page indicator** with spring animation
- **Primary CTA** with gradient + glow

---

## 🔧 Phase 4: Component Library (Reusable)

| Component | Purpose | Used In |
|-----------|---------|---------|
| `GlassCard` | Frosted glass container | All screens |
| `GradientCard` | Branded gradient background | Home, Profile |
| `Avatar` + `AvatarGroup` | Consistent avatars | Everywhere |
| `StatusBadge` | Online/busy/away/offline | Chats, Profile, People |
| `SectionHeader` | Title + action + count | All lists |
| `EmptyState` | Illustration + message + CTA | All empty lists |
| `ShimmerPlaceholder` | Loading skeleton | All async loads |
| `FloatingActionButton` | Primary actions | Home, Chats |
| `BottomSheet` | Modals | Scanner, Filters |
| `AnimatedCounter` | Number animations | Health, Score |
| `ProgressRing` | Circular progress | Health, Profile |
| `Chip` / `FilterChip` | Tabs, filters | Chats, Events, Search |

---

## 🐛 Phase 5: Feature Fixes & Polish

### Known Issues to Fix
- [ ] **Agora Call UI** - Incoming call banner overlaps bottom nav
- [ ] **Chat messages** - Date headers, read receipts, reactions
- [ ] **Notifications** - Deep linking, grouping, mark all read
- [ ] **Search** - Debounce, recent searches, filters
- [ ] **Profile** - Image picker, validation, offline sync
- [ ] **Scanner** - Torch, gallery import, history
- [ ] **Timeline** - Infinite scroll, pull refresh, grouping
- [ ] **Events** - RSVP sync, calendar integration, reminders
- [ ] **Calls** - Call log, duration, recording indicator
- [ ] **Dark mode** - Full support, dynamic color (Android 12+)

### Animation Polish
- [ ] Shared element transitions (Profile ↔ Business Card)
- [ ] Staggered list animations
- [ ] Micro-interactions (button press, chip select, toggle)
- [ ] Page transitions (slide + fade)
- [ ] Loading choreography

---

## 📦 Deliverables Checklist

- [ ] `ThemeExtensions.kt` - All utility functions
- [ ] `ConnectComponents.kt` - 15+ reusable components
- [ ] 22 screens fully migrated + redesigned
- [ ] `MainActivity.kt` - Clean nav host + bottom bar
- [ ] Dark mode verified on all screens
- [ ] Dynamic color (Material You) working
- [ ] Accessibility: content descriptions, semantics, touch targets ≥48dp
- [ ] Performance: no jank, <16ms frames, images cached

---

## 🚀 Start Order (Execution)

1. **ThemeExtensions.kt** + **ConnectComponents.kt** (Foundation)
2. **MainActivity.kt** (Navigation shell)
3. **HomeScreen.kt** → **HomeFeedContent.kt** (Landing experience)
4. **ChatsTabContent.kt** (Core feature)
5. **ProfileScreen.kt** (Identity center)
6. **PeopleTabContent.kt** (Growth feature)
7. ... continue down Tier 1 → Tier 2 → Tier 3

---

## 📐 Design Token Mapping Reference

| Old Hardcoded | New Theme Token |
|---------------|-----------------|
| `Color(0xFFF6F2FA)` | `MaterialTheme.colorScheme.surfaceContainerLowest` |
| `Color(0xFF4343D5)` | `MaterialTheme.colorScheme.primary` |
| `Color(0xFF5D5FEF)` | `MaterialTheme.colorScheme.primaryContainer` |
| `Color(0xFFEEEAF4)` | `MaterialTheme.colorScheme.surfaceContainerLow` |
| `Color(0xFF1B1B20)` | `MaterialTheme.colorScheme.onSurface` |
| `Color(0xFF464555)` | `MaterialTheme.colorScheme.onSurfaceVariant` |
| `Color(0xFF767586)` | `MaterialTheme.colorScheme.outline` |
| `Color(0xFFC7C4D7)` | `MaterialTheme.colorScheme.outlineVariant` |
| `Color(0xFF10B981)` | `ConnectTheme.colors.success` |
| `Color(0xFFF59E0B)` | `ConnectTheme.colors.warn` |
| `Color(0xFFEF4444)` | `ConnectTheme.colors.danger` |
| `20.sp, FontWeight.Bold` | `MaterialTheme.typography.headlineMedium` |
| `16.sp, FontWeight.SemiBold` | `MaterialTheme.typography.titleLarge` |
| `14.sp, FontWeight.Normal` | `MaterialTheme.typography.bodyLarge` |
| `12.sp, FontWeight.Medium` | `MaterialTheme.typography.labelLarge` |
| `20.dp` padding | `Spacing.md` (space16) |
| `24.dp` padding | `Spacing.lg` (space24) |
| `16.dp` gap | `Spacing.md` (space16) |