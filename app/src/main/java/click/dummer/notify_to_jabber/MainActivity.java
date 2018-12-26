package click.dummer.notify_to_jabber;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private TextView txtView;
    private EditText fromJIDedit;
    private EditText toJIDedit;
    private EditText passEdit;

    private EditText gotifyEdit;
    private EditText gotifyAppToken;
    private EditText gotifyFingerprint;

    private NotificationReceiver nReceiver;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtView = (TextView) findViewById(R.id.textView);
        nReceiver = new NotificationReceiver();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fromJIDedit = (EditText) findViewById(R.id.fromJIDedit);
        toJIDedit = (EditText) findViewById(R.id.toJIDedit);
        passEdit = (EditText) findViewById(R.id.passEdit);
        gotifyEdit = (EditText) findViewById(R.id.gotifyEdit);
        gotifyAppToken = (EditText) findViewById(R.id.gotifyAppToken);
        gotifyFingerprint = (EditText) findViewById(R.id.gotifyFingerprint);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Set<String> packs = NotificationManagerCompat.getEnabledListenerPackages(getApplicationContext());
        boolean readNotiPermissions = packs.contains(getPackageName());
        if (readNotiPermissions == false) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            alertDialogBuilder.setMessage(getString(R.string.sorry, getString(R.string.app_name)));
            alertDialogBuilder.show();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationService.ACTION_NEW_FINGERPRINT);
        filter.addAction(NotificationService.ACTION_INCOMING_MSG);
        registerReceiver(nReceiver, filter);

        String fromJID = "";
        String toJID = "";
        String pass = "";

        String gotifyUrl = "";
        String appToken = "";
        String sslFingerprint = "";

        if (mPreferences.contains("gotifyUrl")) {
            gotifyUrl = mPreferences.getString("gotifyUrl", gotifyUrl);
        }
        if (mPreferences.contains("appToken")) {
            appToken = mPreferences.getString("appToken", appToken);
        }

        if (mPreferences.contains("fromJID")) {
            fromJID = mPreferences.getString("fromJID", fromJID);
        }
        if (mPreferences.contains("toJID")) {
            toJID = mPreferences.getString("toJID", toJID);
        }
        if (mPreferences.contains("pass")) {
            pass = mPreferences.getString("pass", pass);
        }
        if (mPreferences.contains("sslFingerprint")) {
            sslFingerprint = mPreferences.getString("sslFingerprint", sslFingerprint);
        }
        fromJIDedit.setText(fromJID);
        toJIDedit.setText(toJID);
        passEdit.setText(pass);

        gotifyEdit.setText(gotifyUrl);
        gotifyAppToken.setText(appToken);
        gotifyFingerprint.setText(sslFingerprint);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);

        String fromJID = fromJIDedit.getText().toString();
        String toJID = toJIDedit.getText().toString();
        String pass = passEdit.getText().toString();
        String gotifyUrl = gotifyEdit.getText().toString();
        String appToken = gotifyAppToken.getText().toString();
        String sslFingerprint = gotifyFingerprint.getText().toString().trim();

        mPreferences.edit().putString("fromJID", fromJID).apply();
        mPreferences.edit().putString("toJID", toJID).apply();
        mPreferences.edit().putString("pass", pass).apply();
        mPreferences.edit().putString("gotifyUrl", gotifyUrl).apply();
        mPreferences.edit().putString("appToken", appToken).apply();
        mPreferences.edit().putString("sslFingerprint", sslFingerprint).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void buttonClicked(View v) {
        String fromJID = fromJIDedit.getText().toString();
        String toJID = toJIDedit.getText().toString();
        String pass = passEdit.getText().toString();
        String gotifyUrl = gotifyEdit.getText().toString();
        String appToken = gotifyAppToken.getText().toString();
        String sslFingerprint = gotifyFingerprint.getText().toString().trim();
        mPreferences.edit().putString("fromJID", fromJID).apply();
        mPreferences.edit().putString("toJID", toJID).apply();
        mPreferences.edit().putString("pass", pass).apply();
        mPreferences.edit().putString("gotifyUrl", gotifyUrl).apply();
        mPreferences.edit().putString("appToken", appToken).apply();
        mPreferences.edit().putString("sslFingerprint", sslFingerprint).apply();

        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this);
        ncomp.setContentTitle("Title");
        ncomp.setContentText("I am the Text from " + getString(R.string.app_name));
        ncomp.setTicker("I am the Ticker");
        ncomp.setSmallIcon(R.mipmap.ic_launcher);
        ncomp.setAutoCancel(true);
        nManager.notify((int)System.currentTimeMillis(), ncomp.build());
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(NotificationService.ACTION_INCOMING_MSG)) {
                String temp = intent.getStringExtra("notification_event") + ": " + txtView.getText();
                txtView.setText(temp);

            } else if (intent.getAction().equals(NotificationService.ACTION_NEW_FINGERPRINT)) {
                String sslFingerprint = intent.getStringExtra("new");
                if (gotifyFingerprint.getText().toString().trim().equals("")) {
                    gotifyFingerprint.setText(sslFingerprint);
                    mPreferences.edit().putString("sslFingerprint", sslFingerprint).apply();
                    txtView.setText(R.string.fingerprint_stored);
                } else {
                    txtView.setText(getString(R.string.fingerprint_may_bad, sslFingerprint));
                }
            }
        }
    }
}

