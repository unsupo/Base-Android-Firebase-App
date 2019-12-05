package com.jarndt.tournament_app.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProfileDrawable extends Drawable {
    private final Paint paint;
    private final String text;

    public ProfileDrawable(String color, String text) {
        this.text = text;
        paint = new Paint();
        paint.setColor(Color.parseColor(color));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // Get the drawable's bounds
        int width = getBounds().width();
        int height = getBounds().height();
        float radius = Math.min(width, height) / 2;

        // Draw a red circle in the center
        canvas.drawCircle(width/2, height/2, radius, paint);
        canvas.drawText(text,0,0,new Paint());
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
