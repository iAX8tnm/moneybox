package com.example.moneybox;

import java.io.Serializable;

/**
 * Created by mumumushi on 18-3-9.
 */

public class SaveMoney implements Serializable {
    private String updateDate;
    private String updateTime;

    private int value;

    public String getUpdateDate()                 { return updateDate;                          }
    public String getUpdateTime()                 { return updateTime;                          }
    public int getValue()                         { return value;                               }
    public void setUpdateDate(String updateDate)  { this.updateDate = updateDate;               }
    public void setUpdateTime(String updateTime)  { this.updateTime = updateTime;               }
    public void setValue(String value)            { this.value = (int) Float.parseFloat(value); }
    public void setValue(int value)               { this.value = value;                         }
}
