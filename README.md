# Notes — AI-Powered Android Notes App

An offline-first Android notes application built with **Kotlin** and **Jetpack Compose**, featuring an on-device AI assistant powered by **Gemma** and **LiteRT-LM**.

The assistant can interact with the app through tools for creating, searching, and deleting notes. Other features include checklists, tags, folders, reminders, voice input, a home-screen widget, and adaptive navigation.

---

## Screenshots

### Adaptive layouts

Show the same screen at different window sizes to demonstrate responsive navigation.

| Phone | Foldable | Tablet |
|---|---|---|
| <img width="220" alt="notes_phone" src="https://github.com/user-attachments/assets/5c0a3c8f-1dd1-4234-8f27-6acbf45b47d3" /> | <img width="260" alt="notes_foldable" src="https://github.com/user-attachments/assets/b01a5faf-7ff9-413b-84b2-2dd0a8e0c6d8" /> | <img width="320" alt="notes_tablet" src="https://github.com/user-attachments/assets/db447ea6-b298-4102-b07d-86f2af7839ff" /> |

### AI assistant

| AI-created note Conversation | AI-searched note conversation|
|---|---|
| <img width="220" alt="notes_create" src="https://github.com/user-attachments/assets/2b24d6e7-fdcb-45fc-8422-cd803ad644a7" /> | <img width="220" alt="notes search" src="https://github.com/user-attachments/assets/c55b4359-328e-4498-b8fe-1925ba975f29" /> |

---

## 1. Overview

The project explores integrating an on-device language model into a practical Android application.

Rather than acting only as a chatbot, the assistant can request application actions through tools. Kotlin implementations validate and execute those actions using the same repositories as the rest of the app.

**Main features**
- Create, edit, organize, pin, favorite, archive, and search notes.
- Add checklists, tags, folders, and reminders.
- Use an on-device Gemma assistant to create, search, and delete notes.
- Provide voice input for assistant prompts.
- Display notes through an Android home-screen widget.
- Adapt navigation to different window sizes.

---

## 2. Technology stack

| Area | Technologies |
|---|---|
| Android UI | Kotlin, Jetpack Compose, Material 3 |
| Navigation | Navigation 3, Material 3 Adaptive |
| Dependency injection | Koin |
| Asynchronous state | Coroutines, Flow |
| Local persistence | Room, Preferences DataStore |
| AI | Gemma 4 E2B IT, LiteRT-LM |
| AndroidX A2UI | Renders agent-driven UI using custom note components and a component catalog. |
| Model download | Ktor Client, OkHttp |
| Voice | ML Kit GenAI Speech Recognition |
| Widget | Jetpack Glance |

---

## 3. Project structure

The project is organized into packages under `com.example.notes`.

```text
com.example.notes/
├── core/
│   ├── agent/
│   │   ├── a2ui/
│   │   ├── chat/
│   │   │   ├── data/
│   │   │   ├── domain/
│   │   │   └── tools/
│   │   ├── di/
│   │   └── speech/
│   ├── data/
│   ├── database/
│   ├── datastore/
│   ├── designsystem/
│   ├── model/
│   ├── navigation/
│   └── notification/
├── feature/
│   ├── agent/
│   ├── editor/
│   ├── folders/
│   ├── notes/
│   ├── reminders/
│   ├── search/
│   └── settings/
├── navigation/
└── ui/
```

**Core responsibilities**

| Package | Responsibility |
|---|---|
| `core.agent.a2ui` | Custom A2UI components, note UI definitions, and component catalog. |
| `core.agent.chat.domain` | AI contracts, tool definitions, arguments, and response models. |
| `core.agent.chat.data` | LiteRT-LM integration, model download, and tool adapter. |
| `core.agent.chat.tools` | Actions exposed to the AI assistant. |
| `core.agent.speech` | Voice-input and speech-recognition logic. |
| `core.database` | Room database, entities, and DAOs. |
| `core.data` | Repository interfaces and implementations. |
| `core.model` | Shared application models. |
| `core.notification` | Reminder scheduling and notifications. |
| `core.designsystem` | Reusable Compose components and themes. |
| `feature` | Feature-specific screens, ViewModels, and UI state. |

---

## 4. AI assistant architecture

The AI assistant is the main technical focus of the project. Gemma runs locally through LiteRT-LM, while application tools let it interact with the user's notes.

### Architecture

```text
                    User prompt
                         │
                         ▼
                   AgentViewModel
                         │
                         ▼
                     LlmEngine
                         │
                         ▼
                  LiteRtLlmEngine
                         │
                    Gemma Model
                         │
                  ┌──────┴──────┐
                  │             │
             Text response   Tool call
                                │
                                ▼
                         AgentToolAdapter
                                │
                                ▼
                        Application tools
                         ├── CreateNoteTool
                         ├── SearchNoteTool
                         └── DeleteNoteTool
                                │
                                ▼
                         NoteRepository
                                │
                                ▼
                              Room

                  Tool results returned
                         │
                         ▼
                    AgentViewModel
                         │
                         ▼
                    A2UI messages
                         │
                  ┌──────┴──────┐
                  │             │
             createSurface  updateDataModel
                  │             │
                  └──────┬──────┘
                         │
                   updateComponents
                         │
                         ▼
                  A2UI component catalog
                         │
                  ┌──────┴──────┐
                  │             │
               NoteCard   StripeNoteCard
```

### Local model and conversation

`LiteRtLlmEngine` implements the `LlmEngine` contract and manages the LiteRT-LM engine and conversation.

Its responsibilities include:
- Initializing and closing the model.
- Creating conversations with system instructions and registered tools.
- Sending prompts and processing model responses.
- Executing requested tools and returning their results to the model.

The model is downloaded to the app's private storage. `ModelDownloader` handles the download and exposes progress, while `ModelState` represents model status. The current engine configuration uses the CPU backend and a maximum token count of 4096.

### Tool calling

Tool calling is configured with `automaticToolCalling = false`, so the application manages the execution loop:

1. Send the prompt to Gemma.
2. Inspect the response for tool calls.
3. Match each call to an application tool.
4. Execute the tool and return its result to the conversation.
5. Continue until the model produces a normal response or the five-round limit is reached.

`AgentToolAdapter` converts the application's Kotlin tool definitions into JSON schemas understood by LiteRT-LM, including parameter types, required fields, nested objects, and arrays.

### Available tools

| Tool | Responsibility |
|---|---|
| `create_note` | Creates a note with content, color, checklist items, tags, and optional reminder data. |
| `search_note` | Searches existing notes through `NoteRepository`. |
| `delete_note` | Moves a note to the trash by title. |

`CreateNoteTool` validates the generated arguments before saving. The assistant's instructions distinguish descriptive content from actionable checklist items and tell it not to create notes during ordinary conversation.

`ToolArguments` centralizes conversion of raw tool arguments into application values and models, including colors, tags, checklist items, and reminders.

---

## 5. A2UI integration

The assistant uses **AndroidX A2UI (Agent-to-UI)** to present structured UI components instead of relying exclusively on text responses.

The `core.agent.a2ui` package contains custom note-related components and a component catalog used to render agent-generated UI.

When a tool returns data, `AgentViewModel` converts the result into A2UI messages and sends them to the UI layer.

The integration uses three main message types:

- `createSurface` — Creates a UI surface using the note assistant catalog.
- `updateDataModel` — Provides structured data that components can reference.
- `updateComponents` — Defines which components to render and supplies their properties.

**Custom components**

| Component | Purpose |
|---|---|
| `NoteCard` | Displays a newly created note with its content, color, tags, and checklist. |
| `StripeNoteCard` | Displays matching notes returned by a search operation. |

---

## 6. Voice input

The assistant includes a `VoiceInputManager` based on ML Kit GenAI Speech Recognition.

It checks feature availability, handles model download when needed, manages listening, and exposes transcription, partial results, readiness, and errors through `VoiceInputState`. The resulting text can be sent to the assistant.

---

## 7. Data and persistence

Room stores notes and their related data. Features and AI tools access note operations through repositories rather than calling DAOs directly.

```text
Notes UI ───────┐
                ├──► NoteRepository ───► Room
AI tools ───────┘
```

The main models include:
- `Note` — title, content, color, status flags, timestamps, folder, tags, checklist, and reminder.
- `ChecklistItem` — task text and completion state.
- `Tag` and `Folder` — note organization.
- `Reminder` — scheduled timestamp, repeat interval, and active state.

This shared repository layer allows AI actions and normal UI operations to use the same persistence logic.

---

## 8. Reminders and notifications

Reminders are associated with notes and scheduled through `AlarmManager`.

```text
Reminder saved
     │
     ▼
AlarmScheduler
     │
     ▼
AlarmManager
     │
     ▼
AlarmReceiver
     ├── Show notification
     ├── Deactivate one-time reminder
     └── Schedule next recurring occurrence
```

`BootReceiver` reschedules active reminders after device restart. The model supports once, daily, weekly, and monthly repeat intervals. Notifications can navigate to the related note.

---

## 9. Adaptive UI and navigation

The app uses Navigation 3 and Material 3 Adaptive Navigation Suite. `NotesApp` obtains window adaptive information and calculates the navigation presentation for the available window size.

Depending on the window, navigation can use a bottom bar or rail. The app also hides the navigation suite while the keyboard is visible.

---

## 10. Home-screen widget

The app includes a home-screen widget built with Jetpack Glance. Its widget UI is separate from the main Compose screens.

| Single Note | Notes List |
|---|---|
| <img width="220" alt="notes_widget_single" src="https://github.com/user-attachments/assets/79937ace-d60e-4bd1-989d-ef882e8c1be2" /> | <img width="220" alt="notes_widget_list" src="https://github.com/user-attachments/assets/1f83abf8-2156-48c9-b644-5d00e607a661" /> |

---

## 11. Screenshot assets
| Notes list | Notes Search | Notes Folders | Notes Settings |
|---|---|---|---|
| <img width="220" alt="notes_list_dark_Theme" src="https://github.com/user-attachments/assets/4a7075e7-800c-41a6-8987-24b927bd4f28" /> | <img width="220" alt="notes_search" src="https://github.com/user-attachments/assets/eeee1ff8-e220-41bd-a84a-eda09781c1f3" /> | <img width="220" alt="notes_folder" src="https://github.com/user-attachments/assets/58312662-fc1f-46e6-8792-f68abf99b86c" /> | <img width="220" alt="notes_settings" src="https://github.com/user-attachments/assets/d26cf818-ec28-489f-be87-52d321adf325" /> |

