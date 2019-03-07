package com.example.moneybox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import static com.example.moneybox.util.DateUtil.getTodayDate;

public class PlanActivity extends AppCompatActivity {


    List<View> viewList = new ArrayList<View>();
    private ViewPager viewPager;
    private int itemPosition;
    private int mCount = 3;

    private static final int TIME = 3500;
    private Handler mHandler = new Handler();
    private static final String TAG = "PlanActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("存钱计划");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView tv_plan_goal = findViewById(R.id.tv_plan_goal_val);
        TextView tv_plan_date = findViewById(R.id.tv_plan_date_val);
        TextView tv_plan_remarks = findViewById(R.id.tv_plan_remarks_val);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String goal = Integer.toString(pref.getInt("PlanGoal", 0)) + "￥";
        String planGoal = Integer.toString(pref.getInt("PlanGoal", 0));
        String deadline = pref.getString("PlanDeadline", getTodayDate());
        String remarks = pref.getString("PlanRemarks", "");

        int totalVal = pref.getInt("TotalVal", 0);

        tv_plan_goal.setText(goal);
        tv_plan_date.setText(deadline);
        tv_plan_remarks.setText(remarks);

        Log.d(TAG, "onCreate:   "+ (totalVal + Integer.parseInt(planGoal)));
        if (totalVal > Integer.parseInt(planGoal) || Integer.parseInt(planGoal) == 0) {
            progressBar.setProgress(100);
        } else {
            progressBar.setProgress((100*totalVal/Integer.parseInt(planGoal)));
        }


        initViewPager();

        mHandler.postDelayed(runnableForViewPager, TIME);

        Log.d(TAG, "onCreate: execute");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.btn, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch(item.getItemId()) {
           case android.R.id.home: {
               finish();
               return true;
           }
           case R.id.btn_plan_menu_edit: {

               Intent intent = new Intent(this, PlanSetActivity.class);
               startActivity(intent);
               finish();
               return true;
           }
       }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


    /**
     * Init ViewPager
     */
    private void initViewPager() {
        LayoutInflater layoutInflater = getLayoutInflater().from(PlanActivity.this);
        viewPager = findViewById(R.id.viewpager_of_plan);

        View view1 = layoutInflater.inflate(R.layout.someknowledge1, null);
        View view2 = layoutInflater.inflate(R.layout.someknowledge2, null);
        View view3 = layoutInflater.inflate(R.layout.someknowledge3, null);

        TextView text1 = view1.findViewById(R.id.tv_knowledge_tip1);
        TextView text2 = view2.findViewById(R.id.tv_knowledge_tip2);
        TextView text3 = view3.findViewById(R.id.tv_knowledge_tip3);

        view1.setBackground(getResources().getDrawable(R.drawable.ic_background4));
        view2.setBackground(getResources().getDrawable(R.drawable.ic_background5));
        view3.setBackground(getResources().getDrawable(R.drawable.ic_background6));

        text1.setText("当你定下了大目标的时候，就意味着你必须付出比别人多得多的努力。");
        text2.setText("金钱有一些秘密和规律，要想了解这些秘密和规律，前提条件是，你自己必须真的有这个愿望。");
        text3.setText("一个人把精力集中在自己所能做的，知道的和拥有的东西上的那一天起，他的成功就已经拉开了序幕。这也使得一个孩子完全有能力比成人挣到更多的钱。");

        viewList.add(view1);
        viewList.add(view2);
        viewList.add(view3);


        //mHandler = new Handler();
        mViewPagerAdapter viewPagerAdapter = new mViewPagerAdapter(viewList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (position == 4) {
                            viewPager.setCurrentItem(1, false);//不要动画
                        }
                    }
                }, 200);//延后执行，改善视觉效果。
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    /**
     * ViewPager的定时器
     */
    Runnable runnableForViewPager = new Runnable() {
        @Override
        public void run() {
            try {
                itemPosition++;
                mHandler.postDelayed(this, TIME);
                viewPager.setCurrentItem(itemPosition % mCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
