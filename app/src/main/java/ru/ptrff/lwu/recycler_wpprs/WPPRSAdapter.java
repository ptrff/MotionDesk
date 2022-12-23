package ru.ptrff.lwu.recycler_wpprs;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.ptrff.lwu.OnItemClickListener;
import ru.ptrff.lwu.databinding.ListItemBinding;
import ru.ptrff.lwu.model.WallpaperItem;

public class WPPRSAdapter extends RecyclerView.Adapter<WPPRSAdapter.ViewHolder> {

    private List<WallpaperItem> data; // данные для вывода в список
    private OnItemClickListener listener;
    private DisplayMetrics metrics;

    public WPPRSAdapter(List<WallpaperItem> data, OnItemClickListener listener, DisplayMetrics metrics) {
        this.data = data;
        this.listener=listener;
        this.metrics=metrics;
    }

    // Создание вьюхолдера из разметки
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ListItemBinding binding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // Выставляет значения из списка данных во вьюхи по номеру элемента списка
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        WallpaperItem item = data.get(position);
        holder.binding.number.setText(item.getHeader());
        holder.binding.name.setText(item.getDescription());

        holder.binding.allItem.setOnClickListener(view -> {
            listener.onItemClick(data.get(position), position);
        });
        holder.binding.backrgoundImage.setImageBitmap(item.getImage());
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                140*((float) item.getImage().getWidth() / (float) item.getImage().getHeight()),
                metrics);
        holder.binding.backrgoundImage.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, px));
        holder.binding.rating.setText(item.getRating()+"");
//        WeatherItem item = items.get(position);
//        holder.binding.number.setText(item.dt_txt);
//        holder.binding.name.setText(item.weather.get(0).main);
//        holder.binding.rating.setText(Math.round((item.main.temp-273)*10)/10f+"");

    }

    // Возвращает размер списка данных, нужно для внутренней работы ресайклера
    @Override
    public int getItemCount() {
        return data.size();
    }

    // Хранит переменные вьюх в разметке элементов списка
    public class ViewHolder extends RecyclerView.ViewHolder {
        ListItemBinding binding;

        public ViewHolder(@NonNull ListItemBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
