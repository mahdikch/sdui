package com.yandex.divkit.demo.div.drawing;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyItemTouchHelperCallback extends ItemTouchHelper.Callback {

  private final IItemTouchHelperAdapter mAdapter;

  public MyItemTouchHelperCallback(IItemTouchHelperAdapter mAdapter) {
    this.mAdapter = mAdapter;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
    int dragFlags;
    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
    if (layoutManager instanceof LinearLayoutManager
        && ((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
      dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    } else {
      dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    }
    return makeMovementFlags(dragFlags, 0);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
    mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
    return true;
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }

  @Override
  public boolean isItemViewSwipeEnabled() {
    return false;
  }

  @Override
  public boolean isLongPressDragEnabled() {
    return true;
  }

  @Override
  public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
    mAdapter.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
    return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy);
  }

  @Override
  public boolean canDropOver(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder current, @NonNull RecyclerView.ViewHolder target) {
    return super.canDropOver(recyclerView, current, target);
  }

  @Override
  public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
    super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
  }
}