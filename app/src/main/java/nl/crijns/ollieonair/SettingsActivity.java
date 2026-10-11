package nl.crijns.ollieonair;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Eén scherm: het adres van het tv-scherm invullen. */
public class SettingsActivity extends Activity {

    private EditText input;
    private TextView permStatus;
    private Button permButton;
    private CheckBox auto;

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

        // automatisch openen als de tv aangaat
        auto = new CheckBox(this);
        auto.setText("Automatisch openen als de tv aangaat");
        auto.setTextColor(Color.WHITE);
        auto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        auto.setChecked(Prefs.autostart(this));
        auto.setPadding(dp(8), dp(8), dp(8), dp(8));
        auto.setOnCheckedChangeListener((b, on) -> {
            Prefs.setAutostart(this, on);
            AutoStart.ensureService(this);
            refreshPermission();
        });
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        alp.topMargin = dp(28);
        root.addView(auto, alp);

        permStatus = new TextView(this);
        permStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        permStatus.setLineSpacing(0, 1.2f);
        permStatus.setPadding(dp(8), dp(4), 0, 0);
        root.addView(permStatus);

        permButton = new Button(this);
        permButton.setText("Toestemming geven");
        permButton.setAllCaps(false);
        permButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        permButton.setTextColor(Color.WHITE);
        permButton.setBackground(box("#2A3A35", "#EE9447"));
        permButton.setPadding(dp(22), dp(8), dp(22), dp(8));
        permButton.setOnClickListener(v -> askPermission());
        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        plp.topMargin = dp(8);
        root.addView(permButton, plp);

        TextView tip = new TextView(this);
        tip.setText("Tip: houd OK op de afstandsbediening 2 seconden ingedrukt om hier later terug te komen.");
        tip.setTextColor(Color.parseColor("#99FFF1DE"));
        tip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tip.setPadding(0, dp(28), 0, 0);
        root.addView(tip);

        setContentView(root);
        input.requestFocus();
        refreshPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permStatus != null) refreshPermission();
    }

    private void refreshPermission() {
        boolean on = auto.isChecked();
        boolean ok = AutoStart.allowed(this);
        permStatus.setVisibility(on ? android.view.View.VISIBLE : android.view.View.GONE);
        permButton.setVisibility(on && !ok ? android.view.View.VISIBLE : android.view.View.GONE);
        if (ok) {
            permStatus.setText("✓ Toestemming staat aan: de jukebox opent vanzelf na opstarten en als de tv uit slaapstand komt.");
            permStatus.setTextColor(Color.parseColor("#8FE3B0"));
        } else {
            permStatus.setText("Nog nodig: de toestemming \"Weergeven over andere apps\". Zonder die toestemming mag Android de app niet vanzelf openen.\n"
                    + "Lukt het niet met de knop, zie dan de LEESMIJ (één opdracht vanaf je pc).");
            permStatus.setTextColor(Color.parseColor("#FFC27A"));
        }
    }

    private void askPermission() {
        Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, "Deze tv heeft daar geen instellingenscherm voor. Volg de stap met de pc uit de LEESMIJ.", Toast.LENGTH_LONG).show();
            }
        }
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
