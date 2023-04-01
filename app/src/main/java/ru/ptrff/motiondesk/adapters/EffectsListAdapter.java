package ru.ptrff.motiondesk.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.LayerItemBinding;
import ru.ptrff.motiondesk.engine.ActorHandler;
import ru.ptrff.motiondesk.engine.BaseEffect;
import ru.ptrff.motiondesk.view.ItemMoveCallback;

public class EffectsListAdapter extends RecyclerView.Adapter<EffectsListAdapter.LayerItemHolder> implements ItemMoveCallback.ItemTouchHelperAdapter {

    private final List<BaseEffect> effects;
    private final LayerListeners listeners;

    public EffectsListAdapter(List<BaseEffect> effects, LayerListeners listeners) {
        this.effects = effects;
        this.listeners = listeners;
    }

    @NonNull
    @Override
    public LayerItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayerItemBinding binding = LayerItemBinding.bind(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layer_item, parent, false));

        return new LayerItemHolder(binding);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull LayerItemHolder holder, int position) {
        holder.name.setText(effects.get(position).getName());
//        holder.root.setOnClickListener(view -> {
//            listeners.onLayerClick(objects.get(position));
//        });
//        holder.dragHandle.setOnTouchListener((v, event) -> {
//            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                listeners.onStartDrag(holder);
//            }
//            return true;
//        });
//        holder.lock.setOnClickListener(v -> {
//            if(objects.get(position).getLockStatus()){
//                holder.lock.setImageResource(R.drawable.ic_lock_open);
//                objects.get(position).setLockStatus(false);
//            }else{
//                holder.lock.setImageResource(R.drawable.ic_lock_closed);
//                objects.get(position).setLockStatus(true);
//            }
//        });
//        holder.visibility.setOnClickListener(v -> {
//            if(objects.get(position).getVisibility()){
//                holder.visibility.setImageResource(R.drawable.ic_eye_closed);
//                objects.get(position).setVisibility(false);
//            }else{
//                holder.visibility.setImageResource(R.drawable.ic_eye);
//                objects.get(position).setVisibility(true);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return effects.size();
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
//        if (fromPosition < toPosition) {
//            for (int i = fromPosition; i < toPosition; i++) {
//                Collections.swap(objects, i, i + 1);
//                Collections.swap(Arrays.asList(actorArray.items), i, i + 1);
//            }
//        } else {
//            for (int i = fromPosition; i > toPosition; i--) {
//                Collections.swap(objects, i, i - 1);
//                Collections.swap(Arrays.asList(actorArray.items), i, i - 1);
//            }
//        }
        //notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemMovingEnd() {

    }

    public static class LayerItemHolder extends RecyclerView.ViewHolder {
        TextView name;
        LinearLayout root;
        ImageView visibility;
        ImageView lock;
        ImageView dragHandle;

        public LayerItemHolder(LayerItemBinding binding) {
            super(binding.getRoot());
            this.name = binding.name;
            this.root = binding.layerRoot;
            this.lock = binding.lock;
            this.dragHandle = binding.dragHandle;
            this.visibility = binding.visibility;
        }

    }

    public interface LayerListeners {
        void onLayerClick(ActorHandler object);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
