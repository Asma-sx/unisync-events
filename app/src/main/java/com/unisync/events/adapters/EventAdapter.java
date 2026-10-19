package com.unisync.events.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.unisync.events.R;
import com.unisync.events.databinding.ItemEventBinding;
import com.unisync.events.models.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnEventAction {
        void onAction(Event event);
    }

    private List<Event> list;
    private final OnEventAction onClick;
    private final OnEventAction onEdit;
    private final OnEventAction onDelete;

    public EventAdapter(List<Event> list, OnEventAction onClick, OnEventAction onEdit, OnEventAction onDelete) {
        this.list = list;
        this.onClick = onClick;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public static class VH extends RecyclerView.ViewHolder {
        final ItemEventBinding binding;

        VH(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventBinding b = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Event e = list.get(position);
        ItemEventBinding b = holder.binding;

        b.tvTitle.setText(e.title);
        b.tvDate.setText(e.dateString);
        b.tvTag.setText(e.category);

        Glide.with(holder.itemView.getContext())
                .load(e.image)
                .placeholder(R.drawable.placeholder_unisync)
                .error(R.drawable.placeholder_unisync)
                .centerCrop()
                .into(b.ivEvent);

        b.viewForeground.setOnClickListener(v -> onClick.onAction(e));
        b.btnEdit.setOnClickListener(v -> onEdit.onAction(e));
        b.btnDelete.setOnClickListener(v -> onDelete.onAction(e));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<Event> newList) {
        list = newList;
        notifyDataSetChanged();
    }
}
