package com.yandex.divkit.demo.div.drawing.turboimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;

import com.yandex.divkit.demo.div.drawing.MenuService;
import com.yandex.divkit.demo.div.drawing.InterfaceUUID;
import com.yandex.divkit.demo.div.drawing.PaintFloat;
import com.yandex.divkit.demo.div.drawing.Shap;


public class TurboImageView extends View implements MultiTouchObjectCanvas<MultiTouchObject>, ImageObjectUUID {
    //region variable
    private static final String TAG = "TurboImageView";

    private ImageObjectUUID imageObjectUUID;
    private ArrayList<MultiTouchObject> mImages = new ArrayList<>();
    public MultiTouchController<MultiTouchObject> multiTouchController = new MultiTouchController<>(this);

    private final PointInfo currTouchPoint = new PointInfo();

    private InterfaceUUID listenerUUID;
    private boolean b = true;
    private boolean boolTouch = false;
    private boolean isSelectItemList = false;
    private String idUUId = "";
    private static final int UI_MODE_ROTATE = 1;
    private static final int UI_MODE_ANISOTROPIC_SCALE = 2;
    private static final int mUIMode = UI_MODE_ROTATE;

    private int from = -1, to = -1;
    private List<Shap> shaps;

    private TurboImageViewListener listener;

    private int objectBorderColor = MultiTouchObject.DEFAULT_BORDER_COLOR;

    private boolean selectOnObjectAdded = true;
    //endregion

    public TurboImageView(Context context) {
        this(context, null);
    }

    public TurboImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TurboImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAAAA();
    }

    private void initAAAA() {
        setBackgroundColor(Color.TRANSPARENT);
        multiTouchController.setClickListener(this);
    }

    public void addObject(Context context, int resourceId, String indexObject, boolean b) {
        ImageObject imageObject = new ImageObject(resourceId, context.getResources(), indexObject);
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, int resourceId, boolean b) {
        ImageObject imageObject = new ImageObject(resourceId, context.getResources());
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, Drawable drawable, String indexObject, boolean b) {
        ImageObject imageObject = new ImageObject(drawable, context.getResources(), indexObject);
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, Drawable drawable, boolean b) {
        ImageObject imageObject = new ImageObject(drawable, context.getResources());
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, Bitmap bitmap, String id, String idUUId, MenuService menuService, boolean b) {
        ImageObject imageObject = new ImageObject(bitmap, context.getResources(), menuService);
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, Bitmap bitmap, String indexObject, String title, int intColor, String idUUId, boolean b, String base64, String parentTitle) {
        ImageObject imageObject = new ImageObject(bitmap, context.getResources(), indexObject, title, intColor, idUUId, base64, parentTitle);
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, Bitmap bitmap, String indexObject, String idUUId, boolean b) {
        ImageObject imageObject = new ImageObject(bitmap, context.getResources(), indexObject, idUUId);
        addObject(context, imageObject, b);
    }

    public void addObject(Context context, ImageObject imageObject, boolean b) {
        this.b = b;
        this.idUUId = imageObject.getStrIdUUId();

        Log.i("XXX_SSKSKSKS", "getStrIdUUId:" + imageObject.getStrIdUUId());

        deselectAll();
        if (b) {

        }

        imageObject.setSelected(selectOnObjectAdded);
        imageObject.setBorderColor(objectBorderColor);
        imageObject.setStrIdUUId(imageObject.getStrIdUUId());
        imageObject.setStrName(imageObject.getStrName());
        imageObject.setStrParentName(imageObject.getStrParentName());

        mImages.add(imageObject);

        float cx = getX() + getWidth() / 2;
        float cy = getY() + getHeight() / 2;
        mImages.get(mImages.size() - 1).init(context, cx, cy);

        invalidate();
    }


    public void setObjectSelectedBorderColor(int borderColor) {
        this.objectBorderColor = borderColor;
        for (MultiTouchObject imageObject : mImages) {
            imageObject.setBorderColor(borderColor);
        }
        invalidate();
    }

    public int getObjectSelectedBorderColor() {
        return this.objectBorderColor;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (MultiTouchObject imageObject : mImages) {
            imageObject.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
//    float lastTouchX = event.getX();
//    float lastTouchY = event.getY();

        Log.i("CC__PSPSS_QQQ",
                "\n boolTouch:" + boolTouch
                        + "\n isSelectItemList:" + isSelectItemList
        );
        if (boolTouch) {
            return multiTouchController.onTouchEvent(event, boolTouch, this.from, this.to);
        } else {
            if (isSelectItemList) {
                return multiTouchController.onTouchEvent(event, boolTouch, this.from, this.to);
            } else {
                return false;
            }
        }

    }

    /**
     * Get the image that is under the single-touch point, or return null
     * (canceling the drag op) if none
     */
    public MultiTouchObject getDraggableObjectAtPoint(PointInfo touchPoint) {
        float x = touchPoint.getX();
        float y = touchPoint.getY();

//    if (((ImageObject) multiTouchObject).getStrIdUUId().equals(ID_UUID)) {
//
//    }
        Log.i("XXX__PAPPA", "X:" + x + " Y:" + y);
        for (int i = mImages.size() - 1; i >= 0; i--) {
            ImageObject imageObject = (ImageObject) mImages.get(i);
            if (imageObject.containsPoint(x, y)) {
                return imageObject;
            }
        }
        return null;
    }

    /**
     * Select an object for dragging. Called whenever an object is found to be
     * under the point (non-null is returned by getDraggableObjectAtPoint()) and
     * a drag operation is starting. Called with null when drag op ends.
     */
    public void selectObject(MultiTouchObject multiTouchObject, PointInfo touchPoint, boolean b, int from, int to) {
//  public void selectObject(MultiTouchObject multiTouchObject, PointInfo touchPoint, boolean b) {

        Log.i("XXX_PAO__AOAO",
                "\n 6666666666666666666666666666666 b:" + b
        );


//    if (b) {


        if (multiTouchObject != null) {
            // Move image to the top of the stack when selected
            if (true) {
                if (from > -1) {

//          mImages.remove(multiTouchObject);
//          mImages.fabAdd(multiTouchObject);
                    if (shaps != null)
                        if (shaps.size() > 0)
                            for (int i = 0; i < shaps.size(); i++) {
                                if (!shaps.get(i).getIdUUId().equals(mImages.get(i).getStrIdUUId())) {
                                    MultiTouchObject buffer = mImages.get(i);
                                    String bufferUUid = shaps.get(i).getIdUUId();
                                    for (int j = 0; j < mImages.size(); j++) {

                                        if (mImages.get(j).getStrIdUUId().equals(bufferUUid)) {
                                            mImages.set(i, mImages.get(j));
                                            mImages.set(j, buffer);
                                        }

                                    }
                                }
                            }


                    currTouchPoint.set(touchPoint);


                    Log.i("AAA__KSKSK", "___________________________________\n");


                }


            }

            if (listener != null) {
                listener.onImageObjectSelected(multiTouchObject);
            }


            invalidate();
        } else {
            // Called with multiTouchObject == null when drag stops.
            if (listener != null) {
                listener.onImageObjectDropped();
            }
        }

    }


    public void selectObjectAAAAAAA() {

        if (listener != null) {
            listener.onImageObjectDropped();
        }
        invalidate();
    }

    @Override
    public void deselectAll() {
        for (MultiTouchObject imageObject : mImages) {
            imageObject.setSelected(false);
        }
        invalidate();
    }

    @Override
    public void selectAll() {
        for (MultiTouchObject imageObject : mImages) {
            imageObject.setSelected(true);
        }
        invalidate();
    }

    @Override
    public void selectMyObject(boolean active, int position, Object tag) {
        if (active) {


            this.isSelectItemList = isSelectItemList;
            this.idUUId = (String) tag;
            for (int i = 0, mImagesSize = mImages.size(); i < mImagesSize; i++) {
                MultiTouchObject imageObject = mImages.get(i);
                Log.i("CC_PSPSSP_SS", "mImages.get(" + i + "):" + mImages.get(i) + "_________position:" + position + "  imageObject.isSelected();" + imageObject.isSelected());
//      if (position == i) {
                if (imageObject.getStrIdUUId().equals((String) tag)) {
                    imageObject.setSelected(true);
                } else {
                    imageObject.setSelected(false);
                }
            }
            invalidate();
        }
    }

    @Override
    public void canvasTouched() {
        if (listener != null) {
            listener.onCanvasTouched();
        }
    }

    public boolean removeSelectedObject() {
        boolean deleted = false;
        Iterator<MultiTouchObject> iterator = mImages.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isSelected()) {
                idUUId = iterator.next().getStrIdUUId();
                Log.i("XXX_PAO_AOAO", ">>>>>>>>>>>>remove strIdUUId:" + idUUId);
                iterator.remove();
                deleted = true;
            }
        }

        invalidate();
        return deleted;
    }

    public String removeSelectedObject_UUID() {
        String idUUId = "";
        Iterator<MultiTouchObject> iterator = mImages.iterator();

        while (iterator.hasNext()) {
            MultiTouchObject multiTouchObject = iterator.next();
            if (multiTouchObject.isSelected()) {
                idUUId = multiTouchObject.getStrIdUUId();
                Log.i("XXX_PAO_AOAO", ">>>>>>>>>>>>remove strIdUUId:" + idUUId);
                iterator.remove();
            }
        }
        invalidate();
        return idUUId;
    }

    public void setFromAndTo(int from, int to, boolean b, List<Shap> shaps) {
        this.from = from;
        this.to = to;
        this.b = b;
        this.shaps = shaps;
        reorderObjectsByShapes(shaps);
    }

    /**
     * Sync canvas z-order with the object list.
     * Earlier items are drawn first (bottom), later items on top.
     */
    public void reorderObjectsByShapes(List<Shap> shaps) {
        if (shaps == null || shaps.isEmpty() || mImages.isEmpty()) {
            return;
        }

        ArrayList<MultiTouchObject> reordered = new ArrayList<>(mImages.size());
        for (Shap shap : shaps) {
            if (shap == null || shap.getIdUUId() == null) {
                continue;
            }
            for (MultiTouchObject obj : mImages) {
                if (shap.getIdUUId().equals(obj.getStrIdUUId())) {
                    reordered.add(obj);
                    break;
                }
            }
        }

        for (MultiTouchObject obj : mImages) {
            if (!reordered.contains(obj)) {
                reordered.add(obj);
            }
        }

        if (reordered.size() != mImages.size()) {
            return;
        }

        mImages.clear();
        mImages.addAll(reordered);
        invalidate();
    }

    public void setSelectItemList(boolean isSelectItemList) {
        this.isSelectItemList = isSelectItemList;
    }

    public void setTouchForDraw(boolean boolTouch) {
        this.boolTouch = boolTouch;
    }

    public List<PaintFloat> getListAnchorAtThisPositionAndScale() {
        return multiTouchController.getListAnchorAtThisPositionAndScale();
    }

    public void removeAllObjects() {
        mImages.clear();
        invalidate();
    }

    public int getSelectedObjectCount() {
        int count = 0;
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                count++;
            }
        }
        return count;
    }

    public int getObjectCount() {
        return mImages.size();
    }

    public ArrayList<MultiTouchObject> getObject() {
        return mImages;
    }

    public int getSizeListObject() {
        return mImages.size();
    }

    public void setObject(Context context, ArrayList<MultiTouchObject> mImagesSet) {
        mImages.clear();
        mImages.addAll(mImagesSet);
        ////////////////////////////////
        float cx = getX() + getWidth() / 2;
        float cy = getY() + getHeight() / 2;
        mImages.get(mImages.size() - 1).init(context, cx, cy);
        ////////////////////////////////
        invalidate();
    }

    public void setObject(ArrayList<MultiTouchObject> mImagesSet) {
        mImages.clear();
        mImages.addAll(mImagesSet);
        invalidate();
    }

    public void setListener(TurboImageViewListener turboImageViewListener) {
        this.listener = turboImageViewListener;
    }

    public void setListenerUUID(InterfaceUUID listenerUUID) {
        this.listenerUUID = listenerUUID;
    }

    /**
     * Get the current position and scale of the selected image. Called whenever
     * a drag starts or is reset.
     */
    public void getPositionAndScale(MultiTouchObject multiTouchObject, PositionAndScale objPosAndScaleOut) {
        objPosAndScaleOut.set(multiTouchObject.getCenterX(), multiTouchObject.getCenterY(),
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0,
                (multiTouchObject.getScaleX() + multiTouchObject.getScaleY()) / 2,
                (mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0, multiTouchObject.getScaleX(),
                multiTouchObject.getScaleY(), (mUIMode & UI_MODE_ROTATE) != 0,
                multiTouchObject.getAngle());
    }

    /**
     * Set the position and scale of the dragged/stretched image.
     */
    public boolean setPositionAndScale(MultiTouchObject multiTouchObject,
                                       PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
        currTouchPoint.set(touchPoint);
        boolean moved = multiTouchObject.setPos(newImgPosAndScale);
        if (moved) {
            invalidate();
        }
        return moved;
    }

    public boolean pointInObjectGrabArea(PointInfo touchPoint, MultiTouchObject multiTouchObject) {
        return false;
    }

    public boolean isSelectOnObjectAdded() {
        return selectOnObjectAdded;
    }

    public void setSelectOnObjectAdded(boolean selectOnObjectAdded) {
        this.selectOnObjectAdded = selectOnObjectAdded;
    }

    public void setFlippedHorizontallySelectedObject(boolean flipped) {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                imageObject.setFlippedHorizontally(flipped);
            }
        }
        invalidate();
    }

    public boolean isFlippedHorizontallySelectedObject() {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                return imageObject.isFlippedHorizontally();
            }
        }
        return false;
    }

    public void toggleFlippedHorizontallySelectedObject() {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                imageObject.setFlippedHorizontally(!imageObject.isFlippedHorizontally());
            }
        }
        invalidate();
    }

    public MultiTouchObject getSelectedObject()  {
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                return imageObject;
            }
        }
        return null;
    }

    public String getSelectedObjectUUID() {

        String UUID ="";
        for (MultiTouchObject imageObject : mImages) {
            if (imageObject.isSelected()) {
                      UUID = imageObject.getStrIdUUId();
                break;
            }
        }
        return UUID;
    }

    @Override
    public String getID_UUID() {
//    listenerUUID.setID_UUID(this.idUUId);
        return this.idUUId;
    }

    @Override
    public void setID_UUID(String id_UUID) {
        Log.i("id_UUID__SSSSSS", "id_UUID:" + id_UUID);
        listenerUUID.setID_UUID(id_UUID);
        this.idUUId = id_UUID;
    }
}
