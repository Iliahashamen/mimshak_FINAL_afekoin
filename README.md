# Afekoin — אפליקציית מטבע קמפוס (Android, Kotlin)

אפליקציה לדימוי מטבע פנים-ארגוני לאפקה: הרשמה והתחברות, ארנק, חנות (דמו), משחקים לזכייה במטבע, לוח תוצאות, העברות בין סטודנטים, והיסטוריית תנועות.

## דרישות המרצה — מיפוי לפרויקט

| דרישה | מימוש |
|--------|--------|
| **שני רכיבי שרת ומעלה** | **Firebase Authentication** (אימייל/סיסמה), **Cloud Firestore** (נתוני משתמשים ותנועות), **Firebase Storage** (תמונת פרופיל). |
| **העלאת תמונות** | במסך הראשי: לחיצה על תמונת הפרופיל → בחירת תמונה מהמכשיר → העלאה ל-`profile_images/{uid}.jpg` ועדכון `photoUrl` ב-Firestore. |
| **ניהול דאטה ב-Firebase** | אוספים `users`, `transactions`; עדכוני יתרה ותנועות דרך `FirebaseWallet` ו-`UserRepository`. |
| **שיתוף מידע בין משתמשים** | לוח תוצאות (קריאת כל ה-`users`), העברות (עדכון שני משתמשים + רישום תנועות לשני הצדדים). |

## ספריות עיקריות

- **Firebase BoM** — Auth, Firestore, Storage  
- **Kotlin Coroutines + kotlinx-coroutines-play-services** — קריאות אסינכרוניות ל-`Task`  
- **Coil** — טעינת תמונות פרופיל מ-URL  
- **Material / AppCompat** — UI  

## מבנה הקוד (פיזור ומחלקות)

- `data/` — מודלים: `Profile`, `LedgerEntry`  
- `firebase/` — `FirestorePaths`, `UserRepository` (פרופיל, לידרבורד, היסטוריה, העלאת תמונה), `FirebaseWallet` (זיכוי, חיוב, העברה)  
- מסכים בחבילה הראשית — Activities נפרדות לכל זרימה  

## הגדרת Firebase (חובה לפני הרצה)

1. צרו פרויקט ב-[Firebase Console](https://console.firebase.google.com/).
2. הוסיפו אפליקציית Android עם `applicationId`: `com.example.mimshak_final_afekoin`.
3. הורידו את **`google-services.json`** האמיתי והחליפו את הקובץ תחת `app/` (התבנית ב-repo אינה מחוברת לפרויקט אמיתי).
4. בקונסול: הפעילו **Authentication → Email/Password**, **Cloud Firestore**, **Storage**.
5. **כללי אבטחה (חשוב להעברות):**  
   עדכון יתרה של משתמש אחר בהעברה דורש כללים מתאימים. להדגמת קורס מומלץ כללים **מתיכנון מחדש לפרודקשן**:

```
// Firestore — דוגמה לסביבת הפגה / פרויקט סטודנטים בלבד
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    match /transactions/{id} {
      allow read, create: if request.auth != null;
    }
  }
}
```

```
// Storage — תמונות פרופיל
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile_images/{fileName} {
      allow read: if true;
      allow write: if request.auth != null && fileName == request.auth.uid + '.jpg';
    }
  }
}
```

בפרודקשן: להקשיח כללים ו/או להשתמש ב-Cloud Functions להעברות.

6. **אינדקס ב-Firestore:** אם מסך ההיסטוריה נופל עם שגיאת אינדקס, לחצו על הקישור בלוגקאט וצרו את האינדקס המורכב עבור `transactions`: שדה `userId` (Ascending) + `createdAt` (Descending).

## בנייה

```bash
./gradlew assembleDebug
```

## הגשה

- ריפו ציבורי ב-GitHub (לפי הנחיות הקורס).  
- סרטון/קישור לסרטון + README מעודכן — מומלץ לצרף צילומי מסך של המסכים העיקריים והסבר קצר על קשיים (למשל אינדקס Firestore, כללי אבטחה).

## קריסות אפשריות (להצגה בכיתה)

- **`google-services.json` חסר או לא תואם** — האפליקציה לא תתחבר לפרויקט.  
- **כללי Firestore/Storage חוסמים כתיבה** — זיכוי/חיוב/העברה/העלאת תמונה ייכשלו עם הודעת שגיאה בלוג.  
- **אינדקס חסר** — טעינת היסטוריית תנועות עלולה להיכשל עד יצירת האינדקס.
