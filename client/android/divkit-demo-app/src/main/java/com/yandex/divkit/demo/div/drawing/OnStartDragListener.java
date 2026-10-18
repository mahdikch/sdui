package com.yandex.divkit.demo.div.drawing;

import androidx.recyclerview.widget.RecyclerView;

public interface OnStartDragListener {
    void onStartDrag(RecyclerView.ViewHolder viewHolder, int position);
}
