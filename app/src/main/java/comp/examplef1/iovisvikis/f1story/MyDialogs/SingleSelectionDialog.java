package comp.examplef1.iovisvikis.f1story.MyDialogs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import comp.examplef1.iovisvikis.f1story.R;


/**
 * Created by iovisvikis on 27/1/2017.
 */

public class SingleSelectionDialog extends SelectionDialog{


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View root = inflater.inflate(R.layout.select_single_dialog, null);

        setSeasonSpinner((AppCompatSpinner) root.findViewById(R.id.dialog_season_choice));

        setSpinners(getSeasonSpinner(), null);

        setXMark((AppCompatImageButton) root.findViewById(R.id.dialog_x_button));

        setCheckMark((AppCompatImageButton) root.findViewById(R.id.dialog_check_button));

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return root;
    }
}
