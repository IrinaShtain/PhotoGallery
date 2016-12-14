package com.shtainyky.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "mLog";
    private static final String API_KEY = "0e5209b06d34034f366f044e3e370906";

    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InputStream in = connection.getInputStream();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    throw new IOException(connection.getResponseMessage() + " :with "+ urlSpec);
                }
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) > 0)
                {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                return out.toByteArray();
            }
            finally {
                connection.disconnect();
            }
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public  List<GalleryItem> fetchRecentPhotos(int page)
    {
        String url = buildUrl(FETCH_RECENTS_METHOD, null, page);
        return downloadGalleryItems(url);
    }

    public  List<GalleryItem> searchPhotos(String query, int page)
    {
        String url = buildUrl(SEARCH_METHOD, query, page);
        return downloadGalleryItems(url);
    }
    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        Log.e(TAG, "items"+ items.hashCode());
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: "+ jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            items = parseItems(jsonObject);

        }
        catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        }
        return items;
    }
    private String  buildUrl(String method, String query, int page)
    {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method" , method)
                .appendQueryParameter("page" , String.valueOf(page));
        if (method.equals(SEARCH_METHOD))
        {
            Log.i(TAG, "Got query: " + query);
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }


    private List<GalleryItem> parseItems(JSONObject jsonObject) throws IOException, JSONException
    {
        List<GalleryItem> tempItems;
        List<GalleryItem> items = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        JSONObject photosJsonObject = jsonObject.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        tempItems = Arrays.asList(gson.fromJson(photoJsonArray.toString(), GalleryItem[].class));
        for (int i = 0; i < tempItems.size(); i++)
        {
            if (tempItems.get(i).getUrl() != null) {
                items.add(tempItems.get(i));
            }
        }
        return items;

//        for (int i = 0; i < photoJsonArray.length(); i++)
//        {
//
//            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
//
//            GalleryItem item = new GalleryItem();
//            item.setId(photoJsonObject.getString("id"));
//            item.setCaption(photoJsonObject.getString("title"));
//            if (!photoJsonObject.has("url_s")) continue;
//            item.setUrl(photoJsonObject.getString("url_s"));
//            items.add(item);
//
//
//
//        }
    }

}
