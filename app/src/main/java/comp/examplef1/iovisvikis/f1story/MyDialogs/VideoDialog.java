package comp.examplef1.iovisvikis.f1story.MyDialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.VideoView;

import comp.examplef1.iovisvikis.f1story.Communication;
import comp.examplef1.iovisvikis.f1story.R;

/**
 * Created by ioannisvisvikis on 7/17/17.
 */

public class VideoDialog extends DialogFragment {

    private VideoView videoView;
    Communication act;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        act = (Communication) getActivity();

        act.getSoundFragment().stopSound();

        act.blockOrientationChanges();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.video_player_layout, container, false);

        videoView = (VideoView) root.findViewById(R.id.videoView);

        videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/raw/monza_1969"));
        //for some stupid reason the completion listener does not work if it is defined in the same lifecycle method.
        //Look inside onStart for it

        FrameLayout cancelFrame = (FrameLayout) root.findViewById(R.id.cancelVideo);

        cancelFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.stopPlayback();
                VideoDialog.this.dismiss();
            }
        });

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return root;
    }



    @Override
    public void onStart() {
        super.onStart();

        double height, width;

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            height = getResources().getDisplayMetrics().heightPixels * 0.50;
            width = getResources().getDisplayMetrics().widthPixels * 0.90;
        }
        else{
            height = getResources().getDisplayMetrics().heightPixels * 0.70;
            width = getResources().getDisplayMetrics().widthPixels * 0.50;
        }

        getDialog().getWindow().setLayout((int) width, (int) height);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                act.allowOrientationChanges();
                VideoDialog.this.dismiss();
            }
        });

        act.blockOrientationChanges();

        videoView.start();
    }


    @Override
    public void onDetach() {
        super.onDetach();

        act.allowOrientationChanges();
        act = null;
    }


}//VideoDialog




