package com.example.walletjournal.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.walletjournal.R;
import com.example.walletjournal.contract.AddCategoryContract;
import com.example.walletjournal.model.CategoryPalette;
import com.example.walletjournal.model.RecordType;
import com.example.walletjournal.presenter.AddCategoryPresenter;

/**
 * Add Category screen: pick an icon + color, name it, save it into Room.
 * The icon/color swatches are a small fixed set, so they're built
 * programmatically rather than via a RecyclerView. The new category is
 * tagged with whichever RecordType (EXPENSE/INCOME) was active on the Add
 * Record screen when "+新增自訂分類" was tapped — see EXTRA_RECORD_TYPE.
 */
public class AddCategoryActivity extends BaseActivity implements AddCategoryContract.IAddCategory_view {

    /** RecordType.name() of the tab that was active when this screen was opened. */
    public static final String EXTRA_RECORD_TYPE = "record_type";

    private AddCategoryPresenter presenter;

    private FrameLayout previewCircle;
    private TextView tvPreviewIcon;
    private EditText etName;
    private LinearLayout containerIcons;
    private LinearLayout containerColors;

    private final List<View> iconSwatches = new ArrayList<>();
    private final List<View> colorSwatches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        RecordType type = RecordType.EXPENSE;
        String typeExtra = getIntent().getStringExtra(EXTRA_RECORD_TYPE);
        if (typeExtra != null) {
            try {
                type = RecordType.valueOf(typeExtra);
            } catch (IllegalArgumentException e) {
                // Unknown value (shouldn't happen) — fall back to EXPENSE.
            }
        }
        presenter = new AddCategoryPresenter(getApplicationContext(), type);

        previewCircle = findViewById(R.id.preview_circle);
        tvPreviewIcon = findViewById(R.id.tv_preview_icon);
        etName = findViewById(R.id.et_name);
        containerIcons = findViewById(R.id.container_icons);
        containerColors = findViewById(R.id.container_colors);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        buildIconSwatches();
        buildColorSwatches();

        findViewById(R.id.btn_add_category).setOnClickListener(v ->
                presenter.submit(etName.getText().toString()));

        // Keyboard "Done" key was unwired, so pressing Enter did nothing.
        // Enter just closes the keyboard — it never submits on its own; saving only
        // ever happens via the 新增分類 button.
        etName.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event, EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard(v);
                v.clearFocus();
                return true;
            }
            return false;
        });

        presenter.attachView(this);
        presenter.selectIcon(CategoryPalette.ICON_KEYS[0]);
        presenter.selectColor(0);
    }

    private void buildIconSwatches() {
        for (String icon : CategoryPalette.ICON_KEYS) {
            FrameLayout ring = new FrameLayout(this);
            LinearLayout.LayoutParams ringParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            ringParams.setMarginEnd(dp(12));
            ring.setLayoutParams(ringParams);
            ring.setPadding(dp(4), dp(4), dp(4), dp(4));

            FrameLayout circle = new FrameLayout(this);
            circle.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            circle.setBackgroundResource(R.drawable.bg_icon_circle_gray);

            TextView glyph = new TextView(this);
            FrameLayout.LayoutParams glyphParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            glyphParams.gravity = Gravity.CENTER;
            glyph.setLayoutParams(glyphParams);
            glyph.setText(CategoryPalette.emoji(icon));
            glyph.setTextSize(16);

            circle.addView(glyph);
            ring.addView(circle);
            ring.setOnClickListener(v -> presenter.selectIcon(icon));

            containerIcons.addView(ring);
            iconSwatches.add(ring);
        }
    }

    private void buildColorSwatches() {
        for (int i = 0; i < CategoryPalette.BASE_COLORS.length; i++) {
            int index = i;

            FrameLayout ring = new FrameLayout(this);
            LinearLayout.LayoutParams ringParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            ringParams.setMarginEnd(dp(12));
            ring.setLayoutParams(ringParams);
            ring.setPadding(dp(4), dp(4), dp(4), dp(4));

            View dot = new View(this);
            dot.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            Drawable dotDrawable = ContextCompat.getDrawable(this, R.drawable.bg_dot);
            if (dotDrawable != null) {
                dotDrawable = dotDrawable.mutate();
                DrawableCompat.setTint(dotDrawable, CategoryPalette.baseColor(index));
                dot.setBackground(dotDrawable);
            }

            ring.addView(dot);
            ring.setOnClickListener(v -> presenter.selectColor(index));

            containerColors.addView(ring);
            colorSwatches.add(ring);
        }
    }

    @Override
    public void showSelection(String icon, int colorIndex) {
        Drawable previewBg = ContextCompat.getDrawable(this, R.drawable.bg_dot);
        if (previewBg != null) {
            previewBg = previewBg.mutate();
            DrawableCompat.setTint(previewBg, CategoryPalette.pastelColor(colorIndex));
            previewCircle.setBackground(previewBg);
        }
        tvPreviewIcon.setText(CategoryPalette.emoji(icon));
        tvPreviewIcon.setTextColor(CategoryPalette.baseColor(colorIndex));

        for (int i = 0; i < CategoryPalette.ICON_KEYS.length; i++) {
            boolean selected = CategoryPalette.ICON_KEYS[i].equals(icon);
            iconSwatches.get(i).setBackgroundResource(selected ? R.drawable.bg_swatch_ring_selected : 0);
        }
        for (int i = 0; i < colorSwatches.size(); i++) {
            colorSwatches.get(i).setBackgroundResource(i == colorIndex ? R.drawable.bg_swatch_ring_selected : 0);
        }
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
