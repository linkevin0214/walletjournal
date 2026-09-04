package com.example.walletjournal.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.walletjournal.contract.BaseContract;

/**
 * Base Activity for MVP screens. Provides a default implementation of
 * BaseContract.IBase_View so concrete Activities only need to override what they use.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseContract.IBase_View {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Tapping anywhere outside the currently focused EditText hides the soft keyboard
     * and clears focus, so the keyboard doesn't linger when the user taps away from it.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focused = getCurrentFocus();
            if (focused instanceof EditText && !isTouchInsideView(focused, event)) {
                hideKeyboard(focused);
                focused.clearFocus();
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean isTouchInsideView(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1],
                location[0] + view.getWidth(), location[1] + view.getHeight());
        return rect.contains((int) event.getRawX(), (int) event.getRawY());
    }

    protected void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void showLoading() {
        // Default no-op; override to show a real progress indicator.
    }

    @Override
    public void hideLoading() {
        // Default no-op; override to hide the progress indicator.
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
