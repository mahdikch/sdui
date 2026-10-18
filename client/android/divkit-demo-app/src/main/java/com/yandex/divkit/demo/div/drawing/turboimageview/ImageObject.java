package com.yandex.divkit.demo.div.drawing.turboimageview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.yandex.divkit.demo.div.drawing.MenuService;

public class ImageObject extends MultiTouchObject {
  private static final double INITIAL_SCALE_FACTOR = 0.15;

  private transient Drawable drawable;
  private String indexObject;
  private int intColor;
  private String idUUId;
  private String nameImageObject;
  private String base64;
  private String parentNameImageObject;
  //  private Bitmap bitmapImageObject;
  private String idImageObject;
  private String titleImageObject;
  private float x;
  private float y;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getIntColor() {
    return intColor;
  }

  public void setIntColor(int intColor) {
    this.intColor = intColor;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public String getStrIdUUId() {
    return idUUId;
  }

  public void setStrIdUUId(String strIdUUId) {
    this.idUUId = strIdUUId;
  }

  public Drawable getDrawable() {
    return drawable;
  }

  public void setDrawable(Drawable drawable) {
    this.drawable = drawable;
  }

  public String getIndexObject() {
    return indexObject;
  }

  public void setIndexObject(String indexObject) {
    this.indexObject = indexObject;
  }

  public String getBase64() {
    return base64;
  }

  public void setBase64(String base64) {
    this.base64 = base64;
  }

  public ImageObject(int resourceId, Resources res) {
    super(res);
    drawable = res.getDrawable(resourceId);
    initPaint();
  }

  public ImageObject(int resourceId, Resources res, String indexObject) {
    super(res);
    this.indexObject = indexObject;
    drawable = res.getDrawable(resourceId);
    initPaint();
  }

  public ImageObject(Drawable drawable, Resources res) {
    super(res);
    this.drawable = drawable;
    initPaint();
  }

  public ImageObject(Drawable drawable, Resources res, String indexObject) {
    super(res);
    this.indexObject = indexObject;
    this.drawable = drawable;
    initPaint();
  }

  public ImageObject(Bitmap bitmap, Resources res, MenuService menuService) {
    super(res);
    this.nameImageObject = menuService.getName();
    this.parentNameImageObject = menuService.getParentName();
//    this.bitmapImageObject = bitmap;
    this.idImageObject = menuService.getId();
    this.titleImageObject = menuService.getTitle();
    this.idUUId = menuService.getIdUUId();
    this.drawable = new BitmapDrawable(res, bitmap);
    initPaint();
  }

  public ImageObject(Bitmap bitmap, Resources res, MultiTouchObject multiTouchObject) {
    super(res);
    this.nameImageObject = multiTouchObject.getStrName();
    this.parentNameImageObject = multiTouchObject.getStrParentName();
//    this.bitmapImageObject = bitmap;
    this.idUUId = multiTouchObject.getStrIdUUId();
    this.idImageObject = multiTouchObject.getId();
    this.titleImageObject = multiTouchObject.getTitle();
    this.x = multiTouchObject.getStartMidX();
    this.y = multiTouchObject.getStartMidY();
    this.drawable = new BitmapDrawable(res, bitmap);
    initPaint();
  }

/*
  public ImageObject(Bitmap bitmap, Resources res) {
    super(res);
    this.drawable = new BitmapDrawable(res, bitmap);
    initPaint();
  }
*/

  public ImageObject(Resources res) {
    super(res);
  }

  public ImageObject(Bitmap bitmap, Resources res, String indexObject, String idUUId) {
    super(res);
    this.idUUId = idUUId;
    this.indexObject = indexObject;
    this.drawable = new BitmapDrawable(res, bitmap);
    initPaint();
  }

  public ImageObject(Bitmap bitmap, Resources res, String indexObject, String title, int intColor, String idUUId,String base64, String parentTitle) {
    super(res);
    this.idUUId = idUUId;
    this.indexObject = indexObject;
    this.idImageObject = indexObject;
    this.title = title;
    this.intColor = intColor;
    this.drawable = new BitmapDrawable(res, bitmap);
    this.base64 = base64;
    this.titleImageObject = parentTitle;
    initPaint();
  }

  public void initPaint() {
    borderPaint.setStyle(Paint.Style.STROKE);
    borderPaint.setColor(borderColor);
    borderPaint.setAntiAlias(true);
    borderPaint.setStrokeWidth(3.0f);
    borderPaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
  }

  public void draw(Canvas canvas) {
    canvas.save();

    float dx = (maxX + minX) / 2;
    float dy = (maxY + minY) / 2;

    drawable.setBounds((int) minX, (int) minY, (int) maxX, (int) maxY);
    if (flippedHorizontally) {
      canvas.scale(-1f, 1f, dx, dy);
    }
    canvas.translate(dx, dy);
    if (flippedHorizontally) {
      canvas.rotate(-angle * 180.0f / (float) Math.PI);
    } else {
      canvas.rotate(angle * 180.0f / (float) Math.PI);
    }
    canvas.translate(-dx, -dy);

    drawable.draw(canvas);

    if (isLatestSelected) {
      canvas.drawRect((int) minX, (int) minY, (int) maxX, (int) maxY, borderPaint);
      /*Ready to show an X button to delete the view but impossible to detect when that X button is touched*/
      //canvas.drawBitmap(cancelBitmap, minX - (cancelBitmap.getWidth() / 2), minY - (cancelBitmap.getHeight() / 2), new Paint());
    }

    canvas.restore();
  }

  /**
   * Called by activity's onPause() method to free memory used for loading the images
   */
  @Override
  public void unload() {
    this.drawable = null;
  }

  /**
   * Called by activity's onResume() method to init the images
   */
  @SuppressWarnings("deprecation")
  @Override
  public void init(Context context, float startMidX, float startMidY) {
    Resources res = context.getResources();
    init(res);

    this.startMidX = startMidX;
    this.startMidY = startMidY;

    width = drawable.getIntrinsicWidth();
    height = drawable.getIntrinsicHeight();

    float centerX;
    float centerY;
    float scaleX;
    float scaleY;
    float angle;
    if (firstLoad) {
      centerX = startMidX;
      centerY = startMidY;

      float scaleFactor = (float) (Math.max(displayWidth, displayHeight) /
        (float) Math.max(width, height) * INITIAL_SCALE_FACTOR);
      scaleX = scaleY = scaleFactor;
      angle = 0.0f;

      firstLoad = false;
    } else {
      centerX = this.centerX;
      centerY = this.centerY;
      scaleX = this.scaleX;
      scaleY = this.scaleY;
      angle = this.angle;
    }
    setPos(centerX, centerY, scaleX, scaleY, angle);
  }


  public String getName() {
    return nameImageObject;
  }

  public void setName(String name) {
    this.nameImageObject = name;
  }

  public String getParentName() {
    return parentNameImageObject;
  }

  public void setParentName(String parentName) {
    this.parentNameImageObject = parentName;
  }

  public String getIdUUId() {
    return idUUId;
  }

  public void setIdUUId(String idUUId) {
    this.idUUId = idUUId;
  }

  public String getNameImageObject() {
    return nameImageObject;
  }

  public void setNameImageObject(String nameImageObject) {
    this.nameImageObject = nameImageObject;
  }

  public String getParentNameImageObject() {
    return parentNameImageObject;
  }

  public void setParentNameImageObject(String parentNameImageObject) {
    this.parentNameImageObject = parentNameImageObject;
  }

  public String getIdImageObject() {
    return idImageObject;
  }

  public void setIdImageObject(String idImageObject) {
    this.idImageObject = idImageObject;
  }

  public String getTitleImageObject() {
    return titleImageObject;
  }

  public void setTitleImageObject(String titleImageObject) {
    this.titleImageObject = titleImageObject;
  }
}
