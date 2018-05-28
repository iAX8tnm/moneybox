package com.example.moneybox;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Handler;


import static com.example.moneybox.MainActivity.fileIsExists;
import static com.example.moneybox.parseData.parseStringDateToMillis;


public class ChartActivity extends AppCompatActivity {

    LineChart mchart;
    List<Entry> entries = new ArrayList<Entry>();

    List<View> viewList = new ArrayList<View>();
    private ViewPager viewPager;
    private int itemPosition;
    private int mCount = 3;


    private mDatabaseHelper dbHelper = new mDatabaseHelper(this, "Deposit.db", null, 2);

    private static final int TIME = 3500;
    private Handler mHandler = new Handler();
    private static final String TAG = "ChartActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        if (fileIsExists(this.getApplication().getFilesDir().getParentFile().getPath()+"/databases/Deposit.db")) {
            initChartData();
            initChartView();
        }

        //mchart.invalidate(); // refresh


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("存钱曲线");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        initViewPager();

        mHandler.postDelayed(runnableForViewPager, TIME);

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


    /**
     * Init Viewpager
     */

    private void initViewPager() {
        LayoutInflater layoutInflater = getLayoutInflater().from(ChartActivity.this);
        viewPager = findViewById(R.id.viewpager_of_chart);

        View view1 = layoutInflater.inflate(R.layout.someknowledge1, null);
        View view2 = layoutInflater.inflate(R.layout.someknowledge2, null);
        View view3 = layoutInflater.inflate(R.layout.someknowledge3, null);

        TextView text1= view1.findViewById(R.id.tv_knowledge_tip1);
        TextView text2= view2.findViewById(R.id.tv_knowledge_tip2);
        TextView text3= view3.findViewById(R.id.tv_knowledge_tip3);

        text1.setText("把钱分成日常开销、梦想目标和金额账户三部分。");
        text2.setText("确立最重要的目标。为什么我们必须特别强调在我们“长长的愿望目录中”的某几个目标。");
        text3.setText("一开始，我们必须明确金钱对您的意义。");

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


    /**
     * 从手机本地提取数据
     */
    private void initChartData() {
        int values = 0;
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query("DailyDeposit",null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String updateDate = cursor.getString(cursor.getColumnIndex("updateDate"));
                    int value = cursor.getInt(cursor.getColumnIndex("value"));
                    Log.d(TAG, "getDataFromDatabase: " + updateDate +" " + value);

                    values = values + value;
                    entries.add(new Entry(parseStringDateToMillis(updateDate), values));

                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getDataFromDatabase: I get an Error at method initChartData()");
        }

    }

    private void initChartView() {
        mchart = findViewById(R.id.chart);
        LineDataSet dataSet = new LineDataSet(entries, "时间");
        //线模式为圆滑曲线（默认折线）
        //dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setColor(Color.parseColor("#FFFFD700"));
        dataSet.setCircleColor(Color.parseColor("#FFFFD700"));
        dataSet.setDrawFilled(true);
        Drawable gradientDrawable = ContextCompat.getDrawable(this, R.drawable.fade_yellow);
        dataSet.setFillDrawable(gradientDrawable);


        dataSet.setDrawCircleHole(true);
        dataSet.setCircleColorHole(Color.parseColor("#FFFFD700"));
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setDrawVerticalHighlightIndicator(false);
        dataSet.setHighLightColor(Color.YELLOW);

        LineData lineData = new LineData(dataSet);
        mchart.setData(lineData);
        // 不显示数据描述
        mchart.getDescription().setEnabled(false);
        // 没有数据的时候，显示“暂无数据”
        mchart.setNoDataText("暂无数据");
        // 不显示表格颜色
        mchart.setDrawGridBackground(false);
        // 设置Y轴不可以缩放
        mchart.setScaleYEnabled(false);
        // 不显示y轴右边的值
        mchart.getAxisRight().setEnabled(false);
        // Y轴标签数三个
        mchart.getAxisLeft().setLabelCount(4, false);
        //禁止通过在其上双击缩放图表
        mchart.setDoubleTapToZoomEnabled(false);
        // 不显示图例
        Legend legend = mchart.getLegend();
        legend.setEnabled(false);
        // 向左偏移15dp，抵消y轴向右偏移的30dp
        mchart.setExtraLeftOffset(-5);

        XAxis xAxis = mchart.getXAxis();
        // 设置x轴数据的位置
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setTextSize(12);
        xAxis.setGridColor(Color.parseColor("#30FFFFFF"));
        // 设置x轴数据偏移量
        xAxis.setYOffset(0);
       // xAxis.setXOffset(0);

        xAxis.setGranularity(86400000);//以天为单位
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            private SimpleDateFormat mFormat = new SimpleDateFormat("M月d");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mFormat.format(new Date((long) value));
            }
        });

        YAxis yAxis = mchart.getAxisLeft();
        // 不显示y轴
        yAxis.setDrawAxisLine(false);
        // 设置y轴数据的位置
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        // 不从y轴发出横向直线
        //yAxis.setDrawGridLines(false);
        yAxis.setTextColor(Color.GRAY);
        yAxis.setTextSize(12);
        // 设置y轴数据偏移量
        yAxis.setXOffset(20);
        yAxis.setYOffset(0);
        yAxis.setAxisMinimum(2);

        yAxis.setValueFormatter(new IAxisValueFormatter() {
            String mFormat = new String("¥");
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (mFormat+value);
            }
        });
    }

}
