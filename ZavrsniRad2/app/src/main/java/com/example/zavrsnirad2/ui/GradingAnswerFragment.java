package com.example.zavrsnirad2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.R;
import com.example.zavrsnirad2.ui.GradingQuestionFragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GradingAnswerFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Button btnNastavi;
    private Spinner spinnerOcjene;
    private TextView tvQuestion, tvAnswer, tvCorrect, tvInterval, tvGrade;
    private String[] data, newData;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_grading_answer, container, false);
        Bundle bundle = this.getArguments();
        String card = bundle.getString("card");
        data = card.split("#");
        newData = card.split("#");
        DBHelper dbHelper = new DBHelper(getActivity());

        tvQuestion = root.findViewById(R.id.tekst_pitanja2);
        tvQuestion.setText(data[1]);
        tvAnswer = root.findViewById(R.id.tekst_odgovora2);
        tvAnswer.setText(data[2]);
        tvCorrect = root.findViewById(R.id.broj_tocnih2);
        tvInterval = root.findViewById(R.id.novi_interval2);
        tvGrade = root.findViewById(R.id.tekst_ocjene);

        String[] gradeMeanings = {
                "0 - Uopće se ne mogu sjetiti odgovora",
                "1 - Ne znam, ali gradivo mi je poznato",
                "2 - Ne znam, ali ću lako zapamtiti za sljedeći put",
                "3 - Znam, ali uz puno razmišljanja",
                "4 - Znam, ali uz malo razmišljanja",
                "5 - Znam, lagano",
                " - Odaberite ocjenu - "
        };

        spinnerOcjene = root.findViewById(R.id.ocjena_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.ocjene, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerOcjene.setSelection(0);
        spinnerOcjene.setAdapter(adapter);
        spinnerOcjene.setOnItemSelectedListener(this);

        spinnerOcjene.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String grade = spinnerOcjene.getSelectedItem().toString();
                switch (grade) {
                    case "- Odaberite ocjenu -":
                        tvGrade.setText(gradeMeanings[6]);
                        break;
                    case "0":
                        tvGrade.setText(gradeMeanings[0]);
                        break;
                    case "1":
                        tvGrade.setText(gradeMeanings[1]);
                        break;
                    case "2":
                        tvGrade.setText(gradeMeanings[2]);
                        break;
                    case "3":
                        tvGrade.setText(gradeMeanings[3]);
                        break;
                    case "4":
                        tvGrade.setText(gradeMeanings[4]);
                        break;
                    case "5":
                        tvGrade.setText(gradeMeanings[5]);
                        break;
                }
                if (!grade.equals("- Odaberite ocjenu -")) {
                    String[] newValues = calculateInterval(grade, data[4], data[5], data[6]);
                    newData[4] = newValues[0];
                    newData[5] = newValues[1];
                    newData[6] = newValues[2];
                    newData[7] = newValues[3];

                    String interval = "Novi interval: " + newData[6];
                    String correct = "Broj uzastopnih točnih odgovora: " + newData[4];
                    tvInterval.setText(interval);
                    tvCorrect.setText(correct);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnNastavi = (Button) root.findViewById(R.id.nastavi_btn2);
        btnNastavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int success = dbHelper.updateCard(newData[0], newData[4], newData[5], newData[6], newData[7]);

                Bundle bundle = new Bundle();
                bundle.putInt("category_id", Integer.parseInt(newData[3]));
                FragmentManager fragmentManager = getFragmentManager();
                GradingQuestionFragment gradingQuestionFragment = new GradingQuestionFragment();
                gradingQuestionFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, gradingQuestionFragment).commit();
            }
        });
        return root;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String choice = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public String[] calculateInterval(String grade, String correct, String ef, String interval) {
        int gradeInt = Integer.parseInt(grade);
        int correctInt = Integer.parseInt(correct);
        double efDouble = Float.parseFloat(ef);
        int intervalInt = Integer.parseInt(interval);
        if (gradeInt >= 3) {
            if (correctInt == 0) {
                intervalInt = 1;
            } else if (correctInt == 1) {
                intervalInt = 6;
            } else {
                if (intervalInt == 0) {
                    intervalInt = 1;
                }
                if (efDouble == 0) {
                    efDouble = 2.5;
                }
                intervalInt = (int) Math.ceil(intervalInt * efDouble);
            }
            if (efDouble == 0) {
                efDouble = 2.5;
            }
            efDouble = efDouble + (0.1 - (5 - gradeInt) * (0.08 + (5 - gradeInt) * 0.02));
            if (efDouble<1.3){
                efDouble = 1.3;
            }
            correctInt++;
        } else {
            efDouble = 2.5;
            correctInt = 0;
            intervalInt = 0;
        }
        ef = new DecimalFormat("#.##").format(efDouble);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(date); // Using today's date
        c.add(Calendar.DATE, intervalInt);
        String dateString = formatter.format(c.getTime());
        String[] resultString = new String[]{String.valueOf(correctInt), ef, String.valueOf(intervalInt), dateString};
        return resultString;
    }
}
