package ru.ptrff.motiondesk.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.data.WallpaperItem;
import ru.ptrff.motiondesk.databinding.ListItemBinding;
import ru.ptrff.motiondesk.view.OnItemClickListener;

public class WpprsAdapter extends ListAdapter<WallpaperItem, WpprsAdapter.ViewHolder> {

    private final OnItemClickListener itemClickListener;
    private final Activity activity;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public WpprsAdapter(OnItemClickListener itemClickListener, Activity activity) {
        super(new WallpaperItemDiffCallback());
        this.activity = activity;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WallpaperItem item = getItem(position);
        initViews(holder, position, item);
    }

    private void initViews(@NonNull ViewHolder holder, int position, WallpaperItem item) {
        String image = item.getImage();
        if (Objects.equals(image, ""))
            holder.binding.backrgoundImage.setImageDrawable(activity.getDrawable(R.drawable.selectable_rounded_foreground));
        else
            Picasso.get().load(image).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.binding.backrgoundImage.setImageBitmap(bitmap);
                    holder.binding.shimmerView.stopShimmerAnimation();
                    holder.binding.shimmerView.setVisibility(View.GONE);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    holder.binding.shimmerView.setVisibility(View.VISIBLE);
                    holder.binding.shimmerView.startShimmerAnimation();
                }
            });

        executor.execute(() -> {
            activity.runOnUiThread(() -> {
                holder.binding.name.setText(item.getName());
                holder.binding.number.setText(item.getDescription());
                holder.binding.allItem.setOnClickListener(view -> {
                    itemClickListener.onItemClick(getItem(position), position);
                });
                holder.binding.stars.setText(String.valueOf(item.getStars()));
                holder.binding.backrgoundImage.setVisibility(View.VISIBLE);
            });
        });
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemBinding binding;

        public ViewHolder(@NonNull ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public void submitList(@Nullable List<WallpaperItem> list, @Nullable Runnable commitCallback) {
        super.submitList(list != null ? new ArrayList<>(list) : null);
    }

    private static class WallpaperItemDiffCallback extends DiffUtil.ItemCallback<WallpaperItem> {
        @Override
        public boolean areItemsTheSame(@NonNull WallpaperItem oldItem, @NonNull WallpaperItem newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull WallpaperItem oldItem, @NonNull WallpaperItem newItem) {
            return oldItem.equals(newItem);
        }
    }
}
