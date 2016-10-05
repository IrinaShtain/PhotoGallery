package com.shtainyky.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String LAST_PAGE = "lastPage";

    public static String getStoredQuery(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_ID, null);
    }
    public static void setLastResultId(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply();
    }


    public static int getPrefLastPage(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(LAST_PAGE, 1);
    }
    public static void setPrefLastPage(Context context, int page)
    {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(LAST_PAGE, page)
                .apply();
    }
}
