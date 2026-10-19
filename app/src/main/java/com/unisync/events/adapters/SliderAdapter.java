package com.unisync.events.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.unisync.events.R;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.VH> {

    private final List<String> images;

    public SliderAdapter(List<String> images) {
        this.images = images;
    }

    public static class VH extends RecyclerView.ViewHolder {
        final ImageView iv;

        VH(View v) {
            super(v);
            iv = v.findViewById(R.id.sliderImage);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Glide.with(holder.iv.getContext())
                .load(images.get(position))
                .placeholder(R.drawable.tag_bg)
                .error(R.drawable.tag_bg)
                .centerCrop()
                .into(holder.iv);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
