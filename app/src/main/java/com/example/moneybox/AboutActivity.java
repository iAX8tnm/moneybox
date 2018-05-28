package com.example.moneybox;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;



public class AboutActivity extends AppCompatActivity {

    private static final String TAG = "AboutActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("关于我们");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView textView = findViewById(R.id.tv_introduction_people);

        SpannableString ss =new SpannableString("                    开发人员\n\n                      袁陈标\n                      侯有钊\n                      傅诗晴\n\n\nCreated By STU MAX Extremity\n");
        ss.setSpan(new AbsoluteSizeSpan(40), 4, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(ss);

        Log.d(TAG, "onCreate: execute");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
