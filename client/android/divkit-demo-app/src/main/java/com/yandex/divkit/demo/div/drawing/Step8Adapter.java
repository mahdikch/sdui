package com.yandex.divkit.demo.div.drawing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import com.yandex.divkit.demo.R;


public class Step8Adapter extends RecyclerView.Adapter<Step8Adapter.ViewHolder> implements IItemTouchHelperAdapter {
  //region variable
  private List<Shap> mData;
  private LayoutInflater mInflater;
  private ItemClickListener mClickListener;
  private OnStartDragListener mDragListener;
  private Context context;
  private int index = -1;
  private boolean activeClick = false;
  private int indexSelectAll = -2;
  //endregion

  //  public Step8Adapter(Context context, List<Shap> data, OnStartDragListener mDragListener) {
  public Step8Adapter(Context context) {
    this.context = context;
    this.mInflater = LayoutInflater.from(context);

  }


  public void setCallback(OnStartDragListener mDragListener) {
    this.mDragListener = mDragListener;
  }

  public void setAdapter(List<Shap> shaps) {
    this.mData = shaps;
    notifyDataSetChanged();
  }

  public List<Shap> getAdapter() {
    return this.mData;

  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = mInflater.inflate(R.layout.row_step8, parent, false);
    return new ViewHolder(view);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Shap shap = mData.get(position);
    holder.txtId.setText(shap.getId());
    holder.txtTitle.setText(shap.getTitle());
    holder.layMian.setTag(shap.getIdUUId());


    if (index == indexSelectAll) {
      holder.layMian.setBackgroundColor(context.getResources().getColor(R.color.circle_green));
    } else if (index == position) {
      holder.layMian.setBackgroundColor(context.getResources().getColor(R.color.circle_green));
    } else {
      holder.layMian.setBackgroundColor(context.getResources().getColor(R.color.colorTrasparent));
    }

    holder.imgEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mClickListener.editPicture(shap , position);
      }
    });

    holder.imgMenu.setOnTouchListener((view, motionEvent) -> {
      if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && mDragListener != null) {
        mDragListener.onStartDrag(holder, holder.getBindingAdapterPosition());
        return true;
      }
      return false;
    });

    holder.layMian.setOnLongClickListener(view -> {
      if (mDragListener != null) {
        mDragListener.onStartDrag(holder, holder.getBindingAdapterPosition());
        return true;
      }
      return false;
    });

    holder.layMian.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!activeClick) {
          index = position;
          notifyDataSetChanged();
          if (mClickListener != null)
            mClickListener.onItemClick(view, position, view.getTag());
        }
      }
    });

  }

  @Override
  public int getItemCount() {
    return mData.size();
  }

  @Override
  public void onItemMove(int fromPosition, int toPosition) {
    Collections.swap(mData, fromPosition, toPosition);
    notifyItemMoved(fromPosition, toPosition);

    mClickListener.onItemMove(fromPosition, toPosition);

  }

  @Override
  public void onItemDismiss(int position) {
    mData.remove(position);
    notifyItemRemoved(position);
  }

  @Override
  public void getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {

    if (animationType == 8)
      mClickListener.getAnimationDuration(true);

  }

  public void setNotifyDataSetChanged() {
    index = -1;
    notifyDataSetChanged();
  }

  public void setNotifyDataSetChanged(int position) {
    index = position;
    notifyDataSetChanged();
  }

  public void deselectItems(boolean activeClick) {
    this.activeClick = activeClick;
    if (!activeClick) {

      this.index = -1;
      notifyDataSetChanged();
    }
  }

  public void selectItems() {
    index = indexSelectAll;
    notifyDataSetChanged();
  }

  // stores and recycles views as they are scrolled off screen
  public class ViewHolder extends RecyclerView.ViewHolder/* implements View.OnClickListener*/ {
    private TextView txtTitle, txtId;
    private ImageView imgMenu;
    private ViewGroup layMian;

    private AppCompatImageView imgEdit;

    ViewHolder(View itemView) {
      super(itemView);
      txtId = itemView.findViewById(R.id.txtId);
      txtTitle = itemView.findViewById(R.id.txtTitle);
      imgMenu = itemView.findViewById(R.id.item_list_menu_imageView);
      layMian = itemView.findViewById(R.id.layMian);
      imgEdit = itemView.findViewById(R.id.imgEdit);
//      itemView.setOnClickListener(this);
    }

  /*  @Override
    public void onClick(View view) {
      Log.i("AA__KAAANNNNA", "getTag:" + view.getTag());

      if (mClickListener != null)
        mClickListener.onItemClickMenu(view, getAdapterPosition(), view.getTag());

    }*/


  }


  // convenience method for getting data at click position
  public Shap getItem(int id) {
    return mData.get(id);
  }

  public void setSelectItem(int position) {
    this.index = position;
    notifyDataSetChanged();
  }

  // allows clicks events to be caught
  public void setClickListener(ItemClickListener itemClickListener) {
    this.mClickListener = itemClickListener;
  }

  // parent activity will implement this method to respond to click events
  public interface ItemClickListener {
    void onItemClick(View view, int position, Object tag);

    void onItemMove(/*int positoin,*/int fromPosition, int toPosition);

    void getAnimationDuration(boolean b);

    void editPicture(Shap uiUuId,int position);

//    void onLongPress(Shap shape);
  }


}
