package dev.oneuiproject.oneui.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.ViewParent;

/** Reuses the backdrop's hardware display lists instead of rasterizing the page on the UI thread. */
final class BackdropRenderNode {
    private RenderNode node;
    private final int[] hostPosition = new int[2], sourcePosition = new int[2];
    private final Rect recordingBounds = new Rect();
    private int lastX = Integer.MIN_VALUE, lastY = Integer.MIN_VALUE;
    private float lastRadius = -1;

    boolean draw(Canvas canvas, View host, View source, Rect bounds, boolean dirty) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || !canvas.isHardwareAccelerated()
                || source == null || source == host || !source.isAttachedToWindow()) return false;
        // Referencing an ancestor would put our own render node into the backdrop graph.
        for (ViewParent parent = host.getParent(); parent instanceof View; parent = parent.getParent())
            if (parent == source) return false;
        if (node == null) node = new RenderNode("OneUI backdrop");
        host.getLocationOnScreen(hostPosition);
        source.getLocationOnScreen(sourcePosition);
        float radius = BlurSettings.getIntensity(host.getContext())
                * host.getResources().getDisplayMetrics().density;
        // Blur needs neighboring pixels beyond the visible circle/card. Cropping its
        // input to the control bounds produces polygonal filter edges on small controls.
        int padding = (int) Math.ceil(radius * 2);
        recordingBounds.set(bounds);
        recordingBounds.inset(-padding, -padding);
        int x = sourcePosition[0] - hostPosition[0] - recordingBounds.left;
        int y = sourcePosition[1] - hostPosition[1] - recordingBounds.top;
        if (dirty || !node.hasDisplayList() || node.getWidth() != recordingBounds.width()
                || node.getHeight() != recordingBounds.height() || lastX != x || lastY != y) {
            node.setPosition(recordingBounds);
            Canvas recording = node.beginRecording(recordingBounds.width(), recordingBounds.height());
            recording.translate(x, y);
            try { source.draw(recording); }
            finally { node.endRecording(); }
            lastX = x; lastY = y;
        }
        if (lastRadius != radius) {
            node.setRenderEffect(radius > 0
                    ? RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP) : null);
            lastRadius = radius;
        }
        canvas.drawRenderNode(node);
        return true;
    }

    void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && node != null) node.discardDisplayList();
    }
}
