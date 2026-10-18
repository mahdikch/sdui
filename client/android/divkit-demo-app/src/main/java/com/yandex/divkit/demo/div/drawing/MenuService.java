package com.yandex.divkit.demo.div.drawing;

import android.graphics.Bitmap;

import androidx.annotation.ColorInt;

public class MenuService {
  private String title;
  private String nameMenuService;
  private String parentNameMenuService;
  private Bitmap iconBitmap;
  private String icon;
  private String id;
  private String parentId;
  private String idUUId;
  private String pathImage;
  private int intGrop;
  @ColorInt
  private int intColor;





  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Bitmap getIconBitmap() {
    return iconBitmap;
  }

  public void setIconBitmap(Bitmap iconBitmap) {
    this.iconBitmap = iconBitmap;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getIdUUId() {
    return idUUId;
  }

  public void setIdUUId(String idUUId) {
    this.idUUId = idUUId;
  }

  public int getIntGrop() {
    return intGrop;
  }

  public void setIntGrop(int intGrop) {
    this.intGrop = intGrop;
  }

  public int getIntColor() {
    return intColor;
  }

  public void setIntColor(int intColor) {
    this.intColor = intColor;
  }

  public String getName() {
    return nameMenuService;
  }

  public void setName(String nameMenuService) {
    this.nameMenuService = nameMenuService;
  }

  public String getParentName() {
    return parentNameMenuService;
  }

  public void setParentName(String parentNameMenuService) {
    this.parentNameMenuService = parentNameMenuService;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getPathImage() {
    return pathImage;
  }

  public void setPathImage(String pathImage) {
    this.pathImage = pathImage;
  }
}
