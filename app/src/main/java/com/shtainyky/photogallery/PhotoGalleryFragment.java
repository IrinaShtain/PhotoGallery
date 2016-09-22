package com.shtainyky.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private final String TAG = "mLog";
    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private int page = 1;

    public static PhotoGalleryFragment newInstance()
    {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute(page++);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_photo_galery_recycler_view);
        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setupAdapter();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView,dx,dy);
                if (!recyclerView.canScrollVertically(1)) {
                    new FetchItemsTask().execute(page++);
                }
            }
        });
        return view;
    }

    private void setupAdapter() {
        if (isAdded())
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
    {
        private TextView mTitleTextView;


        public PhotoHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView)itemView;
        }
        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
            Log.i("mLog", "item.toString()= "+ item.toString());
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
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            holder.bindGalleryItem(mGalleryItems.get(position));
            Log.i("mLog", "pos= "+ position);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>>
    {
        @Override
        protected List<GalleryItem> doInBackground(Integer... integers) {
            return new FlickrFetchr().fetchItems(integers[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
//            mItems = items;
//            setupAdapter();
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
