package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.Tour;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TourAdapter extends RecyclerView.Adapter<TourAdapter.TourViewHolder> {

    private final List<Tour> originalList;
    private List<Tour> filteredList;

    public TourAdapter(List<Tour> list) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public void filter(String query) {
        filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Tour item : originalList) {
                if (item.getName().toLowerCase().contains(filterPattern) || 
                    (item.getLocation() != null && item.getLocation().toLowerCase().contains(filterPattern))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TourViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tour, parent, false);
        return new TourViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourViewHolder holder, int position) {
        Tour item = filteredList.get(position);

        holder.imgTour.setImageResource(item.getImageResId());
        holder.tvName.setText(item.getName());
        holder.tvLocation.setText(item.getLocation());
        holder.tvPrice.setText(item.getPriceString());
        holder.tvDuration.setText(item.getDuration());
        holder.tvRating.setText(String.format(Locale.US, "★ %.1f", item.getRating()));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class TourViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTour;
        TextView tvName, tvLocation, tvPrice, tvRating, tvDuration;

        public TourViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTour = itemView.findViewById(R.id.imgTour);
            tvName = itemView.findViewById(R.id.tvTourName);
            tvLocation = itemView.findViewById(R.id.tvTourLocation);
            tvPrice = itemView.findViewById(R.id.tvTourPrice);
            tvRating = itemView.findViewById(R.id.tvTourRating);
            tvDuration = itemView.findViewById(R.id.tvTourDuration);
        }
    }
}