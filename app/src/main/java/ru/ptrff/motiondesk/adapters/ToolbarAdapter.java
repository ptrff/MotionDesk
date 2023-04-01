package ru.ptrff.motiondesk.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.data.ToolItem;
import ru.ptrff.motiondesk.databinding.ToolItemBinding;

public class ToolbarAdapter extends RecyclerView.Adapter<ToolbarAdapter.ToolItemHolder> {

    private ToolItemBinding binding;
    private final List<ToolItem> tools;
    private final OnImageClickListener listener;

    public ToolbarAdapter(List<ToolItem> tools, OnImageClickListener listener) {
        this.tools = tools;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ToolItemBinding.bind(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tool_item, parent, false));

        return new ToolItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolItemHolder holder, int position) {
        ToolItem tool = tools.get(position);
        bind(tool);
    }

    private void bind(ToolItem tool) {
        binding.icon.setImageResource(tool.getImageResourse());
        binding.label.setText(tool.getLabel());
    }

    public int getToolPosition(ToolItem item){
        return tools.indexOf(item);
    }

    @Override
    public int getItemCount() {
        return tools.size();
    }

    public class ToolItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView icon;
        private final LinearLayout allItem;
        private final TextView label;

        public ToolItemHolder(@NonNull ToolItemBinding binding) {
            super(binding.getRoot());
            icon = binding.icon;
            allItem = binding.allItem;
            label = binding.label;
            allItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onImageClick(tools.get(position));
            }
        }
    }

    public interface OnImageClickListener {
        void onImageClick(ToolItem tool);
    }
}
