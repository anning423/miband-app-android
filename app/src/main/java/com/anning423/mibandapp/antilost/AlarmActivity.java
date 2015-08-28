package com.anning423.mibandapp.antilost;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.anning423.mibandapp.R;

import java.util.Date;

public class AlarmActivity extends Activity {

    private TextView vHint;
    private View vOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        vHint = (TextView) findViewById(R.id.vHint);
        vHint.setText(new Date().toString());

        vOk = findViewById(R.id.vOk);
        vOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String hint = vHint.getText().toString();
        hint += "\n";
        hint += new Date().toString();
        vHint.setText(hint);
    }
}
