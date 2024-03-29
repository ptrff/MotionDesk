package ru.ptrff.motiondesk.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.transition.Explode;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.models.WallpaperItem;
import ru.ptrff.motiondesk.databinding.FragmentInfoBinding;
import ru.ptrff.motiondesk.utils.ProjectManager;

public class InfoFragment extends BottomSheetDialogFragment {
    private FragmentInfoBinding binding;
    private final WallpaperItem item;
    private View.OnClickListener click;
    private final InfoFragmentEvents events;

    public InfoFragment(WallpaperItem item, InfoFragmentEvents events){
        this.item = item;
        this.events = events;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater);

        if(item.isLocal())
            removePublicInfo();
        bindData();
        fillTags();
        fillTextData();

        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void bindData() {
        if (!item.hasPreviewImage()) {
            binding.backgroundImage.setImageDrawable(requireContext().getDrawable(R.drawable.no_image));
            binding.shimmerView.setVisibility(View.GONE);
        } else {
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

        binding.edit.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), WallpaperEditor.class);
            i.putExtra("new_project", false);
            i.putExtra("wallpaper_item", item);
            requireActivity().getWindow().setExitTransition(new Explode());
            startActivity(i);
        });

        binding.name.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(((TextView) view).getText().toString());
            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
            dialog.show();
        });

        binding.rate.setOnClickListener(view -> {
            RatingDialog dialog = new RatingDialog(5);
            dialog.show(getParentFragmentManager(), "rate");
        });

        binding.backgroundImage.setOnClickListener(view -> {
            if(item.hasPreviewImage()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Предпросмотр картинки");

                final ImageView imageView = new ImageView(getContext());
                imageView.setImageDrawable(((ImageView) view).getDrawable());
                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                imageView.setOnTouchListener(new ImageMatrixTouchHandler());
                builder.setView(imageView);

                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
                dialog.show();
            }
        });

        binding.delete.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Удалить обои?");
            builder.setMessage("Вы действительно хотите удалить эти обои?");

            builder.setPositiveButton("Да", (dialog, which) -> {
                ProjectManager.removeProject(requireContext(), item.getId());
                dialog.dismiss();
                events.onWallpaperRemoved();
                dismiss();
            });
            builder.setNegativeButton("Нет", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
            dialog.show();
        });
    }

    private void removePublicInfo(){
        ((View) binding.stars.getParent()).setVisibility(View.GONE);
        ((View) binding.downloads.getParent()).setVisibility(View.GONE);
        binding.rate.setVisibility(View.GONE);
    }

    private void fillTextData(){
        binding.name.setText(item.getName());
        if(item.isLocal()) {
            binding.author.setText(R.string.you);
        } else {
            binding.author.setText(item.getAuthor());
        }
        if (item.getDescription().equals("")){
            binding.description.setText(R.string.empty);
        }else {
            binding.description.setText(item.getDescription());
        }
        binding.stars.setText(String.valueOf(item.getStars()));
        binding.resolution.setText(item.getWidth()+"x"+item.getHeight());
        binding.size.setText(ProjectManager.getProjectSize(requireContext(), item.getId()));

        String rating = new String[]{
                requireContext().getResources().getString(R.string.restriction_for_all),
                requireContext().getResources().getString(R.string.restriction_16),
                requireContext().getResources().getString(R.string.restriction_18)
        }[item.getRating()];

        binding.rating.setText(rating);
        binding.apply.setOnClickListener(click);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        binding.rate.setImageTintList(ColorStateList.valueOf(typedValue.data));
        binding.rate.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
    }

    private void fillTags(){
        for(String tag:item.getTags()){
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = requireContext().getTheme();
            TextView tagView = new TextView(requireContext());
            FlexboxLayout.LayoutParams tagLayoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagLayoutParams.setMargins(0, 15, 15, 0);
            tagView.setLayoutParams(tagLayoutParams);
            tagView.setText(tag);
            tagView.setBackground(getResources().getDrawable(R.drawable.tag_background));
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
            tagView.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
            tagView.setPadding(15, 5, 15, 5);
            theme.resolveAttribute(R.attr.backgroundBlackWhite, typedValue, true);
            tagView.setTextColor(typedValue.data);
            tagView.setTextSize(14);
            binding.tags.addView(tagView);
        }

        if (item.getTags().size()==0){
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = requireContext().getTheme();
            TextView textView = new TextView(requireContext());
            textView.setText(R.string.empty);
            theme.resolveAttribute(R.attr.foregroundBlackWhite, typedValue, true);
            textView.setTextColor(typedValue.data);
            binding.tags.addView(textView);
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

    public void setButtonOnClickListener(View.OnClickListener listener) {
        click = listener;
    }

}

