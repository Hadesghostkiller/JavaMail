package vn.edu.hcmus.mail.config;

public class SupabaseConfig {
    public static String SUPABASE_URL = "https://YOUR-PROJECT.supabase.co";
    public static String SUPABASE_ANON_KEY = "YOUR-ANON-KEY";
    public static String SUPABASE_SERVICE_KEY = "YOUR-SERVICE-KEY";

    private static boolean isEnabled = false;

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public static void configure(String url, String anonKey) {
        if (url != null && !url.isEmpty() && !url.equals("https://YOUR-PROJECT.supabase.co")) {
            SUPABASE_URL = url;
            SUPABASE_ANON_KEY = anonKey;
            isEnabled = true;
            System.out.println("[SUPABASE] Đã cấu hình: " + url);
        }
    }

    public static String getRestUrl() {
        return SUPABASE_URL + "/rest/v1";
    }
}
