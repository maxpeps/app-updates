package ru.shagzashagom.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GoalsStore {
    static SharedPreferences p(Context c) { return c.getSharedPreferences("shag_goals", Context.MODE_PRIVATE); }

    static String today() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }

    static JSONObject days(Context c) {
        try { return new JSONObject(p(c).getString("days", "{}")); } catch (Exception e) { return new JSONObject(); }
    }

    static JSONArray goals(Context c, String date) {
        JSONArray a = days(c).optJSONArray(date);
        return a != null ? a : new JSONArray();
    }

    static JSONArray pending(Context c) {
        try { return new JSONArray(p(c).getString("pending", "[]")); } catch (Exception e) { return new JSONArray(); }
    }

    static synchronized void setDays(Context c, JSONObject d) {
        JSONArray pend = pending(c);
        for (int i = 0; i < pend.length(); i++) {
            JSONObject x = pend.optJSONObject(i);
            if (x == null) continue;
            JSONArray a = d.optJSONArray(x.optString("date"));
            if (a == null) continue;
            for (int j = 0; j < a.length(); j++) {
                JSONObject g = a.optJSONObject(j);
                if (g != null && x.optString("id").equals(g.optString("id"))) {
                    try { g.put("done", x.optBoolean("done")); } catch (Exception ignored) {}
                }
            }
        }
        p(c).edit().putString("days", d.toString()).commit();
    }

    static synchronized boolean toggle(Context c, String date, String id) {
        JSONObject d = days(c);
        JSONArray a = d.optJSONArray(date);
        boolean now = false;
        if (a == null) return false;
        for (int j = 0; j < a.length(); j++) {
            JSONObject g = a.optJSONObject(j);
            if (g != null && id.equals(g.optString("id"))) {
                now = !g.optBoolean("done");
                try { g.put("done", now); } catch (Exception ignored) {}
            }
        }
        JSONArray pend = pending(c), out = new JSONArray();
        for (int i = 0; i < pend.length(); i++) {
            JSONObject x = pend.optJSONObject(i);
            if (x != null && !(date.equals(x.optString("date")) && id.equals(x.optString("id")))) out.put(x);
        }
        try { out.put(new JSONObject().put("date", date).put("id", id).put("done", now)); } catch (Exception ignored) {}
        p(c).edit().putString("days", d.toString()).putString("pending", out.toString()).commit();
        return now;
    }

    static synchronized JSONArray takePending(Context c) {
        JSONArray a = pending(c);
        p(c).edit().putString("pending", "[]").commit();
        return a;
    }

    static boolean lockOn(Context c) { return p(c).getBoolean("lock", true); }

    static void setLock(Context c, boolean on) { p(c).edit().putBoolean("lock", on).commit(); }

    static void refreshAll(Context c) {
        try { GoalsWidget.updateAll(c); } catch (Exception ignored) {}
        try { GoalsNotif.update(c); } catch (Exception ignored) {}
        scheduleMidnight(c);
    }

    static void scheduleMidnight(Context c) {
        try {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(c, GoalsReceiver.class).setAction(GoalsReceiver.REFRESH);
            PendingIntent pi = PendingIntent.getBroadcast(c, 9001, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 1);
            cal.set(Calendar.SECOND, 0);
            if (Build.VERSION.SDK_INT >= 23) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
            else am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        } catch (Exception ignored) {}
    }
}
