package com.practicum.showandroid;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class SlideshowAdapter extends PagerAdapter {

    private Context context;
    private List<Uri> images;
    private long slideshowDelay;

    public SlideshowAdapter(Context context, List<Uri> images, long slideshowDelay) {
        this.context = context;
        this.images = images;
        this.slideshowDelay = slideshowDelay;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.slideshow_item, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);

        Uri imageUri = images.get(position);
        Glide.with(context).load(imageUri).into(imageView);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public void setImages(List<Uri> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public void setSlideshowDelay(long slideshowDelay) {
        this.slideshowDelay = slideshowDelay;
    }
}