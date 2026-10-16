package dev.oneuiproject.oneui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

/** Adds the shared palette option to standard Android and custom XML components. */
public final class PaletteInflater {
    private PaletteInflater() { }

    public static LayoutInflater create(AppCompatActivity activity, LayoutInflater original) {
        LayoutInflater inflater = original.cloneInContext(activity);
        inflater.setFactory2(new LayoutInflater.Factory2() {
            @Override public View onCreateView(String name, Context context, AttributeSet attrs) {
                return onCreateView(null, name, context, attrs);
            }
            @Override public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                if ("fragment".equals(name) || "view".equals(name)) return null;
                Context base = parent != null ? parent.getContext() : context;
                boolean wrapsItself = name.startsWith("dev.oneuiproject.oneui.widget.")
                        || name.startsWith("dev.oneuiproject.oneui.layout.");
                Context component = wrapsItself ? base : OneUIPalette.wrap(base, attrs);
                View view = activity.getDelegate().createView(parent, name, component, attrs);
                if (view == null) {
                    String[] names = name.contains(".") ? new String[]{name}
                            : new String[]{"android.widget." + name, "android.view." + name,
                                    "android.webkit." + name};
                    for (String className : names) {
                        try {
                            Class<? extends View> type = Class.forName(className, false,
                                    component.getClassLoader()).asSubclass(View.class);
                            view = type.getConstructor(Context.class, AttributeSet.class)
                                    .newInstance(component, attrs);
                            break;
                        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                            // Let the next prefix or Android's inflater handle this name.
                        } catch (ReflectiveOperationException error) {
                            throw new android.view.InflateException("Unable to inflate " + name, error);
                        }
                    }
                }
                if (view != null) OneUIPalette.applyToViewTree(view);
                return view;
            }
        });
        return inflater;
    }
}
