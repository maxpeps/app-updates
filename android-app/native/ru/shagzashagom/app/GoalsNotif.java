package ru.shagzashagom.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONObject;

public class GoalsNotif {
    static final String CH = "shag_goals_today";
    static final int ID = 7701;

    static String cut(String s, int n) { return s.length() > n ? s.substring(0, n - 1) + "…" : s; }

    static void update(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        String d = GoalsStore.today();
        JSONArray a = GoalsStore.goals(c, d);
        if (!GoalsStore.lockOn(c) || a.length() == 0) { nm.cancel(ID); return; }
        if (Build.VERSION.SDK_INT >= 33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "Цели на день", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Список целей на сегодня на экране блокировки и в шторке");
            ch.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
        int n = a.length(), done = 0;
        NotificationCompat.InboxStyle st = new NotificationCompat.InboxStyle();
        String firstOpen = null;
        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CH);
        int actions = 0;
        for (int i = 0; i < n; i++) {
            JSONObject g = a.optJSONObject(i);
            if (g == null) continue;
            boolean ok = g.optBoolean("done");
            String t = g.optString("text");
            if (ok) done++;
            else if (firstOpen == null) firstOpen = t;
            st.addLine((ok ? "✓  " : "○  ") + t);
            if (!ok && actions < 3) {
                b.addAction(0, "✓ " + cut(t, 16), GoalsWidget.toggleIntent(c, d, g.optString("id"), 500 + i));
                actions++;
            }
        }
        String title = "Цели на день · " + done + " из " + n;
        String text = done == n ? "Все цели выполнены 🎉" : "Дальше: " + firstOpen;
        st.setBigContentTitle(title);
        b.setSmallIcon(R.drawable.ic_stat_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(st)
                .setColor(0xFF1F7A76)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(GoalsWidget.openApp(c));
        try { nm.notify(ID, b.build()); } catch (Exception ignored) {}
    }
}
