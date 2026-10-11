package nl.crijns.ollieonair;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Eén scherm: het adres van het tv-scherm invullen. */
public class SettingsActivity extends Activity {

    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0E1A17"));
        int pad = dp(48);
        root.setPadding(pad * 2, pad, pad * 2, pad);

        TextView title = new TextView(this);
        title.setText("Ollie on Air");
        title.setTextColor(Color.parseColor("#EE9447"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Adres van het tv-scherm van de jukebox.\n"
                + "Thuis op je pc: het adres uit het servervenster, bijvoorbeeld 192.168.1.20:8888\n"
                + "Met een eigen domein: jouwdomein.nl\n"
                + "(/tv wordt er vanzelf achter gezet)");
        info.setTextColor(Color.parseColor("#CCFFF1DE"));
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        info.setLineSpacing(0, 1.25f);
        info.setPadding(0, dp(12), 0, dp(20));
        root.addView(info);

        input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setHint("192.168.1.20:8888");
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.parseColor("#66FFFFFF"));
        input.setBackground(box("#1B2B27", "#EE9447"));
        input.setPadding(dp(18), dp(14), dp(18), dp(14));
        String current = Prefs.url(this);
        if (current != null) input.setText(current);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                save();
                return true;
            }
            return false;
        });
        root.addView(input, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button go = new Button(this);
        go.setText("Opslaan en openen");
        go.setAllCaps(false);
        go.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        go.setTextColor(Color.WHITE);
        go.setBackground(box("#C9541F", "#FFFFFF"));
        go.setPadding(dp(28), dp(12), dp(28), dp(12));
        go.setOnClickListener(v -> save());
        go.setOnFocusChangeListener((v, has) -> v.setAlpha(has ? 1f : 0.8f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(24);
        root.addView(go, lp);

        TextView tip = new TextView(this);
        tip.setText("Tip: houd OK op de afstandsbediening 2 seconden ingedrukt om hier later terug te komen.");
        tip.setTextColor(Color.parseColor("#99FFF1DE"));
        tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tip.setPadding(0, dp(28), 0, 0);
        root.addView(tip);

        setContentView(root);
        input.requestFocus();
    }

    private void save() {
        String url = Prefs.normalize(input.getText().toString());
        if (url == null) {
            Toast.makeText(this, "Vul het adres in, bijvoorbeeld 192.168.1.20:8888", Toast.LENGTH_LONG).show();
            return;
        }
        Prefs.setUrl(this, url);
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private GradientDrawable box(String fill, String focusStroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.parseColor(fill));
        d.setCornerRadius(dp(12));
        d.setStroke(dp(2), Color.parseColor(focusStroke));
        return d;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()));
    }

    @Override
    public void onBackPressed() {
        if (Prefs.url(this) == null) {
            finish();
            return;
        }
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
