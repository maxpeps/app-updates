package ru.shagzashagom.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GoalsReceiver extends BroadcastReceiver {
    static final String TOGGLE = "ru.shagzashagom.app.GOAL_TOGGLE";
    static final String REFRESH = "ru.shagzashagom.app.GOALS_REFRESH";

    @Override
    public void onReceive(Context c, Intent i) {
        String a = i.getAction();
        if (TOGGLE.equals(a)) {
            String date = i.getStringExtra("date"), id = i.getStringExtra("id");
            if (date != null && id != null) GoalsStore.toggle(c, date, id);
            GoalsStore.refreshAll(c);
            GoalsBridgePlugin.emitToggled();
        } else {
            GoalsStore.refreshAll(c);
        }
    }
}
