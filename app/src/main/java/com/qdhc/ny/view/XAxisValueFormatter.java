package com.qdhc.ny.view;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.qdhc.ny.bmob.Area_Region;

import java.util.List;

/**
 * @Author wj
 * @Date 2020/1/6
 * @Desc
 * @Url http://www.chuangze.cn
 */
public class XAxisValueFormatter implements IAxisValueFormatter {
    List<Area_Region> area_regions;

    public XAxisValueFormatter(List<Area_Region> area_regions) {
        this.area_regions = area_regions;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        int position = (int) value;
        if (area_regions != null && area_regions.size() > position) {
            return area_regions.get(position).getName();
        }
        return "未知";
    }

}