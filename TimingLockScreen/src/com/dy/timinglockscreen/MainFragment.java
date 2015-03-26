package com.dy.timinglockscreen;

import java.nio.channels.GatheringByteChannel;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.dy.timinglockscreen.service.TimerService;

import android.R.anim;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainFragment extends Fragment {
	
	private static MainFragment currentFragment;
	private DevicePolicyManager policyManager;  
	private ComponentName componentName;
	private static Timer timer;
	private Integer[] hourArr=null;
	private Integer[] minuteArr=null;
	private Spinner spinner_hour;
	private Spinner spinner_minute;
	private boolean isEnabled=false;
	private Button btn_lockscreen;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		currentFragment=this;
		//��ȡ�豸�������  
        policyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);  
        //AdminReceiver �̳��� DeviceAdminReceiver  
        componentName = new ComponentName(getActivity(), MyDeviceAdminReceiver.class);  
		
        btn_lockscreen=(Button) getView().findViewById(R.id.btn_lockscreen);
		btn_lockscreen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				clickEnableButton();
			}
		});
		hourArr=new Integer[25];
		for (int i = 0; i < hourArr.length; i++) {
			hourArr[i] = i;
		}
		minuteArr=new Integer[60];
		for (int j = 0; j < minuteArr.length; j++) {
			minuteArr[j]=j;
		}
		//Сʱ
		spinner_hour=(Spinner) getView().findViewById(R.id.spinner_hour);
		ArrayAdapter<Integer> hour_adapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, hourArr);
		hour_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_hour.setAdapter(hour_adapter);
		//����
		spinner_minute=(Spinner) getView().findViewById(R.id.spinner_minute);
		ArrayAdapter<Integer> minute_adapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, minuteArr);
		minute_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_minute.setAdapter(minute_adapter);
		
		initSetTime();
		
	}
	
	public void activeManage() {  
        // �����豸����(��ʽIntent) - ��AndroidManifest.xml���趨��Ӧ������  
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);  
        //Ȩ���б�  
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);  
        //����(additional explanation)  
//        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "------ �������� ------");  
        startActivityForResult(intent, 0);  
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==Activity.RESULT_OK){
			//������ʱ����
			Intent intent=new Intent(getActivity(), TimerService.class);
			intent.putExtra(MyApplication.KEY_TIMER_ACTION, MyApplication.TIMER_ACTION_START);
			getActivity().startService(intent);
		}else if(resultCode==Activity.RESULT_CANCELED){
			//����
			saveSetTime(0);
		}
	}
	
	/**
	 * ��ȡ���õ��ܷ�����
	 * @return
	 */
	private int getSettingTime(){
		int hour = 0;
		int minute = 0;
		try {
			hour = spinner_hour.getSelectedItemPosition();
			minute = spinner_minute.getSelectedItemPosition();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hour * 60 + minute;
	}
	
	/**
	 * ���涨ʱʱ��
	 * @param hour
	 * @param minute
	 */
	private void saveSetTime(int time){
		
		try{
			SharedPreferences preferences=getActivity().getSharedPreferences(MyApplication.KEY_SETTIME, Activity.MODE_PRIVATE);
			Editor editor=preferences.edit();
			editor.putInt(MyApplication.KEY_SETTIME_HOUR, time>60?((time-(time%60))/60):0);
			editor.putInt(MyApplication.KEY_SETTIME_MINUTE, time%60);
			editor.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ������õĶ�ʱʱ��
	 * @return
	 */
	private int getSaveSetTime(){
		
		int time=0;
		try{
			SharedPreferences preferences=getActivity().getSharedPreferences(MyApplication.KEY_SETTIME, Activity.MODE_PRIVATE);
			int hour=preferences.getInt(MyApplication.KEY_SETTIME_HOUR, 0);
			int minute=preferences.getInt(MyApplication.KEY_SETTIME_MINUTE, 0);
			time=hour*60+minute;
		}catch(Exception e){
			e.printStackTrace();
		}
		return time;
	}
	
	/**
	 * �����ť
	 */
	private void clickEnableButton(){
		
		boolean active = policyManager.isAdminActive(componentName);
		if (!active) {// ����Ȩ��
			activeManage();// ȥ���Ȩ��
		}else{
			if(TimerService.isEnableLockScreenTimer()){
				Intent intent=new Intent(getActivity(), TimerService.class);
				intent.putExtra(MyApplication.KEY_TIMER_ACTION, MyApplication.TIMER_ACTION_STOP);
				getActivity().startService(intent);
			}else{
				//�������õĶ�ʱʱ��
				saveSetTime(getSettingTime());
				Intent intent=new Intent(getActivity(), TimerService.class);
				intent.putExtra(MyApplication.KEY_TIMER_ACTION, MyApplication.TIMER_ACTION_START);
				getActivity().startService(intent);
			}
		}
		
		
	}
	
	private void initSetTime(){
		
		try{
			int time=getSaveSetTime();
			int hour=time>60?((time-(time%60))/60):0;
			int minute=time%60;
			spinner_hour.setSelection(hour);
			spinner_minute.setSelection(minute);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ��ʼ����ť
	 */
	public void initEnableButton(){
		
		try{
			if(TimerService.getInstance().isEnableLockScreenTimer()){
				isEnabled=true;
			}else{
				isEnabled=false;
			}
			if(btn_lockscreen!=null){
				if(isEnabled){
					btn_lockscreen.setText(R.string.stop);
				}else{
					btn_lockscreen.setText(R.string.start);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ��ȡʵ��
	 * @return
	 */
	public static MainFragment getInstance(){
		
		return currentFragment;
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		currentFragment=null;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try{
			initEnableButton();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
