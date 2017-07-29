package com.example.michal.myapplication1;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {

    private Button buttonPlayPause;
    private Button cnvrtBtn;
    private Button btnCancel;
    private SeekBar seekBarProgress;
    public EditText editTextSongURL;
    private TextView seconds;
    private RequestQueue mRqstQueue;
    private JsonObjectRequest jsObjRequest;
    String value1;

    int sec=0;
   // String mp3= "http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3";

    private MediaPlayer mediaPlayer;
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class

    private final Handler handler = new Handler();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Intent intent = getIntent();
        if(intent.getExtras()!=null) {
             value1 = intent.getExtras().getString(Intent.EXTRA_TEXT);
            String type = intent.getType();
            if ("text/plain".equals(type)) {
                Log.i("YT2", value1);
                sendRequest(new VolleyCallBack() {
                    @Override
                    public void onSuccess(String result) {
                        playSong(result);
                    }
                },value1);


            }
        }
    }

    /**
     * This method initialise all the views in project
     */
    private void initView() {
        cnvrtBtn = (Button) findViewById(R.id.btnConvert);
        cnvrtBtn.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        buttonPlayPause = (Button) findViewById(R.id.ButtonTestPlayPause);
        buttonPlayPause.setOnClickListener(this);
        buttonPlayPause.setText("Play");

        seekBarProgress = (SeekBar) findViewById(R.id.SeekBarTestPlay);
        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setOnTouchListener(this);
        editTextSongURL = (EditText) findViewById(R.id.EditTextSongURL);
       // editTextSongURL.setText(R.string.testsong_21_sec);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaFileLengthInMilliseconds = mediaPlayer.getDuration();
                seconds.setText("0");
                mediaPlayer.start();
                buttonPlayPause.setText("Pause");
                primarySeekBarProgressUpdater();

            }
        });

       seconds= (TextView) findViewById(R.id.textSecond);
    }
    public void playSong(String url){
        try {
            mediaPlayer.setDataSource(url); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
            mediaPlayer.prepareAsync(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
            seconds.setText("LOADING");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method which updates the SeekBar primary progress by current song playing position
     */
    private void primarySeekBarProgressUpdater() {
        if (mediaPlayer != null) {
            seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
            if (mediaPlayer.isPlaying()) {

                Runnable notification = new Runnable() {
                    public void run() {
                        primarySeekBarProgressUpdater();
                    }
                };
                Runnable secNot = new Runnable() {
                    public void run() {

                        seconds.setText(String.valueOf(sec++));
                    }
                };
                handler.postDelayed(notification, 1000);
                handler.postDelayed(secNot, 1000);
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ButtonTestPlayPause) {

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                buttonPlayPause.setText("Pause");
            } else {
                mediaPlayer.pause();
                buttonPlayPause.setText("Play");
            }
             primarySeekBarProgressUpdater();

        }
        if (v.getId() == R.id.btnConvert) {
            sendRequest(new VolleyCallBack() {
                @Override
                public void onSuccess(String result) {
                    playSong(result);
                }
            },editTextSongURL.getText().toString());
        }
        if (v.getId() == R.id.btnCancel) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.SeekBarTestPlay) {
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if (mediaPlayer.isPlaying()) {
                SeekBar sb = (SeekBar) v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);

            }
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
        buttonPlayPause.setText("Play");
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
       // seconds.setText(sec);
        seekBarProgress.setSecondaryProgress(percent);
    }

    public String sendRequest(final VolleyCallBack callback, String url){
        String url1;
        try {
            url = "http://www.youtubeinmp3.com/fetch/?format=JSON&video="+url;
            url1 = URLEncoder.encode(url, "UTF-8");
            url1 = URLDecoder.decode(url, "UTF-8");

        Log.i("REQ",url);
        mRqstQueue = Volley.newRequestQueue(this);
        jsObjRequest = new JsonObjectRequest(Request.Method.GET, url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i("RESP",response.toString());
                String ret=null;
                try {
                    //Log.e("LINK",response.get("link").toString());
                   callback.onSuccess(response.get("link").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mRqstQueue.add(jsObjRequest);


        return null;
    }

}
