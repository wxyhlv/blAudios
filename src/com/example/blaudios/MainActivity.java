package com.example.blaudios;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private Thread runner;
	private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;
	private MediaRecorder recorder = null;
	private int currentFormat = 1;
	private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
	private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP }; 
	private Vibrator vibrator;
	final Runnable updater = new Runnable(){
		public void run(){
			updateTv();
		};
	};
	final Handler mHandler = new Handler();
	String showDB="";
	double currentDB;
	double limitDB=3;
    TextView  showTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        showTextView=(TextView) findViewById(R.id.text_show);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE); 
        if (runner == null)
        { 
            runner = new Thread(){
                public void run()
                {
                    while (runner != null)
                    {
                        try
                        {
                            Thread.sleep(1000);
                            Log.i("Noise", "Tock");
                        } catch (InterruptedException e) { };
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }
    }
    public void onResume()
    {
        super.onResume();
        startRecording();
    }

	protected void updateTv() {
		
		//voicedb=getAmplitudeEMA();
		currentDB=getAmplitudeEMA();
		action();
		showDB=showDB+Double.toString(currentDB) + " dB"+"\n";
		showTextView.setText(showDB);
		AppLog.logString(currentDB+"");
		AppLog.logString(String.valueOf(recorder.getMaxAmplitude()));
	}
	/**
	 * @Johnson
	 * 计算分贝数，当蝈蝈叫超过限制分贝调用手机震动
	 * */
	public void  action(){	
		if(currentDB-limitDB>0){	
		 AppLog.logString("开始震动.......");
		 vibrator.vibrate( new long[]{100,10,100,1000},-1);		
		 currentDB=currentDB-10;
		}
	}
	/**
	 * @Johnson
	 * 计算声音分贝
	 * */
	private double getAmplitudeEMA() {
		double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
		return mEMA;
	}

	private double getAmplitude() {
		if(recorder != null)
		return recorder.getMaxAmplitude()/2700;
		else
		return 0;
	}

	private String getFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER);
		AppLog.logString(filepath);
		if(!file.exists()){
			file.mkdirs();
		}
		AppLog.logString(file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
	}
	
	private void startRecording(){
		recorder = new MediaRecorder();
		
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(output_formats[currentFormat]);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(getFilename());
		
		recorder.setOnErrorListener(errorListener);
		recorder.setOnInfoListener(infoListener);
		
		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void stopRecording(){
		if(null != recorder){
			recorder.stop();
			recorder.reset();
			recorder.release();
			
			recorder = null;
		}
	}
	
	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			AppLog.logString("Error: " + what + ", " + extra);
		}
	};
	
	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			AppLog.logString("Warning: " + what + ", " + extra);
		}
	};
}