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

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.LayerItemBinding;
import ru.ptrff.motiondesk.engine.effects.BaseEffect;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.view.ItemMoveCallback;

public class EffectsListAdapter extends RecyclerView.Adapter<EffectsListAdapter.LayerItemHolder> implements ItemMoveCallback.ItemTouchHelperAdapter {

    private final ActorHandler actor;
    private final List<BaseEffect> effects;
    private final EffectListeners listeners;

    public EffectsListAdapter(ActorHandler actor, EffectListeners listeners) {
        this.actor = actor;
        this.effects = actor.getEffects();
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
        holder.position.setText(position+".");
        holder.name.setText(effects.get(position).getName());
        holder.root.setOnClickListener(view -> {
            listeners.onEffectClick(effects.get(position));
        });
        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                listeners.onStartDrag(holder);
            }
            return true;
        });
        holder.lock.setVisibility(View.GONE);

        holder.visibility.setImageResource(effects.get(position).isDisabled() ?
                R.drawable.ic_eye_closed : R.drawable.ic_eye);

        holder.visibility.setOnClickListener(v -> {
            if (!effects.get(position).isDisabled()) {
                holder.visibility.setImageResource(R.drawable.ic_eye_closed);
                effects.get(position).setDisabled(true);
            } else {
                holder.visibility.setImageResource(R.drawable.ic_eye);
                effects.get(position).setDisabled(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return effects.size();
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                actor.swapEffects(i, i + 1);
                notifyItemMoved(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                actor.swapEffects(i, i - 1);
                notifyItemMoved(i, i - 1);
            }
        }
    }

    @Override
    public void onItemMovingEnd() {
        notifyDataSetChanged();
    }

    public static class LayerItemHolder extends RecyclerView.ViewHolder {
        TextView position;
        TextView name;
        LinearLayout root;
        ImageView visibility;
        ImageView lock;
        ImageView dragHandle;

        public LayerItemHolder(LayerItemBinding binding) {
            super(binding.getRoot());
            this.position = binding.position;
            this.name = binding.name;
            this.root = binding.layerRoot;
            this.lock = binding.lock;
            this.dragHandle = binding.dragHandle;
            this.visibility = binding.visibility;
        }
    }

    public interface EffectListeners {
        void onEffectClick(BaseEffect effect);

        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
