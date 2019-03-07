package com.example.moneybox;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.moneybox.MainActivity.fileIsExists;
import static com.example.moneybox.util.DateUtil.getCurrentDate;
import static com.example.moneybox.util.DateUtil.getTime;
import static com.example.moneybox.util.DateUtil.getTodayDate;
import static com.example.moneybox.util.DateUtil.getTomorrowDate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "MainActivity";
    List<SaveMoney> saveMoneyList = new ArrayList<>();
    private mDatabaseHelper dbHelper = new mDatabaseHelper(MainActivity.this, "Deposit.db", null, 2);
    saveMoneyAdapter adapter = new saveMoneyAdapter(saveMoneyList);
    String lastRequestDate = "2019-1-1 00:00:00";
    int TotalVal = 0;
    int PlanGoal = 0;
    int hasNewData = 0;
    ScheduledExecutorService mUpdateDataThreadPool = Executors.newScheduledThreadPool(1);

    OkSingleton client = OkSingleton.getInstance();
    ExecutorService taskThreadPool = ThreadPoolSingleton.getThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (fileIsExists(this.getApplication().getFilesDir().getParentFile().getPath()+"/databases/Deposit.db")) {  //先获取本地数据库
            Log.d(TAG, "onCreate: SQL exit! get data from database");
            getDataFromDatabase();
            SharedPreferences pref  = getSharedPreferences("data", MODE_PRIVATE);
            lastRequestDate = pref.getString("LastRequestDate", "2019-1-1 00:00:00");
            TotalVal = pref.getInt("TotalVal", 0);
        }
        else Log.d(TAG, "onCreate: SQL is not exit!");

        //getDataFromLW5(lastRequestDate.substring(0, lastRequestDate.indexOf(" ")));


        //后期计划添加splash启动页面
        //可收缩的Toolbar的显示设置
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        mCollapsingToolbarLayout.setTitle(TotalVal + "元");


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //RecyclerView显示的设置，adapter定义了在MainActivity.class上
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);


        startUpdateDataThreadPool();   //开启接收数据线程池
        //TODO:除了定时UpdateData的线程池，还需要一个普通的线程池执行一般任务

        Log.d(TAG, "onCreate: execute");
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: stopppppppp");
        //暂停定时任务
        super.onStop();
    }

    @Override
    protected void onRestart() {
        //重启定时任务
        SharedPreferences pref  = getSharedPreferences("data", MODE_PRIVATE);
        //FIXME:这个hasWithdrawMoney好像没有用到。。。。。。。。
        boolean hasWithdrawMoney = pref.getBoolean("hasWithdrawMoney", false);
        if (hasWithdrawMoney) {
            hasWithdrawMoney = false;
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
            editor.putBoolean("hasWithdrawMoney", hasWithdrawMoney);
            editor.apply();


            if (fileIsExists(MainActivity.this.getApplication().getFilesDir().getParentFile().getPath()+"/databases/Deposit.db")) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor cursor = db.query("Deposit", null, null, null, null, null, null);
                if (cursor.moveToLast()) {
                    SaveMoney saveMoney = new SaveMoney();
                    saveMoney.setUpdateDate(cursor.getString(cursor.getColumnIndex("updateDate")));
                    saveMoney.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateDate")));
                    saveMoney.setValue(cursor.getInt(cursor.getColumnIndex("value")));
                    saveMoneyList.add(0, saveMoney);
                }
                cursor.close();
            }

            TotalVal = pref.getInt("TotalVal", 0);
            lastRequestDate = pref.getString("LastRequestDate", "2019-1-1 00:00:00");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);
                    mCollapsingToolbarLayout.setTitle(TotalVal + "元");

                    adapter.notifyDataSetChanged();
                }
            });

        }
        super.onRestart();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_is_connect, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 把它改成一个显示手机是否联网的把
     * @param menu 待初始化的menu item
     * @return 完成为真
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.item_main_is_connect).setIcon(R.drawable.ic_disconnect);


        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * 这里用来判断，右上角太阳
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_main_is_connect: {//这是那个太阳


                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_chart) {
            Intent intent = new Intent(this, ChartActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_plan) {
            SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
            if (pref.getBoolean("hasSetPlan", false)) {
                Intent intent = new Intent(this, PlanActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, PlanSetActivity.class);
                startActivity(intent);
            }

        } else if (id == R.id.nav_lock) {
            Intent intent = new Intent(this, UnlockActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * 开启定时查看有没有新数据的线程池
     */
    public void startUpdateDataThreadPool() {

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        PlanGoal = pref.getInt("PlanGoal", 0);


        Log.d(TAG, "onCreate: starting ScheduledExecutorService. update data every 3 Seconds");

        mUpdateDataThreadPool.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: start time: " + lastRequestDate + "............................");
                getDataFromLW5(lastRequestDate.substring(0, lastRequestDate.indexOf(" ")));
                Log.d(TAG, "run: end.......................................................");

            }
        }, 0, 3, TimeUnit.SECONDS);
    }



    /**
     * 从乐联网(LEWEI50)获取数据，发送Http Request时加上请求数据的开始日期以及截止日期，并添加userkey的property
     * @param startTime 获取从上一次请求后的那天（startTime）开始,到今天的数据
     */
    public void getDataFromLW5(String startTime) {
        //乐联网的连接地址
        final String url_getHistoryData = "http://www.lewei50.com/api/v1/sensor/gethistorydata/63012";
        //乐联网的用户键值
        final String userkey = "6409dc66bbaf4c7592dc7e30d2337311";

        final Request request = new Request.Builder()
                .url(url_getHistoryData + "?StartTime=" + startTime + "&EndTime=" + getTomorrowDate())
                .addHeader("userkey", userkey)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String r = response.body().string();
            Log.d(TAG, "onFinish: request new data from LEWEI50.com finish. ");

            parseDataFromJSON(r);    //解析返回的json文件
            // adapter.notifyDataSetChanged();  //更新RecyclerView

            //把数据存入数据库中
            storeLW5Data();

        }catch (Exception e) {
            Log.d(TAG, "onError: I get an Error at method getDataFromLW5()");
            e.printStackTrace();
        }

    }



    /**
     * 解析从乐联网服务器上获得的json文件并saveMoneyList中
     * @param json json 是从乐联网上得到的原始数据，经过预处理去除头尾，利用JSONArray储存
     */
    private void parseDataFromJSON(String json) {

        String jsonData = json.substring(json.indexOf("[{\"updateTime\""),json.lastIndexOf(",\"Successful\""));

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        String lastUpdateTime = pref.getString("LastRequestDate", getCurrentDate());
        hasNewData = 0;
        try {

            JSONArray jsonArray = new JSONArray(jsonData);
            int index = 0;
            int N = jsonArray.length();
            for (int i = N-1; i >= 0; i--) {

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String updateTime = jsonObject.getString("updateTime");
                String value = jsonObject.getString("value");

                //Log.d(TAG, "updateTime is " + updateTime + "lastRequestDate is " + lastRequestDate);
                if (!updateTime.equals(lastUpdateTime)) {
                    hasNewData++;
                    if (i == (N-1)) {
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putString("LastRequestDate", updateTime);   //储存的上次更新的日期，方便下一次启动时从新的日期开始获取获取数据
                        Log.d(TAG, "LastRequestDate " + updateTime + " is saved at SharedPreferences data.xml");
                        editor.apply();
                        lastRequestDate = updateTime;
                    }
                    //Log.d(TAG, "parseDataFromJSON: " + updateTime);

                    SaveMoney saveMoney = new SaveMoney();
                    saveMoney.setUpdateDate(updateTime.substring(0, updateTime.indexOf(" ")));
                    saveMoney.setUpdateTime(updateTime.substring(updateTime.indexOf(" ")+1, updateTime.length()-1));
                    saveMoney.setValue(value);

                    saveMoneyList.add(index, saveMoney);
                    index++;
                } else {
                    Log.d(TAG, "parseDataFromJSON: no new data");
                    break;
                }
            }
            //adapter.notifyDataSetChanged();  //更新RecyclerView
        } catch(Exception e) {
            e.printStackTrace();
            Log.d(TAG, "parseJSON: I get an Error at method parseDataFromJSON()");
        }
    }


    private void storeLW5Data() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String TmpDate = "2019/1/1";
        int TmpVal = 0;
        int TmpHasNewData = hasNewData;
        if (hasNewData != 0) {
            for (int i = hasNewData-1; i >= 0; i--) {
                if (hasNewData == 0)
                    break;
                else {
                    values.put("updateDate", saveMoneyList.get(i).getUpdateDate());
                    values.put("updateTime", saveMoneyList.get(i).getUpdateTime());
                    values.put("value", saveMoneyList.get(i).getValue());
                    //Log.d(TAG, "run: .... "+saveMoneyList.get(i).getUpdateDate()+" "+saveMoneyList.get(i).getUpdateTime()+" "+saveMoneyList.get(i).getValue());
                    db.insert("Deposit", null, values);
                    values.clear();
                    hasNewData--;

                    TmpVal = TmpVal + saveMoneyList.get(i).getValue();
                }
            }
            Log.d(TAG, "storeLW5Data: Already save all new data to Deposit.db");

            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
            //SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
            //int lastTotalVal = pref.getInt("TotalVal", 0);
            TotalVal = TmpVal + TotalVal;
            editor.putInt("TotalVal", TotalVal);   //储存总金额，方便下一次启动时显示
            editor.apply();

            //Log.d(TAG, "run: TotalVal " + TotalVal +" is saved at SharedPreferences data.xml");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {                                        //更新UI
                    CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);
                    mCollapsingToolbarLayout.setTitle(TotalVal + "元");

                    adapter.notifyDataSetChanged();
                }
            });
            TmpVal = 0;
        }



        hasNewData = TmpHasNewData;
        if (hasNewData != 0) {
            for (int i = hasNewData-1; i >= 0; i--) {
                if (hasNewData == 0)
                    break;
                else {

                    //Log.d(TAG, "run: saving data to DailyDeposit " + saveMoneyList.get(i).getUpdateDate() + " " + saveMoneyList.get(i).getValue());
                    if (TmpDate.equals(saveMoneyList.get(i).getUpdateDate())) {
                        TmpVal = TmpVal + saveMoneyList.get(i).getValue();
                    }
                    else if (TmpDate.equals("2019/1/1")) {    //获取第一个日期，应该为最新日期开始
                        TmpDate = saveMoneyList.get(i).getUpdateDate();
                        TmpVal = TmpVal + saveMoneyList.get(i).getValue();
                    }
                    else {
                        values.put("updateDate", TmpDate);
                        values.put("value", TmpVal);
                        db.insert("DailyDeposit", null, values);
                        values.clear();

                        hasNewData--;

                        TmpDate = saveMoneyList.get(i).getUpdateDate();
                        TmpVal = saveMoneyList.get(i).getValue();
                    }
                }
            }
            //因以上写法在历遍完之后，最后一个日期是没有存进数据库的，所以下面进行单独操作
            if (TmpHasNewData != 0) {
                values.put("updateDate", TmpDate);
                values.put("value", TmpVal);
                // SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                Cursor cursor = db.query("DailyDeposit",null, null, null, null, null, null);
                if (cursor.moveToLast()) {
                    String lastDate = cursor.getString(cursor.getColumnIndex("updateDate"));
                    int lastValue = cursor.getInt(cursor.getColumnIndex("value"));


                    if (TmpDate.equals(lastDate)) {           //判断一下是否这个日期是否已经在数据库DailyDeposit里有储存,有的话则合并更新
                        Log.d(TAG, "run: " + TmpDate + " is equals to " + lastDate);

                        values.clear();
                        values.put("value", (TmpVal + lastValue));
                        db.update("DailyDeposit", values, "updateDate=?", new String[]{TmpDate} );
                        //上面这条语句是更新table DailyDeposit 的updateDate = TmpDate 的值成value.
                        //editor.putInt("LastDailyDepositValue", TmpVal + lastValue);
                        // Log.d(TAG, "run:         " + (TmpVal + lastValue));
                        // editor.apply();

                    } else {                                  //没有则直接想最后插入
                        db.insert("DailyDeposit", null, values);

                        // editor.putInt("LastDailyDepositValue", TmpVal);
                        //  editor.apply();
                    }
                }
                cursor.close();
                values.clear();
                Log.d(TAG, "onFinish: Already save all new data to DailyDeposit.db");

            }

        }
    }

    /**
     * 从手机本地提取数据
     */
    public void getDataFromDatabase() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.query("Deposit",null, null, null, null, null, null);
            if (cursor.moveToLast()) {
                do {
                    String updateDate = cursor.getString(cursor.getColumnIndex("updateDate"));
                    String updateTime = cursor.getString(cursor.getColumnIndex("updateTime"));
                    int value = cursor.getInt(cursor.getColumnIndex("value"));
                    //然后填充saveMoneyList
                    SaveMoney saveMoney = new SaveMoney();
                    saveMoney.setUpdateDate(updateDate);
                    saveMoney.setUpdateTime(updateTime);
                    saveMoney.setValue(value);
                    saveMoneyList.add(saveMoney);
                } while (cursor.moveToPrevious());
            }
            cursor.close();
            adapter.notifyDataSetChanged();  //更新RecyclerView
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getDataFromDatabase: I get an Error at method getDataFromDatabase()");
        }

    }

    /**
     * 检查文件是否存在
     * @param strFile 需要检查的路径文件
     * @return 若存在该文件则返回真
     */
    public static boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            { return false; }
        }
        catch (Exception e)
        { return false; }
        return true;
    }
}
