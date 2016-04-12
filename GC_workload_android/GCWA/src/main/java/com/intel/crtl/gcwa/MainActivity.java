package com.intel.crtl.GCWA;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final String mTag = "GCWA";
    private boolean mRunning;

    private Button mStartButton;
    private Button mProfileSettingButton;
    private Spinner mProfileNameList;
    //private AlertDialog mDialog;
    private SeekBar mSeekBar;
    private LinearLayout mWorkloadResultLayout;
    private Button mShowResultButton;
    private TextView mExeTimeView;
    private TextView mRuntimeView;
    private TextView mWorkloadStatus;
    
    private Profile mProfile = null;
    
    private Bundle mResultData = null;

    //handler to handle BenchmarkDone message
    final static int BenchmarkDone = 100;
    final static int BenchmarkProgress = 101;
    final static int BenchmarkOutOfMemoryError = 102;
    private class MyHandler extends Handler {
        public void handleMessage(Message m) {
            switch(m.what) {
            case BenchmarkDone:
                if (mRunning) {
                    //String completionTime = b.getString("completion_time");
                    /*if (mDialog != null) {
                        //mDialog.setMessage(completionTime);
                        //if (!mDialog.isShowing())
                        //    mDialog.show();
                        if (mDialog.isShowing())
                            mDialog.dismiss();
                    }*/
                    mRunning = false;
                    mStartButton.setVisibility(View.VISIBLE);
                    mProfileSettingButton.setVisibility(View.VISIBLE);
                    mWorkloadStatus.setText(R.string.workload_status_desc);
                    mResultData = m.getData();
                    String device = mResultData.getString("device");
                    long execution_time = mResultData.getLong("workload_complete_time");
                    mRuntimeView.setText(device);
                    mExeTimeView.setText(String.valueOf(execution_time));
                    mWorkloadResultLayout.setVisibility(View.VISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                break;
            case BenchmarkProgress:
            	if (mRunning && mSeekBar != null) {
            		int progress = m.arg1;
            		if (progress < mSeekBar.getMax())
            			mSeekBar.setProgress(progress);
            	}
            	break;
            case BenchmarkOutOfMemoryError:
            	if (mRunning) {
                    mWorkloadStatus.setText(R.string.outofmemory_desc);
                    
                    mRunning = false;
            	}
            	break;
            default:
                break;
            }
        }
    }
    private MyHandler mBenchmarkHandler = null;
    public Handler getHandler() {
        return mBenchmarkHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.i(mTag, "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBenchmarkHandler = new MyHandler();

        mWorkloadResultLayout = (LinearLayout)findViewById(R.id.workload_result_layout);
        mSeekBar = (SeekBar)findViewById(R.id.seek_bar);
        mSeekBar.setVisibility(View.INVISIBLE);
        mWorkloadResultLayout.setVisibility(View.INVISIBLE);
        
        mStartButton = (Button)findViewById(R.id.button_start);
        mStartButton.setOnClickListener(startWorkload);
        mWorkloadStatus = (TextView)findViewById(R.id.workload_status);

        mProfileSettingButton = (Button)findViewById(R.id.button_set);
        mProfileSettingButton.setOnClickListener(settingProfile);
        
        mRuntimeView = (TextView)findViewById(R.id.runtime_info);
        mExeTimeView = (TextView)findViewById(R.id.execution_time);
        mShowResultButton = (Button)findViewById(R.id.button_result);
        mShowResultButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mResultData != null) {
	                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
	                Bundle b = new Bundle(mResultData);
	                intent.putExtra("result_data", b);
	                startActivity(intent);
				} else
					Log.e(mTag, "no result to show now");
			}
		});
        
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setPositiveButton("OK", null);
        //mDialog = builder.create();

        mProfileNameList = (Spinner)findViewById(R.id.profile_list_view);
        mProfile = Profile.getInstance();
        Bundle b = getIntent().getExtras();
        if (b != null && b.containsKey("profile_file")) {
            FileInputStream profileFile;
            try {
                profileFile = new FileInputStream(b.getString("profile_file"));
                mProfile.parseProfileData(profileFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(mTag, "Cannot load profile. " + e.getMessage());
            }
        } else {
            mProfile.parseProfileData(getResources().openRawResource(R.raw.profile));
        }

        if (mProfile.initialized()) {
            String[] names = new String[mProfile.mData.size()];
            for (int i = 0; i < mProfile.mData.size(); i++) {
            	Profile.ProfileData d = mProfile.mData.get(i);
                names[i] = d.getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mProfileNameList.setAdapter(adapter);
            mProfileNameList.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                        int arg2, long arg3) {
                    String name = (String)(arg0.getAdapter().getItem(arg2));
                    //mCurProfileData = mProfile.getProfile(name);
                    mProfile.setCurrentProfileData(name);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub
                    //ProfileData data = mProfile.getProfile((String)(arg0.getAdapter().getItem(0)));
                    //setProfileData(data);
                }

            });
            mProfileNameList.setVisibility(View.VISIBLE);
            if (b != null && b.containsKey("profile_name")) {
                int pos = adapter.getPosition(b.getString("profile_name"));
                mProfileNameList.setSelection(pos);
                //Log.i(mTag, "select " + pos + " - " + b.getString("profile_name"));
                //mCurProfileData = mProfile.getProfile(b.getString("profile_name"));
            }
        } else {
            mProfileNameList.setVisibility(View.INVISIBLE);
            Log.i(mTag, "Cannot get profile data");
        }
    }

    @Override
    protected void onPause()
    {
        //if (mDialog != null)
        //    mDialog.dismiss();
        super.onPause();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
    		//Log.i(mTag, keyCode + " is pressed");
    		android.os.Process.killProcess(android.os.Process.myPid());
    	}
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        if (mRunning) {
            if (mStartButton != null)
                mStartButton.setVisibility(View.INVISIBLE);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private View.OnClickListener startWorkload = new View.OnClickListener() {		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Log.i(mTag, "Start Benchmark");
            
            Profile.ProfileData profileData = mProfile.getCurrentProfileData();
			if (profileData == null) {
				Log.e(mTag, "no profile data?!");
				return;
			}
            final GCWAMain benchMain = new GCWAMain((MainActivity)(v.getContext()),
            		profileData.getTotalSize(), profileData.getBucketSize(), profileData.getLosThreshold(),
            		profileData.getSizeDistribution(), profileData.getLifetime(), profileData.getLosElementDist(),
            		profileData.getThreadMode(), profileData.getThreadNum(), profileData.getIterationNum());

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mResultData = null;
            Runtime.getRuntime().runFinalization();
            Runtime.getRuntime().gc();

            mWorkloadResultLayout.setVisibility(View.INVISIBLE);
            mProfileSettingButton.setVisibility(View.INVISIBLE);
            v.setVisibility(View.INVISIBLE);
            mWorkloadStatus.setText("GCWA is running");
            mSeekBar.setVisibility(View.VISIBLE);
            mSeekBar.setMax(profileData.getIterationNum());
            mSeekBar.setProgress(0);
            
            //mDialog.setMessage("GCWA is running");
            //if (!mDialog.isShowing())
            //    mDialog.show();

            mRunning = true;
            Thread worker = new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    benchMain.start();
                }

            });
            worker.start();
        }
	};

	private View.OnClickListener settingProfile = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(getApplicationContext(), ProfileSettingActivity.class);
			startActivity(intent);
			//startActivityForResult(intent);
		}
	};
}
