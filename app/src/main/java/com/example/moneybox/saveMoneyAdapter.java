package com.example.moneybox;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by mumumushi on 18-3-12.
 */

public class saveMoneyAdapter extends RecyclerView.Adapter<saveMoneyAdapter.ViewHolder> {

    private List<SaveMoney> mSaveMoneyList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View saveMoneyView;
        TextView date;
        TextView money;

        public ViewHolder(View view) {
            super(view);
            saveMoneyView = view;
            date = view.findViewById(R.id.tv_date);
            money = view.findViewById(R.id.tv_money);
        }
    }

    public saveMoneyAdapter(List<SaveMoney> saveMoneyList) {
        mSaveMoneyList = saveMoneyList;
    }

    @Override
    public saveMoneyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        holder.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                SaveMoney saveMoney = mSaveMoneyList.get(position);
                Log.d(TAG, "onClick: will replace with change date dialog");
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SaveMoney saveMoney = mSaveMoneyList.get(position);
        holder.date.setText(saveMoney.getUpdateDate());
        holder.money.setText("¥" + Integer.toString(saveMoney.getValue()));
    }

    @Override
    public int getItemCount() {
        return mSaveMoneyList.size();
    }
}
