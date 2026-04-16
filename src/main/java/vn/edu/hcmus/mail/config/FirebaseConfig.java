package vn.edu.hcmus.mail.config;

public class FirebaseConfig {
    public static final String DATABASE_URL = "https://YOUR-FIREBASE-PROJECT-default-rtdb.firebaseio.com";
    public static final String SERVICE_ACCOUNT_PATH = "config/serviceAccountKey.json";
    private static boolean isEnabled = false;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public static void setDatabaseUrl(String url) {
        if (url != null && !url.isEmpty() && !url.equals("https://YOUR-FIREBASE-PROJECT-default-rtdb.firebaseio.com")) {
            DATABASE_URL = url;
            isEnabled = true;
        }
    }
}
