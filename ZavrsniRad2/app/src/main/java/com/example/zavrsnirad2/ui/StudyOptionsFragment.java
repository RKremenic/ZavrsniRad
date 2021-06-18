package com.example.zavrsnirad2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.R;

public class StudyOptionsFragment extends Fragment {
    private Button btnSamostalno, btnUpisivanje;
    private TextView tvOdabranaTema;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_study_options, container, false);
        Bundle bundle = this.getArguments();
        int categoryID = bundle.getInt("category_id");
        DBHelper dbHelper = new DBHelper(getActivity());

        tvOdabranaTema = root.findViewById(R.id.odabrana_tema);
        String categoryName = dbHelper.getCategoryName(categoryID);
        tvOdabranaTema.setText("Odabrana je tema: " + categoryName);

        btnSamostalno = (Button) root.findViewById(R.id.samostalno_btn);
        btnSamostalno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("category_id", categoryID);
                FragmentManager fragmentManager = getFragmentManager();
                GradingQuestionFragment gradingQuestionFragment = new GradingQuestionFragment();
                gradingQuestionFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, gradingQuestionFragment).commit();
            }
        });
        btnUpisivanje = (Button) root.findViewById(R.id.upisivanje_btn);
        btnUpisivanje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("category_id", categoryID);
                FragmentManager fragmentManager = getFragmentManager();
                InputQuestionFragment inputQuestionFragment = new InputQuestionFragment();
                inputQuestionFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, inputQuestionFragment).commit();
            }
        });


        return root;
    }
}
