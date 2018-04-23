package org.polaric.colorful;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ColorPickerDialog extends Dialog implements View.OnClickListener, ColorPickerAdapter.OnItemClickListener {
    private RecyclerView recycler;
    private Toolbar toolbar;
    private OnColorSelectedListener listener;
    private ColorPickerAdapter adapter;
    private boolean primary;

    public ColorPickerDialog(Context context, boolean primary) {
        super(context);
        this.primary = primary;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public void onItemClick(Colorful.ThemeColor color) {
        if (listener != null) {
            listener.onColorSelected(color);
        }
        dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_colorpicker);

        recycler = ((RecyclerView) findViewById(R.id.colorful_color_picker_recycler));
        toolbar = ((Toolbar) findViewById(R.id.colorful_color_picker_toolbar));

        toolbar.setNavigationOnClickListener(this);
        toolbar.setBackgroundColor(getContext().getResources().getColor(Colorful.getThemeDelegate().getPrimaryColor().getColorRes()));
        toolbar.setTitle(R.string.select_primary_color);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new ColorPickerAdapter(getContext(), primary);
        adapter.setOnItemClickListener(this);
        changeType(primary);
        recycler.setAdapter(adapter);
    }

    public void changeType(boolean primary) {
        if (primary) {
            if (null != toolbar) {
                toolbar.setTitle(R.string.select_primary_color);
            }
        } else {
            if (null != toolbar) {
                toolbar.setTitle(R.string.select_accent_color);
            }
        }
    }

    public interface OnColorSelectedListener {
        void onColorSelected(Colorful.ThemeColor color);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public void setTitle(int title) {
        toolbar.setTitle(title);
    }
}
