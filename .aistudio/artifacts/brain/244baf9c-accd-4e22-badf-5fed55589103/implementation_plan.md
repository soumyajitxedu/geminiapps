# Focus Grove - Gamified ICSE Study Tracker & Virtual Forest

Focus Grove is a gamified productivity and syllabus tracking native Android app designed for students (pre-loaded with ICSE Class 10 chapters). It pairs a strict focus countdown with a virtual tree ecosystem where focus grows trees and earns study coins, while app abandonment withers them permanently.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the planning phase:

- **Demo Testing Mode**: Confirmed. A quick test toggle (e.g., 10-second timer) will be available in the top bar to easily test tree growth, death animations, and coin rewards without waiting the full 30 minutes, alongside the default 30:00 focus block.
- **ICSE Exam Countdown & Practice Scores**: Confirmed. Prominent countdown banners ("81 days left for Pre-Board", "119 days left for ICSE 2027") and ICSE Specimen/PYQ score overview will be displayed on the Forest dashboard and Syllabus screen.
- **Tree Species in Shop**: Oak (default), Pine Tree (100 coins), Cherry Blossom (50 coins), and Bonsai (150 coins) with custom animated Compose illustrations. Ambient sounds (Rain, Lo-fi beats) can also be unlocked and played.
- **Local Persistence Strategy**: Room Database stores trees (status: alive, dead, species, timestamp, duration), coins, unlocked skins/audio, syllabus chapter completion, and exam targets.

---

## 1. Overview & Core Concept

- **What It Does**: Focus Grove transforms studying into an engaging conservation game. Users initiate a 30-minute study session that plants a seedling. If the session completes with the app remaining in the foreground, the tree flourishes into the user's permanent forest and awards 10 coins. Leaving or backgrounding the app (via Android `ON_STOP` lifecycle detection) immediately withers the tree into a grey dead stump in the forest.
- **Target Audience**: ICSE Class 10 board students (and focused learners) seeking to avoid digital distractions while conquering their syllabus.
- **Key Value**: Replaces mindless phone checking with loss aversion and tangible visual achievement.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard (The Forest)**:
   - Top Bar displays current Study Coin balance, Pre-board & ICSE 2027 countdown badges, and an Overall Syllabus Conquered progress indicator (e.g., `32% Syllabus Conquered`).
   - The Forest Grid (`LazyVerticalGrid`): Vibrant blooming trees and grey withered dead trees arranged side-by-side. Tapping a tree reveals session details (date, subject studied, outcome).
   - Filter/Sort chips: All trees, Vibrant only, Graveyard only.

2. **Focus Timer**:
   - Clean, distraction-free screen with a custom circular countdown progress indicator and central digital clock (`30:00`).
   - Subject tag selector (e.g., "Studying: Physics - Force").
   - Large prominent "Plant Seed & Start" button.
   - Once running: "Give Up" button, active tree species preview, and background ambient audio toggle (if unlocked).
   - Smooth animation on success: The seedling bursts into a blooming tree with confetti and a +10 Coins toast.
   - Screen desaturation / wilting animation on failure: If user gives up or backgrounds the app, the tree turns monochrome grey and withers.

3. **Syllabus Tracker**:
   - ICSE Class 10 accordion list by subject: Physics, Chemistry, Biology, Maths, History, Geography, Literature, L2, Computer Applications.
   - Real-time progress percentage per subject and overall.
   - Checkboxes for each individual chapter to mark completion, persisting instantly to Room.
   - PYQ Practice & Specimen score log table.

4. **Study Analysis & Shop**:
   - **Analytics Card**: Total focus hours/minutes, Survival rate (% alive vs dead), Trees grown vs killed, Subject distribution chart.
   - **Grove Shop**: Spend earned coins to unlock alternative tree species (Cherry Blossom, Pine Tree, Bonsai) and ambient focus audio (Rain, Lo-fi beats).
   - Active tree selector: Equip unlocked tree species for upcoming sessions.

### Visual Identity & Theme
- **Color Palette**: Forest Greens (`#1E3F20`, `#2E5A36`, `#4C8C5A`), Warm Wood Browns (`#6D4C41`, `#8D6E63`), Soft Cream/Parchment Backgrounds (`#F7F5EE`, Dark mode `#141A15`), and Vibrant Accents (`#E57373` for dead trees, `#F48FB1` for cherry blossoms, `#FFB300` for coins).
- **Typography**: Clean, friendly, and legible modern typography with distinct display styling.
- **Haptic & Visual Feedback**: Smooth scale and color transitions using Jetpack Compose animation APIs (`animateFloatAsState`, `animateColorAsState`, `AnimatedVisibility`).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Lifecycle-Based Distraction Detection (`LifecycleEventObserver`)**
  - *Chosen Approach*: Register an observer in the ViewModel / Activity listening for `Lifecycle.Event.ON_STOP`. If the timer is actively in the `RUNNING` state when `ON_STOP` fires, the session is failed immediately, the tree dies, and is committed to Room as a withered tree.
  - *Why*: Directly enforces the "do not leave the app" mechanic requested in the brief without needing invasive background services or root permissions.
- **Decision 2: Room Database Architecture**
  - *Chosen Approach*: Structured SQLite entities via Room with reactive `Flow` queries for `TreeEntity`, `ChapterProgressEntity`, `UserEconomyEntity`, and `ShopItemEntity`.
  - *Why*: 100% offline-first, instant reactivity with `collectAsStateWithLifecycle`, robust across device reboots and app updates.
- **Decision 3: Ambient Sound Generator**
  - *Chosen Approach*: Self-contained audio synthesizer / tone generator via Android `AudioTrack` producing calming brown/pink noise (rain simulation) and rhythmic chill lo-fi chords offline with zero external audio asset download issues.

---

## 4. Technical Architecture & Data Strategy

### System Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
│         (Edge-to-Edge, Theme, LifecycleOwner for Background Traps)     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                         ┌──────────┴──────────┐
                         │ Navigation Host /   │
                         │   M3 Scaffold Bar   │
                         └──────────┬──────────┘
        ┌───────────────────┬───────┴───────────┬──────────────────┐
        ▼                   ▼                   ▼                  ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐   ┌──────────────┐
│ The Forest   │    │ Focus Timer  │    │ Syllabus     │   │ Analytics &  │
│ (Dashboard)  │    │ Screen       │    │ Tracker      │   │ Shop Screen  │
└───────┬──────┘    └───────┬──────┘    └───────┬──────┘   └──────┬───────┘
        │                   │                   │                 │
        └───────────────────┴─────────┬─────────┴─────────────────┘
                                      ▼
                        ┌───────────────────────────┐
                        │      FocusGroveViewModel  │
                        │  (StateFlow, Timer Logic, │
                        │   Audio Player, Economy)  │
                        └─────────────┬─────────────┘
                                      ▼
                        ┌───────────────────────────┐
                        │    FocusGroveRepository   │
                        └─────────────┬─────────────┘
                                      ▼
                        ┌───────────────────────────┐
                        │       Room Database       │
                        │ ├─ TreeDao                │
                        │ ├─ SyllabusDao            │
                        │ └─ EconomyDao             │
                        └───────────────────────────┘
```

### Data Entities & Schema

1. **`TreeRecord`**:
   - `id: Long` (Primary Key auto-generated)
   - `species: String` (OAK, PINE, CHERRY_BLOSSOM, BONSAI)
   - `status: String` (ALIVE, DEAD)
   - `subjectTag: String` (e.g., "Physics", "Chemistry")
   - `durationSeconds: Int` (e.g., 1800)
   - `timestamp: Long`

2. **`ChapterProgress`**:
   - `id: String` (Primary Key: "Subject_ChapterName")
   - `subject: String`
   - `chapterName: String`
   - `isCompleted: Boolean`
   - `orderIndex: Int`

3. **`UserEconomy`**:
   - `id: Int = 1` (Single row)
   - `coins: Int` (Default 0, +10 on alive tree)
   - `selectedTreeSpecies: String`
   - `unlockedItemsJson: String` (Set of item IDs)
   - `demoModeEnabled: Boolean`

---

## Verification Plan

1. **Automated JVM Tests**: Unit test Room DAOs, Timer calculations, and Economy coin logic with Robolectric and Kotlin Coroutines Test.
2. **Interactive UI Verification**:
   - Test starting timer with Demo Mode and letting it finish -> verify green tree and +10 coins added.
   - Test "Give Up" button -> verify grey dead tree saved.
   - Test backgrounding the app (`ON_STOP`) during active timer -> verify tree dies.
   - Test syllabus checkbox toggling -> verify overall and per-subject progress bar updates.
   - Test Shop purchasing of Cherry Blossom, Pine, and ambient sound -> verify coin deduction and equipping.
3. **Compilation**: Verify build with `compile_applet`.
