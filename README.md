# Afekoin — Campus Coin Wallet

An Android wallet app for Afeka College students. Students earn a virtual campus currency (AFK) by playing mini-games, then spend it in a digital college store or transfer it to other students. Built entirely in **Kotlin** with **Firebase** as the backend.

---

## Features

| Feature | Description |
|---|---|
| **Login / Sign-up** | Firebase Auth — email + password, custom username chosen on registration |
| **Remember me** | Saves email between app sessions |
| **Forgot password** | Sends a reset email via Firebase |
| **Daily bonus** | +5 AFK once per calendar day on first login |
| **Earn → Afequiz** | 15 random CS & Android trivia questions, +1 AFK per correct answer |
| **Earn → Afekliker** | Tap the logo as fast as you can for 10 seconds, up to +2 AFK |
| **Earn → Liebnitz** | Spaceship math runner, up to +5 AFK per run |
| **Class lock** | Games lock for the first 45 minutes of class if the student says they're in class |
| **College Store** | Spend AFK on campus items (see store section below) |
| **P2P Transfer** | Send AFK to any other user by username |
| **Transaction History** | Full ledger of every coin earned and spent |
| **Profile photo** | Upload from gallery, stored in Firebase Storage |

---

## College Store

> **Note:** The AFK prices shown below are placeholders used for development and testing.
> In a real deployment, the college administration would decide the actual redemption values and prices for each item based on their own policies.

| Item | Dev price |
|---|---|
| Print Credit (20 pages) | 50 AFK |
| Coffee Voucher | 100 AFK |
| Campus Lunch | 200 AFK |
| USB Flash Drive (16 GB) | 800 AFK |
| Notebook | 1,300 AFK |
| Afeka T-Shirt | 2,500 AFK |
| Afeka Hoodie | 5,000 AFK |

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
   - A template is provided at `app/google-services.json.example`

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
- **Async:** Kotlin coroutines + `kotlinx-coroutines-play-services`
- **Image loading:** Coil
- **Sound:** Android `ToneGenerator` — no bundled audio files

---

## Project Structure

```
app/src/main/
├── java/com/example/mimshak_final_afekoin/
│   ├── firebase/           FirebaseWallet, UserRepository, FirestorePaths
│   ├── data/               Profile, LedgerEntry
│   ├── MainActivity        Home screen (balance, navigation)
│   ├── LoginActivity       Login + forgot password
│   ├── SignUpActivity      Registration (username + email + password)
│   ├── EarnActivity        Game selection hub
│   ├── AfequizActivity     Trivia quiz game
│   ├── AfeklikerActivity   Tap game
│   ├── LiebnitzActivity    Spaceship game host
│   ├── LiebnitzGameView    Custom Canvas View for the spaceship game
│   ├── PayActivity         College store
│   ├── TransferActivity    P2P coin transfer
│   ├── HistoryActivity     Transaction history list
│   ├── HistoryAdapter      RecyclerView adapter for history
│   └── SoundFx             ToneGenerator-based sound effects
└── res/
    ├── layout/             Screen layouts (one XML per Activity)
    ├── anim/               Slide + fade transitions
    ├── drawable/           Shapes, gradients, icons
    └── values/             Colors, strings, themes
```

---

*Final project — Mobile App Development, Afeka College of Engineering*
