package ru.ptrff.motiondesk.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.badlogic.gdx.graphics.g2d.Batch;

import ru.ptrff.motiondesk.adapters.EffectsListAdapter;
import ru.ptrff.motiondesk.databinding.FragmentEditorLayersBinding;
import ru.ptrff.motiondesk.engine.effects.BaseEffect;
import ru.ptrff.motiondesk.engine.scene.ActorHandler;
import ru.ptrff.motiondesk.engine.scene.WallpaperEditorEngine;

public class EffectsListFragment extends Fragment implements EffectsListAdapter.EffectListeners{

    private final WallpaperEditorEngine engine;
    private EffectsListAdapter adapter;
    private FragmentEditorLayersBinding binding;
    private ItemTouchHelper touchHelper;
    private final EffectsListAdapter.EffectListeners effectListeners;

    public EffectsListFragment(WallpaperEditorEngine engine, EffectsListAdapter.EffectListeners effectListeners){
        this.engine = engine;
        this.effectListeners = effectListeners;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditorLayersBinding.inflate(inflater);

        onResume();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(binding != null) {
            adapter = new EffectsListAdapter(engine.getStageActorArray().get(engine.getDraggedSpriteId()), this);
            binding.layerList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true));
            binding.layerList.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(binding.layerList);
        }
    }

    public void notifyItemInserted(int position){
        if(adapter!=null) {
            adapter.notifyItemInserted(position);
        }
    }

    public void notifyItemRemoved(int position){
        if(adapter!=null) {
            adapter.notifyItemRemoved(position);
            adapter = new EffectsListAdapter(engine.getStageActorArray().get(engine.getDraggedSpriteId()), this);
            binding.layerList.setAdapter(adapter);

            ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(binding.layerList);
        }
    }

    @Override
    public void onEffectClick(BaseEffect effect) {
        effectListeners.onEffectClick(effect);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }
}
