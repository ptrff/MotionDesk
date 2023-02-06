package ru.ptrff.motiondesk.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ptrff.motiondesk.R;
import ru.ptrff.motiondesk.WallpaperEditor;
import ru.ptrff.motiondesk.databinding.FragmentInfoBinding;

public class InfoFragment extends BottomSheetDialogFragment {
    private FragmentInfoBinding binding;
    private String author;
    private String name;
    private String description;
    private String rating;
    private float stars;
    private View.OnClickListener click;
    private final List<String> tags = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater);

        bindData();
        fillTags();
        fillTextData();

        return binding.getRoot();
    }

    private void bindData() {
        Picasso.get().load(R.drawable.preview).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                binding.backrgoundImage.setImageBitmap(bitmap);
                binding.shimmerView.stopShimmerAnimation();
                binding.shimmerView.setVisibility(View.GONE);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                binding.shimmerView.setVisibility(View.VISIBLE);
                binding.shimmerView.startShimmerAnimation();
            }
        });

        binding.edit.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), WallpaperEditor.class);
            i.putExtra("Name", name);
            i.putExtra("Width", 1080);
            i.putExtra("Height", 1920);
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

        binding.backrgoundImage.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            builder.setTitle("Предпросмотр картинки");

            final ImageView imageView = new ImageView(getContext());
            imageView.setImageDrawable(((ImageView)view).getDrawable());
            imageView.setScaleType(ImageView.ScaleType.MATRIX);
            imageView.setOnTouchListener(new ImageMatrixTouchHandler(getContext()));
            builder.setView(imageView);

            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
            dialog.show();
        });

        binding.delete.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Удалить обои?");
            builder.setMessage("Вы действительно хотите удалить эти обои?");

            builder.setPositiveButton("Да", (dialog, which) -> dialog.dismiss());
            builder.setNegativeButton("Нет", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_bottom_dialog);
            dialog.show();
        });
    }

    private void fillTextData(){
        binding.name.setText(name);
        binding.author.setText(author);
        binding.description.setText(description);
        binding.stars.setText(stars + "/10");
        binding.rating.setText(rating);
        binding.apply.setOnClickListener(click);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        binding.rate.setImageTintList(ColorStateList.valueOf(typedValue.data));
        binding.rate.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
    }

    private void fillTags(){
        tags.add("1920x1080");
        tags.add("Anime");
        tags.add("Evangelion");
        tags.add("Asuka");
        tags.add("Misato");
        tags.add("Animated");
        tags.add("Art");

        Collections.sort(tags);

        for(String tag:tags){
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = requireContext().getTheme();
            TextView tagView = new TextView(getContext());
            FlexboxLayout.LayoutParams tagLayoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagLayoutParams.setMargins(0, 15, 15, 0);
            tagView.setLayoutParams(tagLayoutParams);
            tagView.setText(tag);
            tagView.setBackground(requireContext().getResources().getDrawable(R.drawable.tag_background));
            theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
            tagView.setBackgroundTintList(ColorStateList.valueOf(typedValue.data));
            tagView.setPadding(15, 5, 15, 5);
            theme.resolveAttribute(R.attr.backgroundBlackWhite, typedValue, true);
            tagView.setTextColor(typedValue.data);
            tagView.setTextSize(14);
            binding.tags.addView(tagView);
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

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    public void setRating(String rating){
        this.rating = rating;
    }

    public void setButtonOnClickListener(View.OnClickListener listener) {
        click = listener;
    }
}

