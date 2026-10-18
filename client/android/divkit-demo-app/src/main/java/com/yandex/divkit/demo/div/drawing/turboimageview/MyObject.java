package com.yandex.divkit.demo.div.drawing.turboimageview;

import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;

public class MyObject implements Serializable {

  private boolean firstLoad = true;
  private String indexObject;
  private String idUUId;
  private String title;
  private int intColor;

  private String strName;
  private String strParentName;


  public int getIntColor() {
    return intColor;
  }

  public void setIntColor(int intColor) {
    this.intColor = intColor;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getIdUUId() {
    return idUUId;
  }

  public void setIdUUId(String idUUId) {
    this.idUUId = idUUId;
  }

  public String getIndexObject() {
    return indexObject;
  }

  public void setIndexObject(String indexObject) {
    this.indexObject = indexObject;
  }

  private Paint paint = new Paint();
  private final Paint borderPaint = new Paint();

  private final int DEFAULT_BORDER_COLOR = Color.BLACK;
  private int borderColor = DEFAULT_BORDER_COLOR;

  private int width;
  private int height;

  // width/height of screen
  private int displayWidth;
  private int displayHeight;

  private float centerX;
  private float centerY;
  private float scaleX;
  private float scaleY;
  private float angle;

  private float minX;
  private float maxX;
  private float minY;
  private float maxY;

  private final int GRAB_AREA_SIZE = 40;
  private boolean isGrabAreaSelected = false;
  private boolean isLatestSelected = false;

  private float grabAreaX1;
  private float grabAreaY1;
  private float grabAreaX2;
  private float grabAreaY2;

  private float startMidX;
  private float startMidY;

  private boolean flippedHorizontally;

  private final int UI_MODE_ROTATE = 1;
  private final int UI_MODE_ANISOTROPIC_SCALE = 2;
  private final int mUIMode = UI_MODE_ROTATE;

  public boolean isFirstLoad() {
    return firstLoad;
  }

  public void setFirstLoad(boolean firstLoad) {
    this.firstLoad = firstLoad;
  }

  public Paint getPaint() {
    return paint;
  }

  public void setPaint(Paint paint) {
    this.paint = paint;
  }

  public Paint getBorderPaint() {
    return borderPaint;
  }

  public int getDEFAULT_BORDER_COLOR() {
    return DEFAULT_BORDER_COLOR;
  }

  public int getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(int borderColor) {
    this.borderColor = borderColor;
  }

  public String getStrName() {
    return strName;
  }

  public void setStrName(String strName) {
    this.strName = strName;
  }

  public String getStrParentName() {
    return strParentName;
  }

  public void setStrParentName(String strParentName) {
    this.strParentName = strParentName;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getDisplayWidth() {
    return displayWidth;
  }

  public void setDisplayWidth(int displayWidth) {
    this.displayWidth = displayWidth;
  }

  public int getDisplayHeight() {
    return displayHeight;
  }

  public void setDisplayHeight(int displayHeight) {
    this.displayHeight = displayHeight;
  }

  public float getCenterX() {
    return centerX;
  }

  public void setCenterX(float centerX) {
    this.centerX = centerX;
  }

  public float getCenterY() {
    return centerY;
  }

  public void setCenterY(float centerY) {
    this.centerY = centerY;
  }

  public float getScaleX() {
    return scaleX;
  }

  public void setScaleX(float scaleX) {
    this.scaleX = scaleX;
  }

  public float getScaleY() {
    return scaleY;
  }

  public void setScaleY(float scaleY) {
    this.scaleY = scaleY;
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
  }

  public float getMinX() {
    return minX;
  }

  public void setMinX(float minX) {
    this.minX = minX;
  }

  public float getMaxX() {
    return maxX;
  }

  public void setMaxX(float maxX) {
    this.maxX = maxX;
  }

  public float getMinY() {
    return minY;
  }

  public void setMinY(float minY) {
    this.minY = minY;
  }

  public float getMaxY() {
    return maxY;
  }

  public void setMaxY(float maxY) {
    this.maxY = maxY;
  }

  public int getGRAB_AREA_SIZE() {
    return GRAB_AREA_SIZE;
  }

  public boolean isGrabAreaSelected() {
    return isGrabAreaSelected;
  }

  public void setGrabAreaSelected(boolean grabAreaSelected) {
    isGrabAreaSelected = grabAreaSelected;
  }

  public boolean isLatestSelected() {
    return isLatestSelected;
  }

  public void setLatestSelected(boolean latestSelected) {
    isLatestSelected = latestSelected;
  }

  public float getGrabAreaX1() {
    return grabAreaX1;
  }

  public void setGrabAreaX1(float grabAreaX1) {
    this.grabAreaX1 = grabAreaX1;
  }

  public float getGrabAreaY1() {
    return grabAreaY1;
  }

  public void setGrabAreaY1(float grabAreaY1) {
    this.grabAreaY1 = grabAreaY1;
  }

  public float getGrabAreaX2() {
    return grabAreaX2;
  }

  public void setGrabAreaX2(float grabAreaX2) {
    this.grabAreaX2 = grabAreaX2;
  }

  public float getGrabAreaY2() {
    return grabAreaY2;
  }

  public void setGrabAreaY2(float grabAreaY2) {
    this.grabAreaY2 = grabAreaY2;
  }

  public float getStartMidX() {
    return startMidX;
  }

  public void setStartMidX(float startMidX) {
    this.startMidX = startMidX;
  }

  public float getStartMidY() {
    return startMidY;
  }

  public void setStartMidY(float startMidY) {
    this.startMidY = startMidY;
  }

  public boolean isFlippedHorizontally() {
    return flippedHorizontally;
  }

  public void setFlippedHorizontally(boolean flippedHorizontally) {
    this.flippedHorizontally = flippedHorizontally;
  }

  public int getUI_MODE_ROTATE() {
    return UI_MODE_ROTATE;
  }

  public int getUI_MODE_ANISOTROPIC_SCALE() {
    return UI_MODE_ANISOTROPIC_SCALE;
  }

  public int getmUIMode() {
    return mUIMode;
  }

  public MyObject(ImageObject multiTouchObject) {
    this.firstLoad = multiTouchObject.firstLoad;
    this.indexObject = multiTouchObject.getIndexObject();
    this.paint = multiTouchObject.paint;
    this.borderColor = multiTouchObject.borderColor;
    this.width = multiTouchObject.width;
    this.height = multiTouchObject.height;
    this.displayWidth = multiTouchObject.displayWidth;
    this.displayHeight = multiTouchObject.displayHeight;
    this.centerX = multiTouchObject.centerX;
    this.centerY = multiTouchObject.centerY;
    this.scaleX = multiTouchObject.scaleX;
    this.scaleY = multiTouchObject.scaleY;
    this.angle = multiTouchObject.angle;
    this.minX = multiTouchObject.minX;
    this.maxX = multiTouchObject.maxX;
    this.minY = multiTouchObject.minY;
    this.maxY = multiTouchObject.maxY;
    this.isGrabAreaSelected = multiTouchObject.isGrabAreaSelected;
    this.isLatestSelected = multiTouchObject.isLatestSelected;
    this.grabAreaX1 = multiTouchObject.grabAreaX1;
    this.grabAreaY1 = multiTouchObject.grabAreaY1;
    this.grabAreaX2 = multiTouchObject.grabAreaX2;
    this.grabAreaY2 = multiTouchObject.grabAreaY2;
    this.startMidX = multiTouchObject.startMidX;
    this.startMidY = multiTouchObject.startMidY;
    this.flippedHorizontally = multiTouchObject.flippedHorizontally;
  }
}
