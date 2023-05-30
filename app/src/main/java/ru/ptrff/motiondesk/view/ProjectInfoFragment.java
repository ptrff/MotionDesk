package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.databinding.FragmentProjectParametersBinding;
import ru.ptrff.motiondesk.utils.ProjectManager;
import ru.ptrff.motiondesk.utils.Validation;

public class ProjectInfoFragment extends BottomSheetDialogFragment {
    private FragmentProjectParametersBinding binding;
    private final WallpaperItem item;
    private final ProjectInfoFragmentEvents events;
    private String[] ageRatings;
    private Bitmap newImage;

    public ProjectInfoFragment(WallpaperItem item, ProjectInfoFragmentEvents events){
        this.item = item;
        this.events = events;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProjectParametersBinding.inflate(inflater);

        bindData();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(binding.tags.getChildCount()>1)
            binding.tags.removeViews(1, binding.tags.getChildCount()-1);
        fillTextData();
        fillTags();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindData() {
        binding.shimmerView.setVisibility(View.GONE);
        if(item.getImage()!=null){
            binding.backgroundImage.setImageBitmap(item.getImage());
        }else if(!item.hasPreviewImage()){
            binding.backgroundImage.setImageResource(R.drawable.no_image);
        }else {
            Picasso.get().load(ProjectManager.getPreviewById(requireContext(), item.getId())).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    binding.backgroundImage.setImageBitmap(bitmap);
                    binding.shimmerView.stopShimmerAnimation();
                    binding.shimmerView.setVisibility(View.GONE);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    binding.backgroundImage.setImageDrawable(requireContext().getDrawable(R.drawable.no_image));
                    binding.shimmerView.stopShimmerAnimation();
                    binding.shimmerView.setVisibility(View.GONE);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    binding.shimmerView.setVisibility(View.VISIBLE);
                    binding.shimmerView.startShimmerAnimation();
                }
            });
        }

        ageRatings = new String[]{
                        requireContext().getResources().getString(R.string.restriction_for_all),
                        requireContext().getResources().getString(R.string.restriction_16),
                        requireContext().getResources().getString(R.string.restriction_18)
                };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ageRatings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.rating.setAdapter(adapter);

        binding.addTag.setOnClickListener(view -> addTag(getString(R.string.new_tag)));
        binding.backgroundImage.setOnClickListener(view -> {
            binding.shimmerView.startShimmerAnimation();
            binding.shimmerView.setVisibility(View.VISIBLE);
            binding.backgroundImage.setVisibility(View.GONE);
            events.chooseImageFromGallery();
        });
        binding.cancel.setOnClickListener(view -> dismiss());
        binding.apply.setOnClickListener(view -> saveWallpaperItemData());
    }

    public void setNewImage(Bitmap bitmap){
        newImage = bitmap;
        binding.backgroundImage.setImageBitmap(bitmap);
        binding.shimmerView.stopShimmerAnimation();
        binding.shimmerView.setVisibility(View.GONE);
        binding.backgroundImage.setVisibility(View.VISIBLE);
    }

    public void newImageLoadingFailed(){
        binding.shimmerView.stopShimmerAnimation();
        binding.shimmerView.setVisibility(View.GONE);
        binding.backgroundImage.setVisibility(View.VISIBLE);
    }


    private void fillTextData(){
        binding.name.setText(item.getName());
        binding.width.setText(item.getWidth()+"");
        binding.height.setText(item.getHeight()+"");
        binding.description.setText(item.getDescription());
        binding.rating.setText(ageRatings[item.getRating()], false);
    }

    private void addTag(String tag){
        Context context = requireContext();
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();

        theme.resolveAttribute(R.dimen.distance_s, typedValue, true);
        int distance_s = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.distance_s),
                context.getResources().getDisplayMetrics()
        );
        int field_size = binding.addTag.getHeight();
        int icon_size = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.icon_size_s),
                context.getResources().getDisplayMetrics()
        );

        LinearLayout tagView = new LinearLayout(context);
        tagView.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tagLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, field_size);
        tagLayoutParams.setMargins(0, distance_s, distance_s, 0);
        tagView.setBackground(context.getDrawable(R.drawable.tag_background));
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
        tagView.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
        tagView.setLayoutParams(tagLayoutParams);
        tagView.setPadding(distance_s, 0, distance_s, 0);
        tagView.setGravity(Gravity.CENTER);

        EditText tagField = new EditText(context);
        tagField.setSingleLine(true);
        tagField.setText(tag);
        tagField.setBackground(null);
        theme.resolveAttribute(R.attr.backgroundBlackWhite, typedValue, true);
        tagField.setTextColor(typedValue.data);
        tagField.setTextSize(14);

        tagView.addView(tagField);

        ImageView removeTag = new ImageView(context);
        removeTag.setImageTintList(ColorStateList.valueOf(typedValue.data));
        removeTag.setLayoutParams(new LinearLayout.LayoutParams(icon_size, icon_size));
        removeTag.setImageResource(R.drawable.ic_delete);
        removeTag.setOnClickListener(v -> binding.tags.removeView(tagView));

        tagView.addView(removeTag);

        binding.tags.addView(tagView);
    }

    private void fillTags(){
        for(String tag:item.getTags()){
            addTag(tag);
        }
    }

    private void saveWallpaperItemData(){
        int width = 0;
        int height= 0;
        String name = "";
        String description = "";
        if(!binding.name.getText().toString().equals("")) name = binding.name.getText().toString();
        if(!binding.width.getText().toString().equals("")) width = Integer.parseInt(binding.width.getText().toString());
        if(!binding.height.getText().toString().equals("")) height = Integer.parseInt(binding.height.getText().toString());
        if(!binding.description.getText().toString().equals("")) description = binding.description.getText().toString();
        if(Validation.checkString(name)&&width>=640&&width<=9000&&height>=640&&height<=9000) {
            item.setName(name);
            item.setWidth(width);
            item.setHeight(height);
            item.setDescription(description);
            item.setRating(Arrays.asList(ageRatings).indexOf(binding.rating.getText().toString()));

            Set<String> tags = new HashSet<>();
            for (int i = 0; i < binding.tags.getChildCount(); i++) {
                if(binding.tags.getChildAt(i) instanceof LinearLayout){
                    LinearLayout tagView = (LinearLayout) binding.tags.getChildAt(i);
                    if((tagView.getChildAt(0) instanceof EditText)) {
                        String text = ((EditText) tagView.getChildAt(0)).getText().toString();
                        if (Validation.checkString(text)) {
                            tags.add(text);
                        }
                    }
                }
            }

            item.setTags(new ArrayList<>(tags));

            Log.i("fddsfds", item.getTags().toString());

            if(newImage!=null) {
                item.setHasPreviewImage(true);
                item.setImage(newImage);
            }

            events.onProjectInfoChanged();
            dismiss();
        }else {
            Snackbar.make(binding.getRoot(), R.string.fields_filling_error, BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setPeekHeight(bottomSheet.getHeight());
        });
        return dialog;
    }
}

