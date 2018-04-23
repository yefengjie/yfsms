package org.polaric.colorful;

import android.content.Context;
import android.content.res.Resources;

import com.yefeng.support.util.SharedPreferenceUtil;

/**
 * Created by yefeng on 01/09/2017.
 */

@SuppressWarnings("deprecation")
public class ColorfulUtil {
    public static int getPrimaryColor(Context context) {
        return context.getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getColorRes());
    }

    public static int getAccentColor(Context context) {
        return context.getResources().getColor(Colorful.getThemeDelegate().getAccentColor().getColorAccent());
    }

    public static int getAccentColorLight(Context context) {
        return context.getResources().getColor(Colorful.getThemeDelegate().getAccentColor().getLightColorRes());
    }

    public static int getPrimaryDarkColor(Context context) {
        return context.getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getDarkColorRes());
    }

    public static int getPrimaryLightColor(Context context) {
        return context.getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getLightColorRes());
    }

    public static int getTransparentColor(Context context){
        return context.getResources().getColor(R.color.transparent);
    }

    public static int getColorIndex(Context context, String key, int defaultValue) {
        return SharedPreferenceUtil.getInt(context, key, defaultValue);
    }

    public static int getTextColorHoloWhite(Context context){
        return context.getResources().getColor(R.color.colorHoloLightWhite);
    }

    public static int getTextColorSecondary(Context context){
        return context.getResources().getColor(R.color.text_secondary);
    }

    public static int getTextColorPrimary(Context context){
        return context.getResources().getColor(R.color.text_primary);
    }

    public static int getTextColorWhite(Context context){
        return context.getResources().getColor(R.color.white);
    }

    public static int getTextColorBlack(Context context){
        return context.getResources().getColor(R.color.black);
    }

    public static String[] getColorName(Context c) {
        Resources res = c.getResources();
        return new String[]{
                res.getString(R.string.red),
                res.getString(R.string.pink),
                res.getString(R.string.purple),
                res.getString(R.string.deep_purple),
                res.getString(R.string.indigo),
                res.getString(R.string.blue),
                res.getString(R.string.light_blue),
                res.getString(R.string.cyan),
                res.getString(R.string.teal),
                res.getString(R.string.green),
                res.getString(R.string.light_green),
                res.getString(R.string.yellow),
                res.getString(R.string.amber),
                res.getString(R.string.orange),
                res.getString(R.string.deep_orange),
                res.getString(R.string.brown),
        };
    }
}
