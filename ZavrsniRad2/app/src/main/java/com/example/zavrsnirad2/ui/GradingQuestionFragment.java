package com.example.zavrsnirad2.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.R;

public class GradingQuestionFragment extends Fragment {

    private Button btnPrikazi;
    private TextView tvQuestion, tvInterval, tvCorrect;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_grading_question, container, false);
        Bundle bundle = this.getArguments();
        int categoryID = bundle.getInt("category_id");
        DBHelper dbHelper = new DBHelper(getActivity());
        String card = dbHelper.getStudyCard(categoryID);
        if (card.equals("No cards")){
            Bundle bundle2 = new Bundle();
            bundle.putInt("categoryID", categoryID);
            FragmentManager fragmentManager = getFragmentManager();
            EndFragment endFragment = new EndFragment();
            endFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment, endFragment).commit();
        }
        String[] data = card.split("#");
        tvQuestion = root.findViewById(R.id.tekst_pitanja1);
        tvInterval = root.findViewById(R.id.broj_tocnih1);
        tvCorrect = root.findViewById(R.id.trenutni_interval1);
        tvQuestion.setText(data[1]);
        String interval = "Trenutni interval: " + data[6];
        tvInterval.setText(interval);
        String correct = "Broj uzastopnih točnih odgovora: " + data[4];
        tvCorrect.setText(correct);

        btnPrikazi = (Button) root.findViewById(R.id.prikazi_btn1);
        btnPrikazi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("card", card);
                FragmentManager fragmentManager = getFragmentManager();
                GradingAnswerFragment gradingAnswerFragment = new GradingAnswerFragment();
                gradingAnswerFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, gradingAnswerFragment).commit();
            }
        });

        return root;
    }
}
