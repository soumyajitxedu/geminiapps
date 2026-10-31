# Study Tools Suite & Reliable Background Timer Architecture

A unified upgrade implementing a persistent Android Foreground Service for the Focus Timer, transforming Quizzes into a full 3-tab Study Tools suite (Interactive Quiz, 3D Flipping Flashcards, and Pinch-and-Pan Mind Maps), and hardening JSON ingestion against crashes.

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural decisions below synthesize your prompt and clarified preferences:

- **Confirmed: AI Prompt Chapter Selection**: Freeform chapter name input with quick-fill options so you can effortlessly customize the target chapter before copying the AI prompt.
- **Confirmed: Mind Map Layout Hierarchy**: Horizontal branching layout with the root node centered and child topics gracefully extending left and right, interconnected by smooth bezier curves.
- **Foreground Service Lifecycle**: Android 14+ compatible `FocusTimerService` declared with `FOREGROUND_SERVICE` permission and ongoing notification. When switching tabs or locking the screen (`ON_STOP`), the timer persists uninterrupted. The tree only wilts if you explicitly tap **Give Up** or swipe the app from recent tasks.

---

## 1. Overview & Core Concept

- **What It Does**:
  1. **Background Focus Engine**: The timer seamlessly runs in the background when navigating across all app tabs or locking the device screen, backed by a persistent Foreground Service.
  2. **Study Tools Suite**: Consolidates the former Quizzes screen into **Study Tools** featuring a Material 3 `TabRow`:
     - **Quiz**: The existing interactive ICSE chapter quiz player and JSON importer.
     - **Flashcards**: JSON-driven active recall cards with smooth 3D `rotationY` card-flip animations, progress counter, and Previous/Next controls.
     - **Mind Maps**: An infinite-canvas visual diagram engine with pan and pinch-to-zoom gestures (`detectTransformGestures`), rendering hierarchical concept trees horizontally.
  3. **Bulletproof Error Handling**: Any malformed, missing, or markdown-polluted JSON input is caught safely with a user-friendly Toast/Snackbar ("Invalid JSON format. Please try again") without app crashes.

- **Target Audience**: ICSE Class 10 students needing deep focus sessions and multidimensional active revision tools (testing, flashcards, concept mapping).

---

## 2. User Experience & Visual Design

### Key User Flows

```
[ Focus Timer Screen ] ───────────► Screen Lock / App Minimize
         │                                    │
         ▼                                    ▼
Starts Timer ──► Foreground Service Starts ──► Timer Continues Counting!
(Notification Active)                          (Tree Never Dies)
         │                                    │
         ▼                                    ▼
Switch to Study Tools Tab ◄───────────────────┘ (Notification Updates Live)
         │
         ├───► Tab 1: Quiz (MCQ evaluation, score tracking)
         ├───► Tab 2: Flashcards (Copy AI Prompt ➔ Paste JSON ➔ 3D Flip Cards)
         └───► Tab 3: Mind Map (Copy AI Prompt ➔ Paste JSON ➔ Pan/Zoom Concept Graph)
```

### Visual Identity & Theme
- **Theme Consistency**: Preserves Focus Grove's warm pixel-art nature aesthetic:
  - Deep Forest Green (`#2D5A27`), Sage Foliage (`#4E8752`), Earth Brown, Soft Cream cards.
  - Pixelated borders, crisp outlines, and retro badge styling.
- **Flashcard 3D Flip Motion**:
  - `animateFloatAsState` dynamically interpolates `rotationY` from `0f` to `180f`.
  - Content switches midway at `90f` between question (front) and answer (back) with camera distance correction (`graphicsLayer { cameraDistance = 12f * density }`) to eliminate distortion.
- **Mind Map Canvas Rendering**:
  - Root node: Elevated rounded rectangle in Forest Green with bold topic title.
  - Branch nodes: Level 1 and Level 2 chips with colored borders branching left and right.
  - Connectors: Clean anti-aliased cubic Bezier curves (`path.cubicTo`) linking parent nodes to child nodes.
  - Viewport Controls: Gesture-based pan & zoom plus Reset Zoom floating action button.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Timer Execution Model (Foreground Service vs In-Memory Coroutine)**
  - *Chosen Approach*: Lightweight Android `ForegroundService` (`FocusTimerService`) bound to the application context.
  - *Why*: Modern Android (API 31-36) aggressively kills background processes and throttles coroutines when the screen is locked or the app is minimized. A Foreground Service guarantees exact 1-second ticks, live lockscreen notification updates, and zero premature tree deaths.
  - *Alternatives Considered*: WorkManager (unsuitable for second-level real-time countdowns); Pure ViewModel coroutine (vulnerable to OS background memory trimming).

- **Decision 2: 3-Tab Study Tools Layout**
  - *Chosen Approach*: Top-level `PrimaryTabRow` inside `StudyToolsScreen` with persistent tab indices (`0: Quiz`, `1: Flashcards`, `2: Mind Map`).
  - *Why*: Intuitive navigation within a single screen destination without cluttering the bottom navigation bar.

- **Decision 3: Mind Map Graph Computation**
  - *Chosen Approach*: Recursive layout measurement calculating tree heights, node bounds, and horizontal branching offsets, cached in Compose state.
  - *Why*: Guarantees instant 60 FPS rendering without heavy third-party graph dependencies.

---

## 4. Technical Architecture & Data Strategy

### System & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                              MainActivity                              │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             FocusGroveApp                              │
│         LifecycleObserver: Retains timer on ON_STOP (No Kill)          │
└───────┬───────────────────────────┼────────────────────────────┬───────┘
        │                           │                            │
        ▼                           ▼                            ▼
┌───────────────────┐     ┌───────────────────┐     ┌────────────────────┐
│    TimerScreen    │     │ StudyToolsScreen  │     │  Forest / Syllabus │
└─────────┬─────────┘     └─────────┬─────────┘     └────────────────────┘
          │                         │
          │                         ├─── Tab 0: QuizView (Existing Engine)
          │                         ├─── Tab 1: FlashcardsView (3D Flip)
          │                         └─── Tab 2: MindMapView (Pan/Zoom Canvas)
          │
          ▼
┌────────────────────────────────────────────────────────────────────────┐
│                           MainViewModel                                │
│       State: remainingSeconds, timerStatus, flashcards, mindMap        │
└─────────┬─────────────────────────────────────────────────┬────────────┘
          │                                                 │
          ▼                                                 ▼
┌───────────────────┐                             ┌──────────────────────┐
│ FocusTimerService │                             │   FocusGroveDatabase │
│ Ongoing Lockscreen│                             │   (Room Persistence) │
│    Notification   │                             └──────────────────────┘
└───────────────────┘
```

### Data Models & JSON Schemas

1. **Flashcard Models**:
```kotlin
data class FlashcardItem(val front: String, val back: String)
data class FlashcardDeck(val chapter: String, val cards: List<FlashcardItem>)
```
*AI Prompt Schema*:
```json
{
  "chapter": "String",
  "cards": [
    { "front": "Question", "back": "Answer" }
  ]
}
```

2. **Mind Map Models**:
```kotlin
data class MindMapNode(
    val title: String,
    val children: List<MindMapNode> = emptyList()
)
data class MindMapData(
    val chapter: String,
    val root: MindMapNode
)
```
*AI Prompt Schema*:
```json
{
  "chapter": "String",
  "root": {
    "title": "Topic",
    "children": [
      { "title": "Sub", "children": [] }
    ]
  }
}
```

### Safety & Error Handling Strategy
- Ingestion methods (`loadFlashcardsFromJson`, `loadMindMapFromJson`) sanitize markdown code fences (```json ... ```) before parsing.
- Nested `try-catch` catches `JSONException`, syntax errors, or null fields, displaying an explicit Toast/Snackbar: *"Invalid JSON format. Please try again."*
- Zero app crashes or state corruption on bad input.
