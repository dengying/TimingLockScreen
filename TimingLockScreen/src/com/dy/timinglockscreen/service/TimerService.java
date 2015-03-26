package com.dy.timinglockscreen.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.dy.timinglockscreen.MainActivity;
import com.dy.timinglockscreen.MainFragment;
import com.dy.timinglockscreen.MyApplication;
import com.dy.timinglockscreen.MyDeviceAdminReceiver;
import com.dy.timinglockscreen.R;

public class TimerService extends Service {

	private DevicePolicyManager policyManager;  
	private ComponentName componentName;
	private Timer timer;
	private static TimerService currentInstance;
	private Handler handler=new Handler();
	private NotificationManager noticeManager;
	private final int NoticeID=20001;
	public static boolean isEnableTimer=false;
	private long startTime=0;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		currentInstance=this;
		//��ȡ�豸�������  
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        //AdminReceiver �̳��� DeviceAdminReceiver  
        componentName = new ComponentName(this, MyDeviceAdminReceiver.class);  
        //֪ͨ����
        noticeManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        startTime=getSaveStartTime();
		isEnableTimer=getSaveIsTimer();
		System.out.println("�Ƿ�ʱ��"+isEnableTimer);
		if(isEnableTimer){
			startTimerLockScreen();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		try{
			if(intent!=null){
				int action=intent.getIntExtra(MyApplication.KEY_TIMER_ACTION, -1);
				switch(action){
					case MyApplication.TIMER_ACTION_START:
						startTimerLockScreen();
						break;
					case MyApplication.TIMER_ACTION_STOP:
						stopTimerLockScreen();
						break;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * ����
	 */
	private void lockScreen(){  
		boolean active = policyManager.isAdminActive(componentName);
		if (!active) {// ����Ȩ��
//			activeManage();// ȥ���Ȩ��
			if(MainFragment.getInstance()!=null){
				MainFragment.getInstance().activeManage();
			}
		}else{
			policyManager.lockNow();// ֱ������
		}
	}  
	
	/**
	 * ������ʱ������Ļ
	 */
	private void startTimerLockScreen(){
		
		try{
			if(timer!=null){
				timer.cancel();
				timer=null;
			}
			boolean active = policyManager.isAdminActive(componentName);
			if (!active) {// ����Ȩ��
//				activeManage();// ȥ���Ȩ��
				if(MainFragment.getInstance()!=null){
					MainFragment.getInstance().activeManage();
				}
				return;
			}
			int setTime=getSaveSetTime();
			if(setTime>0){
				System.out.println("������ʱ");
				Calendar calendar=Calendar.getInstance();
				if(startTime>0){
					Date startDate=new Date(startTime);
					calendar.setTime(startDate);
				}else{
					Date startDate=new Date();
					startTime=startDate.getTime();
					calendar.setTime(startDate);
				}
				calendar.add(Calendar.MINUTE, setTime);
				timer=new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						System.out.println("��ʱ�С�����");
						try{
							if(MyApplication.lockScreenDate!=null && MyApplication.lockScreenDate.before(new Date())){
								lockScreen();
								stopTimerLockScreen();
							}else{
								showTimerNotice();
							}
							//���ð�ť
							handler.post(new Runnable() {
								
								@Override
								public void run() {
									if(MainFragment.getInstance()!=null){
										MainFragment.getInstance().initEnableButton();
									}
								}
							});
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}, 0, 5000);
				isEnableTimer=true;
				MyApplication.lockScreenDate=calendar.getTime();
				MyApplication.TimerLockScreenMinute=setTime;
				saveSetTime(MyApplication.TimerLockScreenMinute, startTime);
				saveIsTimer(true);
			}else{
				Toast.makeText(this, R.string.noset_time, Toast.LENGTH_SHORT).show();
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ֹͣ��ʱ������Ļ
	 */
	private void stopTimerLockScreen(){
		if(timer!=null){
			timer.cancel();
			timer=null;
		}
		isEnableTimer=false;
		MyApplication.lockScreenDate=null;
		cancelTimerNotice();
		if(MainFragment.getInstance()!=null){
			MainFragment.getInstance().initEnableButton();
		}
		saveIsTimer(false);
	}
	
	/**
	 * ���涨ʱʱ��
	 * @param hour
	 * @param minute
	 */
	private void saveSetTime(int time, long startTime){
		
		try{
			SharedPreferences preferences=getSharedPreferences(MyApplication.KEY_SETTIME, Activity.MODE_PRIVATE);
			Editor editor=preferences.edit();
			editor.putInt(MyApplication.KEY_SETTIME_HOUR, time>60?((time-(time%60))/60):0);
			editor.putInt(MyApplication.KEY_SETTIME_MINUTE, time%60);
			editor.putLong(MyApplication.KEY_SETTIME_STARTTIME, startTime);
			editor.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ������õĶ�ʱ�ܷ���
	 * @return
	 */
	private int getSaveSetTime(){
		
		int time=0;
		try{
			SharedPreferences preferences=getSharedPreferences(MyApplication.KEY_SETTIME, Activity.MODE_PRIVATE);
			int hour=preferences.getInt(MyApplication.KEY_SETTIME_HOUR, 0);
			int minute=preferences.getInt(MyApplication.KEY_SETTIME_MINUTE, 0);
			time=hour*60+minute;
		}catch(Exception e){
			e.printStackTrace();
		}
		return time;
	}
	
	/**
	 * ������ö�ʱ��ʼʱ��
	 * @return
	 */
	private long getSaveStartTime(){
		
		long time=0;
		try{
			SharedPreferences preferences=getSharedPreferences(MyApplication.KEY_SETTIME, Activity.MODE_PRIVATE);
			time=preferences.getLong(MyApplication.KEY_SETTIME_STARTTIME, 0);
		}catch(Exception e){
			e.printStackTrace();
		}
		return time;
	}
	
	/**
	 * ȡ����ʱ֪ͨ
	 */
	private void cancelTimerNotice(){
		if(noticeManager!=null){
			noticeManager.cancel(NoticeID);
		}
	}
	
	private void showTimerNotice(){
		try{
			Notification timerNotice=new Notification();
			timerNotice.flags|=Notification.FLAG_NO_CLEAR ;
//			timerNotice.defaults = Notification.DEFAULT_SOUND;  
			timerNotice.icon=R.drawable.app_ico;
			Intent intent=new Intent(TimerService.this, MainActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent=PendingIntent.getActivity(TimerService.this, 0, intent, 0);
			timerNotice.setLatestEventInfo(this, getResources().getString(R.string.app_name), getResources().getString(R.string.nextlock_lefttime)+getDistanceTime(new Date(), MyApplication.lockScreenDate), pIntent);
	        if(noticeManager!=null){
	            noticeManager.notify(NoticeID, timerNotice);
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/** 
     * ����ʱ�����������Сʱ���ٷ�
     * @param one ǰ
     * @param two ��
     * @return String ����ֵΪ��xxСʱxx��
     */  
    public String getDistanceTime(Date one, Date two) {  
        long hour = 0;  
        long min = 0;  
        try {  
        	if(one.after(two)){
        		return min + getResources().getString(R.string.minute) ;
        	}
            long time1 = one.getTime();  
            long time2 = two.getTime();  
            long diff ;  
            if(time1<time2) {  
                diff = time2 - time1;  
            } else {  
                diff = time1 - time2;  
            }  
            hour = diff / (60 * 60 * 1000);  
            min = ((diff / (60 * 1000))  - hour * 60) + 1;  
        } catch (Exception e) {
            e.printStackTrace();  
        }
        if(hour>0){
        	return  hour + getResources().getString(R.string.hour) + min + getResources().getString(R.string.minute) ;  
        }else{
        	return   min + getResources().getString(R.string.minute) ;  
        }
    }  
	
	/**
	 * ��ȡʵ��
	 * @return
	 */
	public static TimerService getInstance(){
		return currentInstance;
	}
	
	/**
	 * �Ƿ�����ʱ����
	 * @return
	 */
	public static boolean isEnableLockScreenTimer(){
		
		return isEnableTimer;
		
	}
	
	/**
	 * �����Ƿ�ʱ
	 * @param hour
	 * @param minute
	 */
	private void saveIsTimer(boolean isTimer){
		
		try{
			System.out.println("����"+isTimer);
			SharedPreferences preferences=getSharedPreferences(MyApplication.KEY_ISTIMER, Activity.MODE_PRIVATE);
			Editor editor=preferences.edit();
			editor.putBoolean(MyApplication.KEY_ISTIMER, isTimer);
			editor.commit();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ����Ƿ�ʱ
	 * @return
	 */
	private boolean getSaveIsTimer(){
		
		boolean isTimer=false;
		try{
			SharedPreferences preferences=getSharedPreferences(MyApplication.KEY_ISTIMER, Activity.MODE_PRIVATE);
			isTimer=preferences.getBoolean(MyApplication.KEY_ISTIMER, false);
			System.out.println("��ȡ"+isTimer);
		}catch(Exception e){
			e.printStackTrace();
		}
		return isTimer;
	}
	
	@Override
	public boolean stopService(Intent name) {
		return super.stopService(name);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		currentInstance=null;
	}

}
