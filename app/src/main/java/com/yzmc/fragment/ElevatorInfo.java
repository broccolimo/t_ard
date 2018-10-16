package com.yzmc.fragment;

/**
 * @author jy
 * @date 2018-07-04
 * @description 维保单电梯信息
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.yzmc.R;
import com.yzmc.util.AFCallBack;
import com.yzmc.util.SerializableMap;
import java.util.Map;

public class ElevatorInfo extends Fragment {

    private Map<String, Object> elevatorInfo;
    private MyListener1 listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.elevator_info, null);

        ((TextView)view.findViewById(R.id.useUnitID)).setText(String.valueOf(elevatorInfo.get("useUnitID")));
        ((TextView)view.findViewById(R.id.productID)).setText(String.valueOf(elevatorInfo.get("productID")));
        ((TextView)view.findViewById(R.id.makeUnit)).setText(String.valueOf(elevatorInfo.get("makeUnit")));
        ((TextView)view.findViewById(R.id.installUnit)).setText(String.valueOf(elevatorInfo.get("installUnit")));
        ((TextView)view.findViewById(R.id.repairUnit)).setText(String.valueOf(elevatorInfo.get("repairUnit")));
        ((TextView)view.findViewById(R.id.maintainUnit)).setText(String.valueOf(elevatorInfo.get("maintainUnit")));
        ((TextView)view.findViewById(R.id.elevatorModel)).setText(String.valueOf(elevatorInfo.get("elevatorModel")));
        ((TextView)view.findViewById(R.id.tractionMachineModel)).setText(String.valueOf(elevatorInfo.get("tractionMachineModel")));
        ((TextView)view.findViewById(R.id.tractionMachine)).setText(String.valueOf(elevatorInfo.get("num")) + "/" + String.valueOf(elevatorInfo.get("diameter")));
        ((TextView)view.findViewById(R.id.motorPower)).setText(String.valueOf(elevatorInfo.get("motorPower")));
        ((TextView)view.findViewById(R.id.controlBox)).setText(String.valueOf(elevatorInfo.get("controlBox")));
        ((TextView)view.findViewById(R.id.brakes)).setText(String.valueOf(elevatorInfo.get("brakes")));
        ((TextView)view.findViewById(R.id.speedLimiter)).setText(String.valueOf(elevatorInfo.get("speedLimiter")));
        ((TextView)view.findViewById(R.id.safetyGear)).setText(String.valueOf(elevatorInfo.get("safetyGear")));
        ((TextView)view.findViewById(R.id.doorLock)).setText(String.valueOf(elevatorInfo.get("doorLock")));
        ((TextView)view.findViewById(R.id.ratedSpeed)).setText(String.valueOf(elevatorInfo.get("ratedSpeed")));
        ((TextView)view.findViewById(R.id.ratedLoad)).setText(String.valueOf(elevatorInfo.get("ratedLoad")));
        ((TextView)view.findViewById(R.id.layers)).setText(elevatorInfo.get("floor") + "层" + elevatorInfo.get("station") + "站" + elevatorInfo.get("door") + "门");
        ((TextView) view.findViewById(R.id.transformModel)).setText(String.valueOf(elevatorInfo.get("transformModel")));
        ((TextView) view.findViewById(R.id.annualInspection)).setText(String.valueOf(elevatorInfo.get("annualInspection")));

        listener.sendMessage1(0);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        SerializableMap map = (SerializableMap) bundle.get("map");
        elevatorInfo = map.getMap();
        listener = (MyListener1) getActivity();
    }


    public interface MyListener1{
        abstract void sendMessage1(int code);
    }
}
