package com.example.android.NewSynth;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;



    public class MainBuffer implements Parcelable 
	 {
    	
    	private static final int SQUARE = 1;
    	private static final int SIN = 0;
    	
   	
		 Frame frame_queue[]; // array of buffers
		 boolean frames_valid[];
		 int frameSize;		 
		 int playhead;
		 int writehead;
		 int notelength;
		 boolean choke;
		double twopi = 8.*Math.atan(1.);
		double ph=0.0;
		int sr = 44100;
		AudioTrack audioTrack;
			
		int note_length=10000;

		 				 
		 //CONSTRUCTOR:
		 public MainBuffer(int buffsize ) {
			 int i ;
			 
			 audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr, 
		               AudioFormat.CHANNEL_OUT_MONO, 
		               AudioFormat.ENCODING_PCM_16BIT, 
		               buffsize, 
		               AudioTrack.MODE_STREAM);
				audioTrack.play();


			 notelength = note_length;
			 frame_queue = new Frame[note_length*10];
			 frameSize=buffsize;
			 playhead=0;
			 writehead=0;
			 frames_valid = new boolean[note_length*10];
			 for(i=0;i<note_length*10;i++)
			 {
				 frame_queue[i] = new Frame(buffsize); //setup large buffer for 10 notes
				 frames_valid[i]=false;
			 }
				 
		}
		 
		public void addNote(double fr, int ampl, int attack, int notelength, boolean choke, int waveform) {
			
			int i;
			int pos = playhead;
			Frame frame;
			frame = frame_queue[pos];
			short sample;
			int framepos=0;
			float envelope;
			float wave = 0;
			
			short square_hinge = (short) (sr / (2 * fr)); 
			
			for( i=0; i < frameSize*notelength; i++){ 
			//GENERATE WAVE:
				
				//envelope
				if(i<attack)
				{
					envelope = (float) i / (float) attack;
				}
				else
				{
					envelope = (1 - ( (float) i ) / ( (float) frameSize*notelength) );
				}
				
				//wave
				if(waveform==SIN)
				{
					wave = (float) Math.sin(ph);
				}
				else if(waveform==SQUARE)
				{
					if( i % square_hinge  == 1 ) 
					{
						Log.d("blah","sq=" + square_hinge + "i=" + i);
						if(wave == 1)
							wave = -1;
						else
							wave = 1;
					}
				}
				
				sample = (short) ( ampl * envelope * wave);
				ph += twopi*fr/sr;
				
			//END GENERATE WAVE
				 
				 //write sample to frame in main buffer:
				 frame.writeSample( framepos , sample, choke) ;
				 
				 framepos++;
				 
				 if(framepos==frameSize) // frame is full
				 {
					 frames_valid[pos]=true; // set frame ready to play
					 
					 
					 //load next frame:
					 pos++;
					 
					 if(pos == notelength*10)  // reached the end of the main buffer
					 {
						 pos = 0;
					 }
					 
					 frame = frame_queue[pos]; 
					 framepos=0;  
				 }
				 
								     
			 }
			
			
		}

		public void playNextFrame()
		 {
			 playFrame( playhead );
			 playhead++;
			 if(playhead == notelength*10) // reached end of main buffer
			 {
				 playhead = 0;   // go back to the beginning
			 }
		 }
		
		public void playFrame( int frameNum )
		{
			Frame nextFrame;
			nextFrame = frame_queue[frameNum];
			audioTrack.write( nextFrame.getData(), 0, frameSize );
			frames_valid[frameNum]=false; // FRAME HAS BEEN PLAYED
			nextFrame.clear();  // set data back to zero
			
		}
		
		public boolean playReady()
		{
			if(frames_valid[playhead]) return true;
			else return false;
		}
		 
    	public class Frame
    	{
    		short data[]; 
    		int size;
    		public Frame( int buffsize )
    		{
    			size = buffsize;
    			int i;
    			data = new short[buffsize];
    			for(i=0;i<buffsize;i++)
    			{
    				data[i]=0;
    			}
    		}
    		
    		public int getSize()
    		{
    			return size;
    		}
    		
    		public short[] getData()
    		{
    			return data;
    		}
    		void clear()
    		{
    			int i;
    			for(i=0; i<size;i++)
    			{
    				data[i]=0;
    			}
    		
    		}
    		
    		void writeSample(int offset, short datapt, boolean choke)
    		{
    			if(choke)
    			{
    				data[offset] = datapt;
    			}
    			else 
    			{
    				data[offset] += datapt;  //ADDS data
    			}
    			 
    		}
    		
    	}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel arg0, int arg1) {
			// TODO Auto-generated method stub
			// frame_queue = arg0.framequeue[]; // array of buffers
			 boolean frames_valid[];
			 int frameSize;		 
			 int playhead;
			 int writehead;
			 int notelength;
			 boolean choke;
			double twopi = 8.*Math.atan(1.);
			double ph=0.0;
			int sr = 44100;
			AudioTrack audioTrack;
			
		}
    	
    	

	 }