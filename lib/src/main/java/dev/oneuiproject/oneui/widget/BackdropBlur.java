package dev.oneuiproject.oneui.widget;

import android.graphics.Bitmap;

/** Three separable box passes: a smooth backdrop blur with work linear in image size. */
final class BackdropBlur {
    private BackdropBlur() { }

    static void apply(Bitmap bitmap, int[] pixels, int[] scratch, int radius) {
        if (radius <= 0) return;
        int width = bitmap.getWidth(), height = bitmap.getHeight();
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int pass = 0; pass < 3; pass++) {
            pass(pixels, scratch, width, height, radius, true);
            pass(scratch, pixels, width, height, radius, false);
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    private static void pass(int[] input, int[] output, int width, int height,
                             int radius, boolean horizontal) {
        int length = horizontal ? width : height;
        int lines = horizontal ? height : width;
        int stride = horizontal ? 1 : width;
        int count = radius * 2 + 1;
        for (int line = 0; line < lines; line++) {
            int start = horizontal ? line * width : line;
            int a = 0, r = 0, g = 0, b = 0;
            for (int i = -radius; i <= radius; i++) {
                int color = input[start + Math.max(0, Math.min(length - 1, i)) * stride];
                a += color >>> 24; r += color >> 16 & 255;
                g += color >> 8 & 255; b += color & 255;
            }
            for (int i = 0; i < length; i++) {
                output[start + i * stride] = (a / count << 24) | (r / count << 16)
                        | (g / count << 8) | b / count;
                int remove = input[start + Math.max(0, i - radius) * stride];
                int add = input[start + Math.min(length - 1, i + radius + 1) * stride];
                a += (add >>> 24) - (remove >>> 24);
                r += (add >> 16 & 255) - (remove >> 16 & 255);
                g += (add >> 8 & 255) - (remove >> 8 & 255);
                b += (add & 255) - (remove & 255);
            }
        }
    }
}
