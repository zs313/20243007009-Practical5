package com.example.practicalfive;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MainActivity extends Activity {
    private final TextProcessor textProcessor = new TextProcessor();
    private EditText municipalityInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UiInsets.apply(this, R.id.main_root);
        municipalityInput = findViewById(R.id.municipality_input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // This search field does not collect a personal autofill value.
            municipalityInput.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }
        municipalityInput.setOnEditorActionListener((view, actionId, event) -> {
            // Consume both hardware events, but navigate once on the initial key-down.
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    openMunicipality();
                }
                return true;
            }
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                openMunicipality();
                return true;
            }
            return false;
        });
    }

    private void openMunicipality() {
        String input = municipalityInput.getText().toString();
        String name = textProcessor.processMunicipality(input);
        if (name == null) {
            municipalityInput.setError(getString(TextProcessor.isBlank(input)
                    ? R.string.input_empty_error : R.string.input_invalid_error));
            municipalityInput.requestFocus();
            return;
        }
        municipalityInput.setError(null);
        municipalityInput.setText(name);
        municipalityInput.setSelection(name.length());
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(municipalityInput.getWindowToken(), 0);
        }
        Intent intent = new Intent(this, Municipality.class);
        intent.putExtra(Municipality.EXTRA_MUNICIPALITY, name);
        startActivity(intent);
    }
}
