package com.yandex.divkit.demo.div.drawing;

import android.graphics.Path;

class FingerPath {
    final int color;
    final boolean emboss;
    final boolean blur;
    final int strokeWidth;
    final Path path;

    FingerPath(int color, boolean emboss, boolean blur, int strokeWidth, Path path) {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
