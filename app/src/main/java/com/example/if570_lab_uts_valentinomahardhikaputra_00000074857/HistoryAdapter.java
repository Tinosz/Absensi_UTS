package com.example.if570_lab_uts_valentinomahardhikaputra_00000074857;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<AttendanceEntry> entries;
    private final String TAG = "HistoryAdapter";

    public HistoryAdapter(List<AttendanceEntry> entries) {
        this.entries = entries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AttendanceEntry entry = entries.get(position);
        holder.dateTimeTextView.setText(String.format("Date: %s", entry.date));

        // Set the text for the "Absen Masuk" and "Absen Pulang" times
        holder.textViewMasukLabel.setText(String.format("Masuk at: %s", entry.timeIn));
        holder.textViewPulangLabel.setText(String.format("Pulang at: %s", entry.timeHome));

        Log.d(TAG, "Loading image for Absen Masuk: " + entry.imageUrlMasuk);
        Log.d(TAG, "Loading image for Absen Pulang: " + entry.imageUrlPulang);

        // Using Glide to load images
        Glide.with(holder.itemView.getContext())
                .load(entry.imageUrlMasuk)
                .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_broken_images))
                .into(holder.imageMasukView);

        Glide.with(holder.itemView.getContext())
                .load(entry.imageUrlPulang)
                .apply(new RequestOptions().placeholder(R.drawable.loading_animation).error(R.drawable.ic_broken_images))
                .into(holder.imagePulangView);
    }


    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTimeTextView;
        public TextView textViewMasukLabel; // TextView for displaying "Absen Masuk" time
        public TextView textViewPulangLabel; // TextView for displaying "Absen Pulang" time
        public ImageView imageMasukView;
        public ImageView imagePulangView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTimeTextView = itemView.findViewById(R.id.textView_dateTime);
            textViewMasukLabel = itemView.findViewById(R.id.textView_masuk_label);
            textViewPulangLabel = itemView.findViewById(R.id.textView_pulang_label);
            imageMasukView = itemView.findViewById(R.id.imageView_masuk);
            imagePulangView = itemView.findViewById(R.id.imageView_pulang);
        }
    }

}
