package com.windcity.yefeng.yfsms.presentation.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.windcity.yefeng.yfsms.R;
import com.windcity.yefeng.yfsms.base.BaseActivity;
import com.windcity.yefeng.yfsms.domain.usecase.sms.SmsConst;
import com.yefeng.support.util.SharedPreferenceUtil;

import org.polaric.colorful.ColorfulUtil;
import org.polaric.colorful.Util;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        Context context = preference.getContext();
        String stringValue = null;
        if (Util.PREF_KEY_PRIMARY_COLOR_INDEX.equals(preference.getKey())) {
            int c = context.getResources().getColor(R.color.yf_grey_primary_dark);
            VectorDrawableCompat iconDrawable = getColorVectorDrawable(R.drawable.ic_palette_black_24dp, c, context);
            preference.setIcon(iconDrawable);
            int index = ColorfulUtil.getColorIndex(context,
                    Util.PREF_KEY_PRIMARY_COLOR_INDEX, Util.SP_THEME_COLOR_DEFAULT_PRIMARY_INDEX);
            stringValue = ColorfulUtil.getColorName(context)[index];
        } else if (Util.PREF_KEY_ACCENT_COLOR_INDEX.equals(preference.getKey())) {
            int c = context.getResources().getColor(R.color.yf_grey_primary_dark);
            VectorDrawableCompat iconDrawable = getColorVectorDrawable(R.drawable.ic_palette_reverse_black_24dp, c, context);
            preference.setIcon(iconDrawable);
            int index = ColorfulUtil.getColorIndex(
                    context, Util.PREF_KEY_ACCENT_COLOR_INDEX, Util.SP_THEME_COLOR_DEFAULT_ACCENT_INDEX);
            stringValue = ColorfulUtil.getColorName(context)[index];
        } else if (SmsConst.SP_SERVICE_CENTER.equals(preference.getKey())) {
            int c = context.getResources().getColor(R.color.yf_grey_primary_dark);
            VectorDrawableCompat iconDrawable = getColorVectorDrawable(R.drawable.ic_sms_black_24dp, c, context);
            preference.setIcon(iconDrawable);
            stringValue = SharedPreferenceUtil.getString(context, SmsConst.SP_SERVICE_CENTER, "");
        }
        if (TextUtils.isEmpty(stringValue)) {
            stringValue = preference.getContext().getString(R.string.defaults);
        }
        preference.setSummary(stringValue);
        return true;
    };
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    public static void startMe(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, null);
    }

    private static VectorDrawableCompat getColorVectorDrawable(int res, int color, Context context) {
        VectorDrawableCompat drawableCompat = VectorDrawableCompat.create(
                context.getResources(), res, context.getTheme());
        if (null != drawableCompat) {
            drawableCompat.setTint(color);
        }
        return drawableCompat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getSupportFragmentManager().beginTransaction().add(R.id.content, new SmsPreferenceFragment()).commitAllowingStateLoss();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Util.PREFERENCE_KEY.equals(key)
                || SmsConst.SP_SERVICE_CENTER.equals(key)) {
            Intent intent = new Intent(SettingActivity.this, SettingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SmsPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs);
            bindPreferenceSummaryToValue(findPreference(SmsConst.SP_SERVICE_CENTER));
            bindPreferenceSummaryToValue(findPreference(Util.PREF_KEY_PRIMARY_COLOR_INDEX));
            bindPreferenceSummaryToValue(findPreference(Util.PREF_KEY_ACCENT_COLOR_INDEX));
        }
    }
}
