package com.yandex.divkit.demo.div.drawing;

import android.graphics.Bitmap;

public class Shap {
  private Bitmap bitmap;
  private String id;
  private String idUUId;
  private String nameShap;
  private String parentNameShap;
  private String title;
  private boolean active;

  public Bitmap getBitmap() {
    return bitmap;
  }

  public void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIdUUId() {
    return idUUId;
  }

  public void setIdUUId(String idUUId) {
    this.idUUId = idUUId;
  }

  public String getName() {
    return nameShap;
  }

  public void setNameShap(String nameShap) {
    this.nameShap = nameShap;
  }

  public String getParentNameShap() {
    return parentNameShap;
  }

  public void setParentNameShap(String parentNameShap) {
    this.parentNameShap = parentNameShap;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
