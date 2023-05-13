package ru.ptrff.motiondesk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.ToolItem;
import ru.ptrff.motiondesk.databinding.ToolItemBinding;

public class ToolbarAdapter extends ListAdapter<ToolItem, ToolbarAdapter.ToolItemHolder> {

    private final OnImageClickListener listener;

    public ToolbarAdapter(OnImageClickListener listener) {
        super(new DiffUtil.ItemCallback<ToolItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ToolItem oldItem, @NonNull ToolItem newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ToolItem oldItem, @NonNull ToolItem newItem) {
                return false;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToolItemHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tool_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToolItemHolder holder, int position) {
        ToolItem tool = getItem(position);
        holder.bind(tool);
    }

    public class ToolItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView icon;
        private final LinearLayout allItem;
        private final TextView label;

        public ToolItemHolder(@NonNull View itemView) {
            super(itemView);
            ToolItemBinding binding = ToolItemBinding.bind(itemView);
            icon = binding.icon;
            allItem = binding.allItem;
            label = binding.label;
            allItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onImageClick(getItem(position));
            }
        }

        public void bind(ToolItem tool) {
            icon.setImageResource(tool.getImageResourse());
            label.setText(tool.getLabel());
        }
    }

    public interface OnImageClickListener {
        void onImageClick(ToolItem tool);
    }

    public void submitListWithAnimation(List<ToolItem> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return getCurrentList().size();
            }

            @Override
            public int getNewListSize() {
                return newList==null ? 0 : newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return false;
            }
        }, true);
        submitList(newList);
        diffResult.dispatchUpdatesTo(this);
    }
}