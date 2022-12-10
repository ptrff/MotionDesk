package ru.ptrff.lwu.recycler_wpprs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import ru.ptrff.lwu.databinding.ListItemBinding;

public class WPPRSAdapter extends RecyclerView.Adapter<WPPRSAdapter.ViewHolder> {

    private final List<WPPRSListEntity> data; // данные для вывода в список
    private final LayoutInflater localInflater; // "раздуватель" с контекстом

    public WPPRSAdapter(Context context, List<WPPRSListEntity> data) {
        this.data = data;
        this.localInflater = LayoutInflater.from(context);
    }


    // Создание вьюхолдера из разметки
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ListItemBinding binding = ListItemBinding.inflate(localInflater, parent, false);
        return new ViewHolder(binding);
    }

    // Выставляет значения из списка данных во вьюхи по номеру элемента списка
    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {
        WPPRSListEntity item = data.get(position);
        holder.number.setText(item.getHeader());
        holder.name.setText(item.getDescription());

        AsyncLoadImages task = new AsyncLoadImages();
        try {
            holder.image.setImageBitmap(task.execute().get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Возвращает размер списка данных, нужно для внутренней работы ресайклера
    @Override
    public int getItemCount() {
        return data.size();
    }

    // Хранит переменные вьюх в разметке элементов списка
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView number;
        TextView name;
        ImageView image;

        public ViewHolder(@NonNull ListItemBinding binding) {
            super(binding.getRoot());
            number=binding.number;
            name= binding.name;
            image=binding.backrgoundImage;
        }
    }

    String[] names = {"cat1.jpg", "cat2.jpg", "big1.jpg", "4651.jpg", "enot1.jpg", "girl1.jpg"};

    private class AsyncLoadImages extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            File file = new File(
                    Environment.getExternalStorageDirectory().getAbsoluteFile(),
                    "Download/"+names[new Random().nextInt(names.length)]);
            try{
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }catch (NullPointerException e){
                Toast.makeText(localInflater.getContext(), "null", Toast.LENGTH_SHORT).show();
            }
            return bitmap;
        }
    }
}
