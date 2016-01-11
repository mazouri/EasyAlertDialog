package com.oliverwdd.alertdialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.DialogPreference;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wangdd.easyalertdialog.EasyAlertDialog;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final EasyAlertDialog dialog = new EasyAlertDialog.Builder(this)
                .setTitle(R.string.easy_alert_dialog_title)
                .setMessage(R.string.easy_alert_dialog_msg)
                .setNegativeButton(R.string.easy_alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "You click negative button", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton(R.string.easy_alert_dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "You click positive button", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.easy_alert_dialog_neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "You click neutral button", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();



        Button showDialog = (Button) findViewById(R.id.btn_show);

        showDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
