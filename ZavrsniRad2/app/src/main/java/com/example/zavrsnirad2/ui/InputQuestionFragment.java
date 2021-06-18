package com.example.zavrsnirad2.ui;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.R;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InputQuestionFragment extends Fragment {

    private Button btnNeZnam, btnProvjeri;
    private EditText upisivanje;
    private TextView tvQuestion, tvInterval, tvCorrect;
    private String[] newData;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_input_question, container, false);
        Bundle bundle = this.getArguments();
        int categoryID = bundle.getInt("category_id");
        DBHelper dbHelper = new DBHelper(getActivity());
        String card = dbHelper.getStudyCard(categoryID);
        String[] data = card.split("#");
        newData = card.split("#");
        String correct = "Broj uzastopnih točnih odgovora: " + data[4];
        String interval = "Trenutni interval: " + data[6];

        tvQuestion = root.findViewById(R.id.tekst_pitanja3);
        tvQuestion.setText(data[1]);
        tvCorrect = root.findViewById(R.id.broj_tocnih3);
        tvCorrect.setText(correct);
        tvInterval = root.findViewById(R.id.trenutni_interval3);
        tvInterval.setText(interval);

        btnNeZnam = (Button) root.findViewById(R.id.ne_znam_btn);
        btnNeZnam.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Bundle bundle = getBundle(data, dbHelper, card, "0");

                FragmentManager fragmentManager = getFragmentManager();
                InputDontKnowFragment inputDontKnowFragment = new InputDontKnowFragment();
                inputDontKnowFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, inputDontKnowFragment).commit();
            }
        });
        btnProvjeri = (Button) root.findViewById(R.id.provjeri_btn);
        upisivanje = root.findViewById(R.id.upisi_odgovor);
        btnProvjeri.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                String userInput = upisivanje.getText().toString();
                if (userInput.toUpperCase().equals(data[2].toUpperCase())) {
                    Bundle bundle = getBundle(data, dbHelper, card, "5");
                    bundle.putString("userInput", userInput);
                    InputCorrectFragment inputCorrectFragment = new InputCorrectFragment();
                    inputCorrectFragment.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, inputCorrectFragment).commit();
                } else {
                    Bundle bundle = getBundle(data, dbHelper, card, "2");
                    bundle.putString("userInput", userInput);
                    InputIncorrectFragment inputIncorrectFragment = new InputIncorrectFragment();
                    inputIncorrectFragment.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, inputIncorrectFragment).commit();
                }
            }
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NotNull
    private Bundle getBundle(String[] data, DBHelper dbHelper, String card, String grade) {
        Bundle bundle = new Bundle();
        String[] newValues = calculateInterval(grade, data[4], data[5], data[6]);
        newData[4] = newValues[0];
        newData[5] = newValues[1];
        newData[6] = newValues[2];
        newData[7] = newValues[3];
        int success = dbHelper.updateCard(newData[0], newData[4], newData[5], newData[6], newData[7]);
        String newCard = String.join("#", newData);
        bundle.putString("card", newCard);
        return bundle;
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
