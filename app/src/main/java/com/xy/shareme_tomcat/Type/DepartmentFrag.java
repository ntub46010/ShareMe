package com.xy.shareme_tomcat.Type;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.xy.shareme_tomcat.MainActivity;
import com.xy.shareme_tomcat.R;

import static com.xy.shareme_tomcat.MainActivity.vpgHome;
import static com.xy.shareme_tomcat.Product.ProductHomeFrag.conProductHome;
import static com.xy.shareme_tomcat.Product.ProductHomeFrag.gbmProductHome;
import static com.xy.shareme_tomcat.Product.ProductHomeFrag.adpProductHome;
import static com.xy.shareme_tomcat.Product.ProductHomeFrag.recyProduct;
import static com.xy.shareme_tomcat.data.DataHelper.isFromDepartment;
import static com.xy.shareme_tomcat.data.DataHelper.setBoardTitle;

public class DepartmentFrag extends Fragment {
    private Button[] btnDep;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_department_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnDep = new Button[] {
                (Button) getView().findViewById(R.id.btnDepAll),
                (Button) getView().findViewById(R.id.btnDepGN),
                (Button) getView().findViewById(R.id.btnDepAI),
                (Button) getView().findViewById(R.id.btnDepFN),
                (Button) getView().findViewById(R.id.btnDepFT),
                (Button) getView().findViewById(R.id.btnDepIB),
                (Button) getView().findViewById(R.id.btnDepBM),
                (Button) getView().findViewById(R.id.btnDepIM),
                (Button) getView().findViewById(R.id.btnDepAFL),
                (Button) getView().findViewById(R.id.btnDepCD),
                (Button) getView().findViewById(R.id.btnDepDM),
                (Button) getView().findViewById(R.id.btnDepCC)
        };
        //指派偵聽器
        for (int i=0; i<btnDep.length; i++) {
            final int index = i;

            btnDep[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDepFrag(btnDep[index].getId());
                }
            });
        }
    }

    public void showDepFrag(int viewId) {
        //設置科系編號
        switch (viewId) {
            case R.id.btnDepAll:
                MainActivity.board = "";
                break;
            case R.id.btnDepGN:
                MainActivity.board = "00";
                break;
            case R.id.btnDepAI:
                MainActivity.board = "01";
                break;
            case R.id.btnDepFN:
                MainActivity.board = "02";
                break;
            case R.id.btnDepFT:
                MainActivity.board = "03";
                break;
            case R.id.btnDepIB:
                MainActivity.board = "04";
                break;
            case R.id.btnDepBM:
                MainActivity.board = "05";
                break;
            case R.id.btnDepIM:
                MainActivity.board = "06";
                break;
            case R.id.btnDepAFL:
                MainActivity.board = "07";
                break;
            case R.id.btnDepCD:
                MainActivity.board = "A";
                break;
            case R.id.btnDepCC:
                MainActivity.board = "B";
                break;
            case R.id.btnDepDM:
                MainActivity.board = "C";
                break;
        }

        //停止下載任務
        try {
            adpProductHome.setCanCheckLoop(false);
            conProductHome.cancel();
            gbmProductHome.cancel(true);
        }catch (NullPointerException e) {}

        //切換畫面
        isFromDepartment = true;
        vpgHome.setCurrentItem(1);
        setBoardTitle();

        recyProduct.setVisibility(View.GONE);
    }
}
