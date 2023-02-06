package ru.ptrff.motiondesk.adapters;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ru.ptrff.motiondesk.data.BrowseSector;
import ru.ptrff.motiondesk.databinding.BrowseSectorBinding;

public class BrowseSectorAdapter extends RecyclerView.Adapter<BrowseSectorAdapter.ViewHolder> {
    private static final int RecyclerElementWidth = 140;
    private final List<BrowseSector> data;
    private WpprsAdapter adapter;
    private final Activity activity;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private LinearLayoutManager layoutManager;
    private final RecyclerView browseRecycler;


    public BrowseSectorAdapter(List<BrowseSector> data, Activity activity, RecyclerView browseRecycler) {
        this.data = data;
        this.activity = activity;
        this.browseRecycler=browseRecycler;
    }

    // Создание вьюхолдера из разметки
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BrowseSectorBinding binding = BrowseSectorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    // Выставляет значения из списка данных во вьюхи по номеру элемента списка
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BrowseSector item = data.get(position);
        initViews(holder, position, item);
    }

    private void initViews(@NonNull ViewHolder holder, int position, BrowseSector item) {
            activity.runOnUiThread(() -> {
                executor.execute(() -> {
                holder.binding.sectionName.setText("• "+item.getName() + (position + 1));

                holder.binding.overscroll.setOnOverScrollReleaseListener(() -> {
                    Toast.makeText(activity, "overscrolled " + (position + 1), Toast.LENGTH_SHORT).show();
                });

                adapter = new WpprsAdapter(item.getItemList(), null, activity);

                applyManagerToAdapter(adapter, holder.binding.recycler);
                holder.binding.recycler.post(() -> applyItemsSize(holder.binding.recycler));
            });
        });
    }

    private void applyItemsSize(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int itemCount = calculateColumnCount()+1;
                int parentWidth = ((View) recyclerView.getParent()).getWidth();
                if(parentWidth!=0) {
                    view.getLayoutParams().width = (parentWidth / itemCount)* ((GridLayoutManager) browseRecycler.getLayoutManager()).getSpanCount();
                }
            }
        });
    }
    private int calculateColumnCount(){
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density  = activity.getResources().getDisplayMetrics().density;
        float dpWidth  = outMetrics.widthPixels / density;
        return Math.round(dpWidth/RecyclerElementWidth);
    }

    private void applyManagerToAdapter(WpprsAdapter adapter, RecyclerView recyclerView) {
        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        //recyclerView.scrollToPosition(viewModel.getScrollPosition().getValue());
    }

    // Возвращает размер списка данных, нужно для внутренней работы рекуклера
    @Override
    public int getItemCount() {
        if(data!=null) return data.size();
        else return 0;
    }

    // Хранит переменные вьюх в разметке элементов списка
    public static class ViewHolder extends RecyclerView.ViewHolder {
        BrowseSectorBinding binding;

        public ViewHolder(@NonNull BrowseSectorBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
