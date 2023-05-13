package ru.ptrff.motiondesk.adapters;

import android.content.Context;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.databinding.ListItemBinding;
import ru.ptrff.motiondesk.utils.ProjectManager;
import ru.ptrff.motiondesk.view.OnItemClickListener;

public class WpprsAdapter extends ListAdapter<WallpaperItem, WpprsAdapter.ViewHolder> {

    private final OnItemClickListener itemClickListener;
    private final Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public WpprsAdapter(OnItemClickListener itemClickListener, Context context) {
        super(new WallpaperItemDiffCallback());
        this.context = context;
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

        if (!item.hasPreviewImage()) {
            holder.binding.backgroundImage.setImageDrawable(context.getDrawable(R.drawable.no_image));
            holder.binding.shimmerView.setVisibility(View.GONE);
        } else {
            Picasso.get().load(ProjectManager.getPreviewById(context, item.getId())).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    holder.binding.backgroundImage.setImageBitmap(bitmap);
                    holder.binding.shimmerView.stopShimmerAnimation();
                    holder.binding.shimmerView.setVisibility(View.GONE);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    holder.binding.backgroundImage.setImageDrawable(context.getDrawable(R.drawable.no_image));
                    holder.binding.shimmerView.stopShimmerAnimation();
                    holder.binding.shimmerView.setVisibility(View.GONE);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    holder.binding.shimmerView.setVisibility(View.VISIBLE);
                    holder.binding.shimmerView.startShimmerAnimation();
                }
            });
        }


        holder.binding.name.setText(item.getName());
        holder.binding.number.setText(item.getDescription());
        holder.binding.allItem.setOnClickListener(view -> itemClickListener.onItemClick(getItem(position), position));
        holder.binding.stars.setText(String.valueOf(item.getStars()));
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
