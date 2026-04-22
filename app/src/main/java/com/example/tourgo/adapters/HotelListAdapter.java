package com.example.tourgo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tourgo.R;
import com.example.tourgo.models.Hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelListAdapter extends RecyclerView.Adapter<HotelListAdapter.HotelViewHolder> {

    private final List<Hotel> originalList;
    private List<Hotel> filteredList;

    public HotelListAdapter(List<Hotel> list) {
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    public void filter(String query) {
        filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Hotel item : originalList) {
                if (item.getName().toLowerCase().contains(filterPattern) ||
                    item.getAddress().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hotel_list, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel item = filteredList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvLocation.setText(item.getAddress());
        holder.tvPrice.setText(String.format(Locale.US, "%.0f$", item.getPricePerNight()));
        holder.tvDescription.setText(item.getDescription());
        holder.tvRating.setText(String.format(Locale.US, "★ %.1f", item.getRating()));

        // Mặc định lúc đầu là tim trắng (Reset trạng thái khi tái sử dụng ViewHolder)
        holder.btnFavorite.setSelected(false);
        holder.btnFavorite.setColorFilter(ContextCompat.getColor(holder.btnFavorite.getContext(), android.R.color.white));

        holder.btnFavorite.setOnClickListener(v -> {
            // Đảo ngược trạng thái selected
            v.setSelected(!v.isSelected());
            
            if (v.isSelected()) {
                // Khi bấm vào: Đổi sang màu đỏ
                ((ImageView)v).setColorFilter(ContextCompat.getColor(v.getContext(), android.R.color.holo_red_dark));
            } else {
                // Khi bấm lần nữa: Quay lại màu trắng
                ((ImageView)v).setColorFilter(ContextCompat.getColor(v.getContext(), android.R.color.white));
            }
            
            // Hiệu ứng nảy (Animation)
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHotel, btnFavorite;
        TextView tvName, tvLocation, tvPrice, tvAmenities, tvRating, tvDescription;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            imgHotel = itemView.findViewById(R.id.imgHotelList);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            tvName = itemView.findViewById(R.id.tvHotelName);
            tvLocation = itemView.findViewById(R.id.tvHotelLocation);
            tvPrice = itemView.findViewById(R.id.tvHotelPrice);
            tvAmenities = itemView.findViewById(R.id.tvHotelAmenities);
            tvRating = itemView.findViewById(R.id.tvHotelRating);
            tvDescription = itemView.findViewById(R.id.tvHotelDescription);
        }
    }
}