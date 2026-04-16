# Afekoin — Campus Coin Wallet

An Android wallet app for Afeka College students. Earn, spend, and transfer a virtual campus currency (AFK) through mini-games and a peer-to-peer transfer system. Built with **Kotlin** and **Firebase**.

---

## Features

| Feature | Description |
|---|---|
| **Login / Sign-up** | Firebase Auth — email + password + custom username |
| **Remember me** | Saves email between sessions |
| **Forgot password** | Sends a reset email via Firebase |
| **Daily bonus** | +5 AFK once per calendar day on login |
| **Earn → Afequiz** | 15 random CS & Android trivia questions, +4 AFK for perfect score |
| **Earn → Afekliker** | Tap the logo for 10 seconds, up to +2 AFK |
| **Earn → Liebnitz** | Spaceship math runner, up to +5 AFK |
| **Class lock** | Games locked for first 45 min of class if user says they're in class |
| **College Store** | Spend AFK on Notebook (5), Coffee (3), Hoodie (25) |
| **P2P Transfer** | Send AFK to any user by username |
| **Transaction History** | Full ledger of every coin earned and spent |
| **Profile photo** | Upload from gallery, stored in Firebase Storage |

---

## How to Run

### Prerequisites
- Android Studio Hedgehog or newer
- Android device or emulator (API 26+)
- A Firebase project with **Email/Password** authentication enabled

### Steps

1. **Clone the repo**
   ```
   git clone https://github.com/Iliahashamen/mimshak_FINAL_afekoin.git
   ```

2. **Add your Firebase config**
   - Go to [Firebase Console](https://console.firebase.google.com) → your project → Project settings → Download `google-services.json`
   - Place the file at `app/google-services.json`
   - (A template is provided at `app/google-services.json.example`)

3. **Enable Firebase services** in the console:
   - Authentication → Email/Password
   - Firestore Database
   - Storage

4. **Open in Android Studio**, let Gradle sync, then **Run** on your device.

---

## Tech Stack

- **Language:** Kotlin 100%
- **UI:** XML layouts, Material Components, ConstraintLayout
- **Backend:** Firebase Auth, Firestore, Firebase Storage
- **Architecture:** Single-activity-per-screen, coroutines for async, object-based repositories
- **Animations:** Custom XML `anim/` transitions (slide-up, fade)
- **Sound:** Android `ToneGenerator` — no bundled audio files

---

## Project Structure

```
app/src/main/
├── java/com/example/mimshak_final_afekoin/
│   ├── firebase/          # FirebaseWallet, UserRepository, FirestorePaths
│   ├── data/              # Profile, LedgerEntry (data models)
│   ├── MainActivity        # Home screen
│   ├── LoginActivity       # Login + forgot password
│   ├── SignUpActivity      # Registration
│   ├── EarnActivity        # Game hub
│   ├── AfequizActivity     # Trivia game
│   ├── AfeklikerActivity   # Tap game
│   ├── LiebnitzActivity    # Space runner host
│   ├── LiebnitzGameView    # Custom game canvas view
│   ├── PayActivity         # Campus store
│   ├── TransferActivity    # P2P coin transfer
│   ├── HistoryActivity     # Transaction history
│   └── SoundFx             # Lightweight sound effects
└── res/
    ├── layout/             # All screen XMLs
    ├── anim/               # Transition animations
    ├── drawable/           # Shapes, gradients, icons
    └── values/             # Colors, strings, themes
```

---

*Final project — Mobile App Development, Afeka College of Engineering*
