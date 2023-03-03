package ru.ptrff.motiondesk.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.view.OnItemClickListener;
import ru.ptrff.motiondesk.view.OnItemLongClickListener;
import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.ListItemBinding;
import ru.ptrff.motiondesk.data.WallpaperItem;

public class WpprsAdapter extends RecyclerView.Adapter<WpprsAdapter.ViewHolder> {

    private final List<WallpaperItem> data; // данные для вывода в список
    private final Activity activity;
    private final OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private boolean longClickEnabled = true;


    public WpprsAdapter(List<WallpaperItem> data, OnItemClickListener itemClickListener, Activity activity) {
        this.activity = activity;
        this.data = data;
        this.itemClickListener = itemClickListener;
        this.longClickEnabled = false;
    }

    public WpprsAdapter(List<WallpaperItem> data, OnItemClickListener itemClickListener, OnItemLongClickListener itemLongClickListener, Activity activity) {
        this.activity = activity;
        this.data = data;
        this.itemClickListener = itemClickListener;
        this.itemLongClickListener = itemLongClickListener;
    }

    // Создание вьюхолдера из разметки
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // Выставляет значения из списка данных во вьюхи по номеру элемента списка
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WallpaperItem item = data.get(position);
        initViews(holder, position, item);
    }

    private void initViews(@NonNull ViewHolder holder, int position, WallpaperItem item) {
        Picasso.get().load(R.drawable.preview).into(new Target() {
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
                holder.binding.name.setText("WallpaperNamefdshfsdhfjshdfNamehgdhfsjgdsNameNameNameName");
                holder.binding.number.setText(item.getDescription()+"d");
                holder.binding.allItem.setOnClickListener(view -> {
                    itemClickListener.onItemClick(data.get(position), position);
                });
                if(longClickEnabled) {
                    holder.binding.allItem.setOnLongClickListener(view -> {
                        itemLongClickListener.onItemLongClick(data.get(position), position);
                        return true;
                    });
                }
                holder.binding.stars.setText(String.valueOf(item.getStars()));

                holder.binding.backrgoundImage.setVisibility(View.VISIBLE);
            });
        });
    }

    // Возвращает размер списка данных, нужно для внутренней работы ресайклера
    @Override
    public int getItemCount() {
        if(data!=null) return data.size();
        else return 0;
    }

    // Хранит переменные вьюх в разметке элементов списка
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemBinding binding;

        public ViewHolder(@NonNull ListItemBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
