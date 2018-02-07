package comp.examplef1.iovisvikis.f1story.MyDialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import comp.examplef1.iovisvikis.f1story.MainActivity;
import comp.examplef1.iovisvikis.f1story.R;

/**
 * Created by iovisvikis on 15/10/2016.
 */

public class UpdatesDialog extends DialogFragment {


    private MainActivity act;

    private AppCompatTextView txtView;
    private VideoView videoView;

    private boolean videoOver = false;


    public boolean isVideoOver(){return this.videoOver;}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        act = (MainActivity) getActivity();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = act.getLayoutInflater().inflate(R.layout.download_progress, null);

        txtView = view.findViewById(R.id.message);

        videoView = view.findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/raw/monza_1969"));

        //for some stupid reason the onCompletionListener does not work if it is defined in the same lifecycle method
        //with the video view assignment. See in onResume method for it

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return view;
    }



    @Override
    public void onStart(){
        super.onStart();

        Dialog dialog =  getDialog();

        WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int dialogWidth, dialogHeight;

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            dialogWidth = 8 * screenWidth/10;
            dialogHeight = screenHeight/2;
        }
        else {
            dialogWidth =  screenWidth/2;
            dialogHeight = 8 * screenHeight/10;
        }

        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
    }



    @Override
    public void onResume() {
        super.onResume();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoOver = true;
                Log.e("VIDEO_OVER", videoOver + "");
            }
        });

        videoView.start();


    }


    @Override
    public void onDetach() {
        super.onDetach();

        act = null;
    }


    public void updateMessage(String newMessage){txtView.setText(newMessage);}



}//UpdatesDialog



