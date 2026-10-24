import sys
p=sys.argv[1];s=open(p,encoding='utf-8').read()
add='''
        <receiver android:name=".GoalsWidget" android:exported="true" android:label="Цели на день">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/goals_widget_info" />
        </receiver>
        <receiver android:name=".GoalsReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>
'''
if 'GoalsWidget' not in s:
    i=s.rindex('</application>');s=s[:i]+add+'    '+s[i:]
open(p,'w',encoding='utf-8').write(s)
print('manifest patched')
