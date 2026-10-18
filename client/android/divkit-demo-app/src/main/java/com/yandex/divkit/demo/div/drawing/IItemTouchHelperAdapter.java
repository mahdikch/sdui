package com.yandex.divkit.demo.div.drawing;

import androidx.recyclerview.widget.RecyclerView;

public interface IItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

    void getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy);
}
