package ru.ptrff.motiondesk.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.badlogic.gdx.utils.Array;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.databinding.LayerItemBinding;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.view.ItemMoveCallback;

public class LayerListAdapter extends RecyclerView.Adapter<LayerListAdapter.LayerItemHolder> implements ItemMoveCallback.ItemTouchHelperAdapter {

    private final Array<ActorHandler> actorArray;
    private final LayerListeners listeners;

    public LayerListAdapter(Array<ActorHandler> actorArray, LayerListeners listeners) {
        this.actorArray = actorArray;
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
        holder.position.setText(position+".");
        holder.name.setText(actorArray.get(position).getName());
        holder.root.setOnClickListener(view -> {
            listeners.onLayerClick(getActorHandler(position));
        });
        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                listeners.onStartDrag(holder);
            }
            return true;
        });
        holder.lock.setOnClickListener(v -> {
            if (getActorHandler(position).getLockStatus()) {
                holder.lock.setImageResource(R.drawable.ic_lock_open);
                getActorHandler(position).setLockStatus(false);
            } else {
                holder.lock.setImageResource(R.drawable.ic_lock_closed);
                getActorHandler(position).setLockStatus(true);
            }
        });
        holder.visibility.setOnClickListener(v -> {
            if (getActorHandler(position).getVisibility()) {
                holder.visibility.setImageResource(R.drawable.ic_eye_closed);
                getActorHandler(position).setVisibility(false);
            } else {
                holder.visibility.setImageResource(R.drawable.ic_eye);
                getActorHandler(position).setVisibility(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return actorArray.size;
    }

    private ActorHandler getActorHandler(int id) {
        return actorArray.get(id);
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                actorArray.swap(i, i + 1);
                notifyItemMoved(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                actorArray.swap(i, i - 1);
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

    public interface LayerListeners {
        void onLayerClick(ActorHandler object);
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }
}
