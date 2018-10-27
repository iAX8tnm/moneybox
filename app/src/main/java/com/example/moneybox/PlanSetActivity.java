package com.example.moneybox;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.Calendar;

import static com.example.moneybox.util.DateUtil.getCurrentDate;
import static com.example.moneybox.util.DateUtil.getTodayDate;

public class PlanSetActivity extends AppCompatActivity {

    SocketClient socket = SocketClient.getInstance();
    private String deadline = "2018/1/1";
    private Boolean hasSetPlan = false;
    private static final String TAG = "PlanSetActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_set);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("存钱计划");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        hasSetPlan = pref.getBoolean("hasSetPlan", false);

        if (hasSetPlan) {
            EditText et_plan_set_goal = findViewById(R.id.et_plan_set_goal);
            TextView tv_plan_set_date = findViewById(R.id.tv_plan_set_date);
            EditText et_plan_set_remarks = findViewById(R.id.et_plan_set_remarks);

            deadline = pref.getString("PlanDeadline", getTodayDate());
            String planGoal =  Integer.toString(pref.getInt("PlanGoal", 0)) + "";
            String remarks = pref.getString("PlanRemarks", "");

            et_plan_set_goal.setText(planGoal);
            tv_plan_set_date.setText(deadline);
            et_plan_set_remarks.setText(remarks);
        } else {
            TextView tv_plan_set_date = findViewById(R.id.tv_plan_set_date);
            deadline = getTodayDate();
            tv_plan_set_date.setText(deadline);
        }

        Log.d(TAG, "onCreate: execute");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
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

    public void showDateDialogPick(View view) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                if (isDateBefore(view)) {
                    StringBuilder string = new StringBuilder("");
                    string.append(year);
                    string.append("/");
                    string.append((monthOfYear+1));      //因为monthOfYear会比实际月份少一月所以这边要加1
                    string.append("/");
                    string.append(dayOfMonth);
                    deadline = string.toString();
                    Log.d(TAG, "onDateSet: " + deadline);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = findViewById(R.id.tv_plan_set_date);
                            textView.setText(deadline);
                        }
                    });
                } else Toast.makeText(PlanSetActivity.this, "请选择今天之后的日期", Toast.LENGTH_SHORT).show();


            }


        }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();

    }


    public void savePlan(View view) {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        EditText et_goal = findViewById(R.id.et_plan_set_goal);
        EditText et_remarks = findViewById(R.id.et_plan_set_remarks);

        //需要判断一下是不是真的有goal
        if (!et_goal.getText().toString().equals("")) {
            int goal = Integer.parseInt(et_goal.getText().toString());
            if (goal >= 1000000) {   //判断一下有没有超过一百万。。
                Toast.makeText(this, "安全起见，还是放到银行里吧(っ °Д °;)っ", Toast.LENGTH_SHORT).show();
            } else {
                String remarks = et_remarks.getText().toString();

                editor.putInt("PlanGoal", goal);
                editor.putString("PlanDeadline", deadline);
                editor.putString("PlanRemarks", remarks);
                editor.putBoolean("hasSetPlan", true);
                editor.apply();

                //把存钱数据即刻更新到存钱宝中
                SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
                int PlanGoal = pref.getInt("PlanGoal", 0);
                int count = 1;
                int TmpPlanGoal = PlanGoal;

                while((TmpPlanGoal /= 10) > 0) {
                    count++;
                }
                String TmpGoal = Integer.toString(PlanGoal);
                for (int i = 0; i < (7-count); i++)
                    TmpGoal += "\n";
                //socket.sendMessage(TmpGoal + "GOAL");

                int TotalVal = pref.getInt("TotalVal", 0);
                count = 1;
                int TmpTotalVal = TotalVal;
                while((TmpTotalVal /= 10) > 0) {
                    count++;
                }
                String TmpVal = Integer.toString(TotalVal);
                for (int i = 0; i < (7-count); i++)
                    TmpVal += "\n";
                socket.sendMessage(TmpGoal + "GOAL" + TmpVal + "VAL");


                //跳转PlanActivity
                Intent intent = new Intent(PlanSetActivity.this, PlanActivity.class);
                startActivity(intent);
                finish();
            }


        } else
            Toast.makeText(this, "请输入你的小目标", Toast.LENGTH_SHORT).show();

    }

    public void cancelPlan(View view) {

        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        final EditText et_goal = findViewById(R.id.et_plan_set_goal);
        final TextView tv_deadline = findViewById(R.id.tv_plan_set_date);
        final EditText et_remarks = findViewById(R.id.et_plan_set_remarks);

        deadline = getTodayDate();
        editor.putBoolean("hasSetPlan", false);
        editor.apply();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et_goal.setText("");
                et_remarks.setText("");
                tv_deadline.setText(deadline);
            }
        });

    }

    private boolean isDateBefore(DatePicker tempView) {
        Calendar mCalendar = Calendar.getInstance();
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(tempView.getYear(), tempView.getMonth(),
                tempView.getDayOfMonth(), 0, 0, 0);
        return tempCalendar.after(mCalendar);
    }

}
