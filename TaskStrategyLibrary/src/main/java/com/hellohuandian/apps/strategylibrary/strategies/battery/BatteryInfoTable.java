package com.hellohuandian.apps.strategylibrary.strategies.battery;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-26
 * Description:
 */
public final class BatteryInfoTable extends ConcurrentHashMap<String, String>
{
    public BatteryInfoTable()
    {
        init();
    }

    private void init()
    {
        // TODO: 2019-09-26 电池容量规格 
        put("0A", "48/12");
        put("0B", "48/12.5");
        put("0C", "48/13");
        put("0D", "48/13.5");
        put("0E", "48/14");
        put("0F", "48/14.5");
        put("0G", "48/15");
        put("0H", "48/15.5");
        put("0I", "48/16");
        put("0J", "48/16.5");
        put("0K", "48/17");
        put("0L", "48/17.5");
        put("0M", "60/16");
        // TODO: 2019-09-26 BMS生产厂家 
        put("1A", "艾启");
        put("1B", "博强");
        put("1C", "超力源");
        put("1D", "诺万");
        put("1E", "捷敏科");
        // TODO: 2019-09-26 Pack生产厂家 = 生产线识别码 = 电芯生产厂家
        put("2A", "华富");
        put("2B", "南都");
        put("2C", "国轩");
        put("2D", "芯驰");
        put("2E", "乐嘉");
        put("2F", "鹏辉");
        put("2G", "沃泰通");
        put("2H", "洁能劲");
        // TODO: 2019-09-26 ：年
        put("5F", "2018");
        put("5G", "2019");
        put("5H", "2020");
        put("5I", "2021");
        put("5J", "2022");
        // TODO: 2019-09-26 ：月
        put("6A", "1");
        put("6B", "2");
        put("6C", "3");
        put("6D", "4");
        put("6E", "5");
        put("6F", "6");
        put("6G", "7");
        put("6H", "8");
        put("6I", "9");
        put("6J", "10");
        put("6K", "11");
        put("6L", "12");
    }

    public String match(int index, char mark)
    {
        switch (index)
        {
            case 2:
            case 3:
            case 4:
                return get("2" + mark);
            default:
                return get("" + index + mark);
        }
    }
}
