package ru.shagzashagom.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

public class GoalsWidget extends AppWidgetProvider {
    static final int[] ROWS = {R.id.g0, R.id.g1, R.id.g2, R.id.g3, R.id.g4, R.id.g5, R.id.g6, R.id.g7};
    static final int[] TXT = {R.id.t0, R.id.t1, R.id.t2, R.id.t3, R.id.t4, R.id.t5, R.id.t6, R.id.t7};
    static final int[] CHK = {R.id.c0, R.id.c1, R.id.c2, R.id.c3, R.id.c4, R.id.c5, R.id.c6, R.id.c7};

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        RemoteViews v = build(c);
        for (int id : ids) m.updateAppWidget(id, v);
        try { GoalsNotif.update(c); } catch (Exception ignored) {}
        GoalsStore.scheduleMidnight(c);
    }

    static void updateAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        int[] ids = m.getAppWidgetIds(new ComponentName(c, GoalsWidget.class));
        if (ids == null || ids.length == 0) return;
        RemoteViews v = build(c);
        for (int id : ids) m.updateAppWidget(id, v);
    }

    static PendingIntent openApp(Context c) {
        Intent i = new Intent(c, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static PendingIntent toggleIntent(Context c, String date, String id, int code) {
        Intent i = new Intent(c, GoalsReceiver.class).setAction(GoalsReceiver.TOGGLE)
                .setData(Uri.parse("shag://goal/" + date + "/" + Uri.encode(id)))
                .putExtra("date", date).putExtra("id", id);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static RemoteViews build(Context c) {
        String d = GoalsStore.today();
        JSONArray a = GoalsStore.goals(c, d);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.goals_widget);
        int n = a.length(), done = 0;
        for (int i = 0; i < n; i++) { JSONObject g = a.optJSONObject(i); if (g != null && g.optBoolean("done")) done++; }
        v.setTextViewText(R.id.w_count, n == 0 ? "" : done + " / " + n);
        v.setOnClickPendingIntent(R.id.w_head, openApp(c));
        v.setOnClickPendingIntent(R.id.w_empty, openApp(c));
        v.setViewVisibility(R.id.w_empty, n == 0 ? View.VISIBLE : View.GONE);
        for (int i = 0; i < ROWS.length; i++) {
            if (i < n) {
                JSONObject g = a.optJSONObject(i);
                boolean ok = g != null && g.optBoolean("done");
                v.setViewVisibility(ROWS[i], View.VISIBLE);
                v.setTextViewText(TXT[i], g != null ? g.optString("text") : "");
                v.setTextColor(TXT[i], ok ? 0x99FFFFFF : 0xFFFFFFFF);
                v.setImageViewResource(CHK[i], ok ? R.drawable.goal_check_on : R.drawable.goal_check_off);
                v.setOnClickPendingIntent(ROWS[i], toggleIntent(c, d, g != null ? g.optString("id") : "", 100 + i));
            } else {
                v.setViewVisibility(ROWS[i], View.GONE);
            }
        }
        v.setViewVisibility(R.id.w_more, n > ROWS.length ? View.VISIBLE : View.GONE);
        if (n > ROWS.length) v.setTextViewText(R.id.w_more, "+ ещё " + (n - ROWS.length));
        v.setOnClickPendingIntent(R.id.w_more, openApp(c));
        return v;
    }
}
