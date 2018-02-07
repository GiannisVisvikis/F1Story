
package comp.examplef1.iovisvikis.f1story;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import comp.examplef1.iovisvikis.f1story.R;


public class ResultFragment extends Fragment {


    //results will appear here by adapting a new BaseAdapter at a time
    private RecyclerView resultList;
    private RecyclerView.Adapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.result_fragment_layout, container, false);

        resultList = root.findViewById(R.id.resultList);

        return root;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        resultList.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(adapter != null){
            setResultAdapter(adapter);
        }

    }



    public void setResultAdapter(RecyclerView.Adapter adapter){

        this.adapter = adapter;

        resultList.setAdapter(adapter);
    }


    public RecyclerView.Adapter getAdapter(){

        return resultList.getAdapter();
    }


}//ResultFragment


