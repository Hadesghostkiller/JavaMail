package vn.edu.hcmus.mail.config;

public class SupabaseConfig {
    public static String SUPABASE_URL = "https://swmipqjacpmyqdiparwf.supabase.co";
    public static String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN3bWlwcWphY3BteXFkaXBhcndmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzYzNDY2MTYsImV4cCI6MjA5MTkyMjYxNn0.Gzm2vzuDm6ji5HGuIwwpv-Cw_3NWrllIPVvOdtw_WFQ";
    

    private static boolean isEnabled = true;

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
