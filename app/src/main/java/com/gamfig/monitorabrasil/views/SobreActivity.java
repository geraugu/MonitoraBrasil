package com.gamfig.monitorabrasil.views;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.gamfig.monitorabrasil.R;

public class SobreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);

        TextView versao = (TextView)findViewById(R.id.versao);
        try {
            versao.setText("Versão: "+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("SobreActivity")
                .putContentType("Activity"));
    }
}
