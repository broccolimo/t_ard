package com.yzmc.util;

import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.yzmc.R;

public class MyItem implements ClusterItem {
    private final LatLng position;

    public MyItem(LatLng latLng){
        this.position = latLng;
    }
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor() {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location);
    }
}
