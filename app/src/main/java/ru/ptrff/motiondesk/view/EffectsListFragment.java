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
import ru.ptrff.motiondesk.engine.ActorHandler;
import ru.ptrff.motiondesk.engine.WallpaperEditorEngine;

public class EffectsListFragment extends Fragment implements EffectsListAdapter.LayerListeners{

    private final WallpaperEditorEngine engine;
    private EffectsListAdapter adapter;
    private FragmentEditorLayersBinding binding;
    private ItemTouchHelper touchHelper;

    EffectsListFragment(WallpaperEditorEngine engine){
        this.engine = engine;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditorLayersBinding.inflate(inflater);

        adapter = new EffectsListAdapter(engine.getStageActorArray().get(engine.getDraggedSpriteId()).getEffects(), this);
        binding.layerList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true));
        binding.layerList.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.layerList);

        return binding.getRoot();
    }

    public void notifyItemInserted(int position){
        adapter.notifyItemInserted(position);
    }

    public void notifyItemRemoved(int position){
        adapter.notifyItemRemoved(position);
        adapter = new EffectsListAdapter(engine.getStageActorArray().get(engine.getDraggedSpriteId()).getEffects(), this);
        binding.layerList.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.layerList);
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
