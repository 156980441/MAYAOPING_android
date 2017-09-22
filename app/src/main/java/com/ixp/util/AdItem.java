package com.ixp.util;

import android.graphics.Bitmap;


public class AdItem {

    public Bitmap image;
    public String url;
    public String fileName;


    public AdItem(Bitmap image, String url, String fileName) {
        this.image = image;
        this.url = url;
        this.fileName = fileName;
    }

}
