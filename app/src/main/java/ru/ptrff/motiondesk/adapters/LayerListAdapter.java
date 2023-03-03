package ru.ptrff.motiondesk.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crashinvaders.vfx.scene2d.VfxWidgetGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.LayerItemBinding;
import ru.ptrff.motiondesk.engine.ImageActor;
import ru.ptrff.motiondesk.view.ItemMoveCallback;

public class LayerListAdapter extends RecyclerView.Adapter<LayerListAdapter.LayerItemHolder> implements ItemMoveCallback.ItemTouchHelperAdapter {

    private final VfxWidgetGroup objects;
    private final LayerListeners listeners;

    public LayerListAdapter(VfxWidgetGroup widgetGroup, LayerListeners listeners) {
        this.objects = widgetGroup;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public LayerItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ru.ptrff.motiondesk.databinding.LayerItemBinding binding = LayerItemBinding.bind(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layer_item, parent, false));

        return new LayerItemHolder(binding);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull LayerItemHolder holder, int position) {
        holder.name.setText(objects.getChild(position).getName());
        holder.root.setOnClickListener(view -> {
            listeners.onLayerClick((ImageActor) objects.getChild(position));
        });
        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                listeners.onStartDrag(holder);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return objects.getChildren().size;
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(Arrays.asList(objects.getChildren().begin()), i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(Arrays.asList(objects.getChildren().begin()), i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class LayerItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        LinearLayout root;
        ImageView visibility;
        ImageView dragHandle;

        public LayerItemHolder(LayerItemBinding binding) {
            super(binding.getRoot());
            this.name = binding.name;
            this.root = binding.layerRoot;
            this.dragHandle = binding.dragHandle;
            this.visibility = binding.visibility;
        }

        @Override
        public void onClick(View view) {

        }
    }

    public interface LayerListeners {
        void onLayerClick(ImageActor object);

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
