package com.example.android.NewSynth;

import java.io.File;
import java.io.FileOutputStream;

import android.R.string;
import android.media.AudioFormat;
import android.util.Log;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
//import com.example.android.NewSynth.LargeBuffer;

public class SynthUI extends Activity {

	private EditText text;
    public  final static String PAR_KEY = "com.example.android.NewSynth.par";  
	Thread t;    
    int sr = 44100;
    boolean isRunning = true;
    
    double fr=440.f;
	volatile int i;
	
	volatile boolean notechange = false;
	
	int buffsize = 40;
	AudioTrack audioTrack;
	
	short large_buffer[];
	int lb_playhead=0;
	int lb_last_play=0;
	
	LargeBuffer largeBuff ;
	
	
	
	short samples[] ;
	int amp = 10000;
	int note_length = 1000;
	
	double twopi = 8.*Math.atan(1.);
	
	double ph = 0.0;
	MainBuffer buffer2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 
		Button button1= (Button)findViewById(R.id.button1);
		Button button2= (Button)findViewById(R.id.button2);
		Button button3= (Button)findViewById(R.id.button3);
		Button button4= (Button)findViewById(R.id.button4);
		
		button1.setOnTouchListener(buttonOneListener);
		button2.setOnTouchListener(buttonTwoListener);
		button3.setOnTouchListener(buttonThreeListener);

		largeBuff = new LargeBuffer( buffsize, note_length );
		
		
		 // start a new thread to synthesise audio        
	    t = new Thread() {
	         public void run() {
	        	 
	        	 Bundle b = getIntent().getExtras();

	        //	 if(b!=null)
	        	  // mainBuffer = b.getParcelable(PAR_KEY);
	        		 
	        
	        	 
	           // set process priority
	           setPriority(Thread.NORM_PRIORITY);
	           
	          // buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, 
              //         AudioFormat.ENCODING_PCM_16BIT);
//create an audiotrack object
 audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr, 
               AudioFormat.CHANNEL_OUT_MONO, 
               AudioFormat.ENCODING_PCM_16BIT, 
               buffsize, 
               AudioTrack.MODE_STREAM);
 
 
 				
 				int lb_size = buffsize * note_length * 2;
 				
 				buffer2 = new MainBuffer(400);
 
 				large_buffer= new short[ lb_size ];
 				
 				for(i=0;i<buffsize*note_length*2;i++)
 					large_buffer[i]=0;
 

			
			
			// start audio
			audioTrack.play();
			
			// synthesis loop
			while(isRunning){
							   			   
			}
			
			audioTrack.stop();
			audioTrack.release();
			}
		};
			
			t.start();
		
	}
	
	 public void freqClickHandler(View view) {
//		    switch (view.getId()) {
//		    case R.id.button0:
//		      
//		      if (text.getText().length() == 0) {
//		        Toast.makeText(this, "Please enter a valid number",
//		            Toast.LENGTH_LONG).show();
//		        return;
//		      }
//
//		      float inputValue = Float.parseFloat(text.getText().toString());
//		      fr = (double) inputValue;
//		      break;
//		    }
		    
		  }
	 
	 
	 private OnTouchListener buttonOneListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonOneHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };
	 
	 private OnTouchListener buttonTwoListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonTwoHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };
	 private OnTouchListener buttonThreeListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonThreeHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };	 
	 
	 public void buttonOneHandler(View view) {
		 		
		 buffer2.addNote(440, 8000, 100, 1000, false, 0 );
		 
	 }
	 
	 public void buttonTwoHandler(View view) {
		buffer2.addNote(493.88, 8000, 100, 1000, false, 0 );

	 }
	 
	 public void buttonThreeHandler(View view) {
		 buffer2.addNote(554.37, 8000, 100, 1000, false, 0 );


	 }
	 
	 public void returnButtonHandler(View view)
	 {
		 finish();
	 }
	
	 
	 public void playNote(float freq, int amplitude, int attack, int length)
	 {
		 //buffsize=40;
		 //buffsize is 6144
		 //max amplitude?
		 int i;
		 
		 //int length=10;
		 
		 samples = new short[buffsize*length];
		 
		 for( i=0; i < buffsize*length; i++){ 
			 
			 if(i < attack)
			 {
				 samples[i] = (short) (amp * ( (float) Math.sin(ph) ) * (float) i / (float) attack  );
			 }
			 else
			 {
				samples[i] = (short) ( amp * ( (float) Math.sin(ph) ) * (1 - ( (float) i ) / ( (float) buffsize*length) ) );
			     
			 }   
			 
			 ph += twopi*freq/sr;
			     //Log.d( "regular", "" + samples[i] );
			     
		 }
		 
		 queueNote(samples, length);	 
		 		 
	 }
	 
	 
	 
	 void queueNote( short[] samples, int length_in_buffers )
	 {
		 int i;
		 
		 int notesize = length_in_buffers * buffsize;
		 
		 /*
		 for(i=0; i< notesize ; i++)  
		 {
			large_buffer[i] += samples[i]; // add note's samples to the buffer to be played
		 }
		 */
		 
		 
		 
		 //lb_playhead += length_in_buffers;
		 //lb_last_play = lb_playhead + length_in_buffers;
		 
//		 
//		 for(i=0; i<length; i++)
//		 {
//			 audioTrack.write(samples, i*buffsize, buffsize); 
//		 } 
		 
	 }
	 
	 public class LargeBuffer
	 {
		 short frame_queue[][]; // array of frames
		 int playhead;
		 int buffer_size;
		 int writehead;
		 
		 //TODO generalize note length
		 
		 //CONSTRUCTOR:
		 public LargeBuffer(int buffsize, int note_length) {
			 int i, j;
			 buffer_size = buffsize;
			 frame_queue = new short[note_length*2][buffsize];
			 playhead=0;
			 writehead=0;
			 for(i=0;i<note_length*2;i++)
			 {
				 for(j=0;j<buffsize;j++)
				 {
					 frame_queue[i][j]=0;
				 }
			 }
				 
		}

		public void writeNote(float freq, int amp, int attack, int note_length) {
			 int i,j;
			 
			 //int length=10;
			 
			 short samples_arr[] = new short[buffer_size*note_length];
			 
			 //create full array for note: 
			 for( i=0; i < buffer_size*note_length; i++){ 
				 
				 if(i < attack)
				 {
					 samples_arr[i] = (short) (amp * ( (float) Math.sin(ph) ) * (float) i / (float) attack  );
				 }
				 else
				 {
					samples_arr[i] = (short) ( amp * ( (float) Math.sin(ph) ) * (1 - ( (float) i ) / ( (float) buffer_size*note_length) ) );
				     
				 }   
				 
				 ph += twopi*freq/sr;
				     //Log.d( "regular", "" + samples[i] );
				     
			 }
			 
			 
			 ///////
			 for(i=0;i<note_length;i++)
			 {
				 audioTrack.write( samples_arr, i*buffer_size, buffer_size); // works--for loop to play entire note!
			 }
			 ///////
			 for(i=0; i< note_length;i++)
			 {
				 //playBuf(samples_arr, i); // works
			 }
			 
			///////
			 for(i=0; i< note_length;i++)
			 {
				 queueBuff(samples_arr, i);
			 }
			 
			 
			 
			 /*
			 
			 for(i=0; i< note_length ; i++)  
			 {
				 writeFrame(samples_arr, ) ;
				 
				 
				for(j=0;j<buffer_size;j++)
				{
					 frame_queue[i][j] += samples_arr[i*buffer_size + j]; // add note's samples to the buffer to be played
				}
			 }
			 */
			 

			 
		}

		private void queueBuff(short[] samples_arr, int offset ) {
			int j;
			for(j=0;j<buffer_size;j++)
			{
				frame_queue[offset][j] += samples_arr[offset*buffer_size + j];		
			}
			
		}
		
		private void playFrame( )
		{
			
		}

		private void playBuf(short[] samples_arr, int offset) {
			audioTrack.write( samples_arr, offset*buffer_size, buffer_size); // works		
		}

		public boolean hasFrames()
		 {
			 return true;
		 }
		 
		 public void playOneFrame()
		 {
			 int i;
			 audioTrack.write(frame_queue[playhead], 0, buffer_size);
			 
			 
			 //clear audio data from buffer
			 for(i=0;i<buffer_size;i++)
			 {
				 frame_queue[playhead][i]=0;
			 }
			 
			 playhead++;
			 
		 }
		 
		 public void writeFrame( short[] samples )
		 {
			 int i;
			 for(i = 0; i < buffer_size; i++)
			 {
				 frame_queue[writehead][i] += samples[i]; 
			 }
			 
		 }
		 
		 
	 }
 
	 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onDestroy(){
		   super.onDestroy();    
		   isRunning = false;
		   try {
		     t.join();
		   } catch (InterruptedException e) {
		     e.printStackTrace();
		   }    
		  t = null;
		}
	

}
