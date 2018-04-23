package org.polaric.colorful;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yefeng.support.util.SharedPreferenceUtil;

class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ItemViewHolder> {
    private Context context;
    private OnItemClickListener listener;
    private boolean primary;

    ColorPickerAdapter(Context context, boolean primary) {
        this.context = context;
        this.primary = primary;
    }

    @Override
    public int getItemCount() {
        return Colorful.ThemeColor.values().length;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder ViewHolder, int i) {
        if (primary) {
            ViewHolder.circle.setColor(context.getResources().getColor(Colorful.ThemeColor.values()[i].getColorRes()));
        } else {
            ViewHolder.circle.setColor(context.getResources().getColor(Colorful.ThemeColor.values()[i].getColorAccent()));
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final ItemViewHolder holder = new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_coloritem, viewGroup, false));
        holder.circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (primary) {
                    SharedPreferenceUtil.putInt(view.getContext(), Util.PREF_KEY_PRIMARY_COLOR_INDEX, holder.getAdapterPosition());
                } else {
                    SharedPreferenceUtil.putInt(view.getContext(), Util.PREF_KEY_ACCENT_COLOR_INDEX, holder.getAdapterPosition());
                }
                if (listener != null) {
                    listener.onItemClick(Colorful.ThemeColor.values()[holder.getAdapterPosition()]);
                }
            }
        });
        return holder;
    }

    void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CircularView circle;

        ItemViewHolder(View v) {
            super(v);
            circle = ((CircularView) v);
        }
    }

    interface OnItemClickListener {
        void onItemClick(Colorful.ThemeColor color);
    }
}
