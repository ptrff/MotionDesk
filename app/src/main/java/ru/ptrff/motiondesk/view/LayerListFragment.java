package ru.ptrff.motiondesk.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.ptrff.motiondesk.adapters.EffectsListAdapter;
import ru.ptrff.motiondesk.adapters.LayerListAdapter;
import ru.ptrff.motiondesk.databinding.FragmentEditorLayersBinding;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;

public class LayerListFragment extends Fragment implements LayerListAdapter.LayerListeners{

    private final WallpaperEditorEngine engine;
    private LayerListAdapter adapter;
    private FragmentEditorLayersBinding binding;
    private ItemTouchHelper touchHelper;

    public LayerListFragment(WallpaperEditorEngine engine){
        this.engine = engine;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditorLayersBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(binding != null) {
            adapter = new LayerListAdapter(engine.getStageActorArray(), this);
            binding.layerList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true));
            binding.layerList.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(binding.layerList);
        }
    }

    public void notifyItemInserted(int position){
        if (adapter!=null) {
            adapter.notifyItemInserted(position);
        }
    }

    public void notifyItemRemoved(int position){
        if(adapter!=null) {
            adapter.notifyItemRemoved(position);
            adapter = new LayerListAdapter(engine.getStageActorArray(), this);
            binding.layerList.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(binding.layerList);
        }
    }

    @Override
    public void onLayerClick(ActorHandler object) {
        engine.chooseObject(object);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }
}
