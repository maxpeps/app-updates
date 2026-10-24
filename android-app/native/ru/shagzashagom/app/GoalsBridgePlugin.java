package ru.shagzashagom.app;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.os.Build;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONObject;

@CapacitorPlugin(name = "GoalsBridge")
public class GoalsBridgePlugin extends Plugin {
    static GoalsBridgePlugin inst;

    @Override
    public void load() { inst = this; }

    static void emitToggled() {
        try { if (inst != null) inst.notifyListeners("toggled", new JSObject()); } catch (Exception ignored) {}
    }

    @PluginMethod
    public void sync(PluginCall call) {
        try {
            JSObject days = call.getObject("days", new JSObject());
            Boolean lock = call.getBoolean("lock", true);
            GoalsStore.setLock(getContext(), lock == null || lock);
            GoalsStore.setDays(getContext(), new JSONObject(days.toString()));
            GoalsStore.refreshAll(getContext());
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    @PluginMethod
    public void takePending(PluginCall call) {
        JSObject r = new JSObject();
        try { r.put("items", new JSArray(GoalsStore.takePending(getContext()).toString())); }
        catch (Exception e) { r.put("items", new JSArray()); }
        call.resolve(r);
    }

    @PluginMethod
    public void requestPin(PluginCall call) {
        JSObject r = new JSObject();
        boolean ok = false;
        try {
            AppWidgetManager m = AppWidgetManager.getInstance(getContext());
            if (Build.VERSION.SDK_INT >= 26 && m.isRequestPinAppWidgetSupported())
                ok = m.requestPinAppWidget(new ComponentName(getContext(), GoalsWidget.class), null, null);
        } catch (Exception ignored) {}
        r.put("ok", ok);
        call.resolve(r);
    }
}
