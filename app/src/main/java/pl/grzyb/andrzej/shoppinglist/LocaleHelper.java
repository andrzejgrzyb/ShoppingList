package pl.grzyb.andrzej.shoppinglist;

/**
 * Created by Andrzej on 09.08.2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Locale;

/**
 * http://gunhansancar.com/change-language-programmatically-in-android/
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 * <p/>
 * You can also change the locale of your application on the fly by using the setLocale method.
 * <p/>
 * Created by gunhansancar on 07/10/15.
 */
public class LocaleHelper {

    private static String LANGUAGE_KEY;

    public static void onCreate(Context context) {
        // Get Preference key for Language
        LANGUAGE_KEY = context.getResources().getString(R.string.pref_key_language);
        // Get default locale code (from phone's global configuration)
        String defaultLanguage = Resources.getSystem().getConfiguration().locale.getLanguage();
        // Get language from SharedPrefs
        String lang = getLanguage(context);

        if (lang.isEmpty()) {
            lang = defaultLanguage;
        }
        updateResources(context, lang);
        Log.d("LOCALE", context.getClass().toString() + " " + lang);
    }

    public static void onCreate(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static void setLocale(Context context, String language) {
        persist(context, language);
        updateResources(context, language);
    }

    // Returns language code from SharedPreferences
    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(LANGUAGE_KEY, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(LANGUAGE_KEY, language);
        editor.apply();
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}