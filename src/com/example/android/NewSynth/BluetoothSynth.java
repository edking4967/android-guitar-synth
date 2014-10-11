/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.NewSynth;

//import com.example.android.BluetoothChat.R;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothSynth extends Activity {
	
    public  final static String PAR_KEY = "com.example.android.NewSynth.par";  
    
	//hardcoded:
	private static final int SQUARE = 1;
	private static final int SIN = 0;
	
	int wf=SIN;  // initial waveform
	
	// Settings/config:
	
	private static final boolean SEND_ONE_BYTE = false;
	private static final int ADC_SAMPLE_RATE = 15463;
	private static final int NUM_SAMPLES = 1024;
	
	
	// Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    
    //audio stuff:
    
	MainBuffer mainBuff ;
    
    Thread t;    
    int sr = 44100;  // sample rate
    boolean isRunning = true;
    
    float fr=440;
    float last_fr;
	volatile int i;
	
	volatile boolean notechange = false;
	
	int buffsize;
	AudioTrack audioTrack;
	
	short samples[] ;
	int amp = 6000;  // amplitude of each note
	double twopi = 8.*Math.atan(1.);
	
	double ph = 0.0;

	String readMessage;
	
	byte passval[]; 
	int passval0, passval1, passval2, passval3;
	int pass_size;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
                
        
        Button button1= (Button)findViewById(R.id.button3);
		Button button2= (Button)findViewById(R.id.button8);
		Button button3= (Button)findViewById(R.id.button7);
		Button button4= (Button)findViewById(R.id.button1);
		Button button5= (Button)findViewById(R.id.button6);
		Button button6= (Button)findViewById(R.id.button5);
		Button button7= (Button)findViewById(R.id.button4);
		Button button8= (Button)findViewById(R.id.button9);
	        
        
		button1.setOnTouchListener(buttonOneListener);
		button2.setOnTouchListener(buttonTwoListener);
		button3.setOnTouchListener(buttonThreeListener);
		button4.setOnTouchListener(buttonFourListener);
		button5.setOnTouchListener(buttonFiveListener);
		button6.setOnTouchListener(buttonSixListener);
		button7.setOnTouchListener(buttonSevenListener);
		button8.setOnTouchListener(buttonEightListener);

        
        //SET UP BUFFER:
        
        mainBuff = new MainBuffer( 40 ); //buffer size 40 frames, note size 1000 buffers
 
        

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
     // start a new thread to synthesise audio        
	    t = new Thread() {
	         @SuppressLint("NewApi")
			public void run() {
	           // set process priority
	           setPriority(Thread.NORM_PRIORITY);
	           
	          // buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, 
               //        AudioFormat.ENCODING_PCM_16BIT);
	           
	           buffsize = 400;
	           
//create an audiotrack object
 audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr, 
               AudioFormat.CHANNEL_OUT_MONO, 
               AudioFormat.ENCODING_PCM_16BIT, 
               buffsize, 
               AudioTrack.MODE_STREAM);
 				samples = new short[buffsize];
 

			
			
			// start audio
			audioTrack.play();
			int asd=1;
			// synthesis loop
			while(isRunning){
				
			   	if( mainBuff.playReady() )  // if there is a frame to play
			   	{
			   		mainBuff.playNextFrame(); // play the frame
			   	}
			
			   
			}
			
			audioTrack.stop();
			audioTrack.release();
			}
		};
		t.start();


    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
      //  mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	//Switch to synth control panel
            	/*
            	Intent intent = new Intent(getApplicationContext(), SynthUI.class);
            	
            	Bundle b = new Bundle();
            	b.putParcelable( PAR_KEY ,  mainBuff);
            	intent.putExtras(b);
            	
            	startActivity( intent );
            	*/
            	
            	ListView lv = (ListView) findViewById(R.id.in);
            	if(lv.getVisibility() == View.VISIBLE )
            	{
            		lv.setVisibility(View.GONE);
            	}
            	else
            	{
            		lv.setVisibility(View.VISIBLE);
            	}
            	
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
	   
        
        isRunning = false;
	   try {
	     t.join();
	   } catch (InterruptedException e) {
	     e.printStackTrace();
	   }    
	  t = null;
    
    }
    


    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
           // mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            if(D) Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                readMessage = new String(readBuf, 0, msg.arg1);
                passval = readBuf;      
                pass_size=msg.arg1;
                
                if(SEND_ONE_BYTE)
                {
	                passval0= ( passval[0] & 0xFF) ;
					passval1=passval[1];
					passval2=passval[2];
					passval3=passval[3];
					//fr = passval0 * 4;
					fr = passval0 * 4;
					//Log.d( "regular" , "" + fr );  //debug 
                }
                else
                {
	                last_fr = fr;
                	passval0= ( passval[0] & 0xFF) ;
					fr = (float) passval0 / (float) NUM_SAMPLES * (float) ADC_SAMPLE_RATE ; 
				}
                                
                mainBuff.addNote(fr, amp, att, nl, false, wf);
                
               // mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + Float.toString( fr ));
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.scan:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }
     
    
    double freqs[] = {440, 493.88, 554.37, 587.33, 659.25, 739.99, 830.61, 880};
    
    public void octaveUp(View view)
    {
    	int i;
    	for(i = 0; i < 8; i++)
    	{
    		freqs[i] *= 2;
    	}
    }
    
    public void octaveDown(View view)
    {
    	int i;
    	for(i = 0; i < 8; i++)
    	{
    		freqs[i] /= 2;
    	}
    }
    
    int nl = 1000;
    
    int att = 100;
    
    
	 public void buttonOneHandler(View view) {
	 		
		 mainBuff.addNote(freqs[0], 8000, att, nl, false, wf );
		 
	 }
	 
	 public void buttonTwoHandler(View view) {
		mainBuff.addNote(freqs[1], 8000, att, nl, false, wf );

	 }
	 
	 public void buttonThreeHandler(View view) {
		 mainBuff.addNote(freqs[2], 8000, att, nl, false, wf );
	 }
	 
	 public void buttonFourHandler(View view) {
		 mainBuff.addNote(freqs[3], 8000, att, nl, false, wf );


	 }
	 
	 public void buttonFiveHandler(View view) {
		 mainBuff.addNote(freqs[4], 8000, att, nl, false, wf );

	 }
	 
	 public void buttonSixHandler(View view) {
		 mainBuff.addNote(freqs[5], 8000, att, nl,  false, wf );
	 }
	 

	 public void buttonSevenHandler(View view) {
		 mainBuff.addNote(freqs[6], 8000, att, nl, false, wf );
	 }

	 public void buttonEightHandler(View view) {
		 mainBuff.addNote(freqs[7], 8000, att, nl, false, wf );
	 }
	 
	 
	 public void squaresin(View view) {
		 if(wf==SQUARE)
		 {
			 wf=SIN;
			 amp*=2;
		 }
		 else
		 {
			 wf=SQUARE;
			 amp*=.5;
		 }
	 }
	 
	 public void changeAtt(View view)
	 {
		 EditText text = (EditText) findViewById(R.id.editTextAtt);
		 att= Integer.parseInt( text.getText().toString() );
	 }
	 
	 public void changeDur(View view)
	 {
		 EditText text = (EditText) findViewById(R.id.editTextDur);
		 nl= Integer.parseInt( text.getText().toString() );

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
	 
	 private OnTouchListener buttonFourListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonFourHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };
	 
	 private OnTouchListener buttonFiveListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonFiveHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };
	 private OnTouchListener buttonSixListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonSixHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };	 

	 private OnTouchListener buttonSevenListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonSevenHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };
	 private OnTouchListener buttonEightListener = new OnTouchListener()
	 {
		   

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				int action = arg1.getActionMasked();
				 if (action == MotionEvent.ACTION_DOWN)
				 {
						buttonEightHandler(arg0);
						
				 }
				 return true;

				// TODO Auto-generated method stub
			}

	 };	 
    

}