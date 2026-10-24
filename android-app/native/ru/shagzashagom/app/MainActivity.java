package ru.shagzashagom.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(GoalsBridgePlugin.class);
        super.onCreate(savedInstanceState);
    }
}
