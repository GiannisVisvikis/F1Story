package comp.examplef1.iovisvikis.f1story.MyDialogs;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import comp.examplef1.iovisvikis.f1story.R;

/**
 * Created by giannis on 28/11/2017.
 */

public class CheckDialog extends DialogFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.check_dialog, container, false);


    }


    @Override
    public void onStart() {
        super.onStart();

        int width, height;

        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            height = (int) (getResources().getDisplayMetrics().heightPixels * 0.60);
        }
        else{
            width = (int) (getResources().getDisplayMetrics().widthPixels * 0.60);
            height = (int) (getResources().getDisplayMetrics().heightPixels * 0.80);
        }

        getDialog().getWindow().setLayout(width, height);

    }



}
