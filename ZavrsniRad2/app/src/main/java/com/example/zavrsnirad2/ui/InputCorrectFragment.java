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

import com.example.zavrsnirad2.R;
import com.example.zavrsnirad2.ui.InputQuestionFragment;

public class InputCorrectFragment extends Fragment {

    private Button btnNastavi;
    private TextView tvQuestion, tvAnswer, tvCorrectAnswer, tvCorrect, tvInterval;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_input_correct, container, false);
        Bundle bundle = this.getArguments();
        String card = bundle.getString("card");

        String userInput = bundle.getString("userInput");
        String[] data = card.split("#");

        tvQuestion = root.findViewById(R.id.tekst_pitanja5);
        tvQuestion.setText(data[1]);
        tvCorrectAnswer = root.findViewById(R.id.tekst_odgovora5);
        tvCorrectAnswer.setText(data[2]);
        tvAnswer = root.findViewById(R.id.tekst_ponudenog_odgovora5);
        tvAnswer.setText(userInput);
        tvCorrect = root.findViewById(R.id.broj_tocnih5);
        tvInterval = root.findViewById(R.id.novi_interval5);
        String interval = "Novi interval: " + data[6];
        String correct = "Broj uzastopnih točnih odgovora: " + data[4];
        tvInterval.setText(interval);
        tvCorrect.setText(correct);

        btnNastavi = (Button) root.findViewById(R.id.nastavi_btn5);
        btnNastavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("category_id", Integer.parseInt(data[3]));
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
