package com.shtainyky.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private final String TAG = "mLog";
    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    private int page = 0;
    private int column = 3;

    public static PhotoGalleryFragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder holder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                holder.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_galery_recycler_view);

        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int temp_width = display.getHeight() / 250;
                if (temp_width - 1 > 3) {
                    column = temp_width;
                }
                else {
                    temp_width = display.getHeight() / 250;
                    if (temp_width - 1 > 3)
                        column = temp_width;
                }


            }
        });
        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), column);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setupAdapter();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView,dx,dy);
                if (!recyclerView.canScrollVertically(1)) {
                    updateItems();
                    Log.i(TAG, "GOT PAGE on Scroll"+ page);
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i("mLog", "Background thread destroyed" );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView)searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getActivity(), query);
                newQuery();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
        MenuItem item = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity()))
            item.setTitle(R.string.stop_polling);
        else
            item.setTitle(R.string.start_polling);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                newQuery();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        Log.d(TAG, "UP query " + query);
        new FetchItemsTask(query).execute(++page);
        QueryPreferences.setPrefLastPage(getActivity(),page);
        Log.i(TAG, " GOT PAGE "+ page);
    }

    private void newQuery()
    {
        Intent intent = getActivity().getIntent();
        getActivity().finish();
        startActivity(intent);
        page = 0;
    }
    private void setupAdapter() {
        if (isAdded())
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));

    }

    private class PhotoHolder extends RecyclerView.ViewHolder
    {
        private ImageView mItemImageView;


        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView)itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>
    {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> mGalleryItems) {
            this.mGalleryItems = mGalleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
          LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Drawable drawable = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(drawable);
            mThumbnailDownloader.quequeThumbnail(holder, mGalleryItems.get(position).getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>>
    {
        private String mQuery;

         public FetchItemsTask(String query) {
            this.mQuery = query;
             Log.d(TAG, "doInBackground mQuery " + mQuery);
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... integers) {
            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos(integers[0]);
            } else {
                return new FlickrFetchr().searchPhotos(mQuery, integers[0]);


            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
//            mItems = items;
//            setupAdapter();
            Log.d(TAG, "onPostExecute mQuery " + mQuery);
            if(page > 1){
                mItems.addAll(items);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
            else{
                mItems = items;
                setupAdapter();
            }
        }
    }

}
