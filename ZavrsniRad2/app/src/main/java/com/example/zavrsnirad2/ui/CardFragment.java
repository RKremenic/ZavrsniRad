package com.example.zavrsnirad2.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.MainActivity;
import com.example.zavrsnirad2.R;
import com.example.zavrsnirad2.ui.CategoryFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CardFragment extends Fragment {

    private Button btnPovratak;
    private TextView tv_question, tv_answer, tvPoints, tvRank, tvBest, tvWorst, tvAverage, tvMedian, tvInterval, tvCorrect;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_card, container, false);
        Bundle bundle = this.getArguments();
        int cardID = bundle.getInt("card_id");
        tv_question = root.findViewById(R.id.pitanje8);
        tv_answer = root.findViewById(R.id.odgovor8);
        DBHelper dbHelper = new DBHelper(getActivity());
        float myPoints = dbHelper.getCardPoints(cardID);

        String qAndA = dbHelper.getCardData(cardID);
        tv_question.setText(qAndA.split("#")[0]);
        tv_answer.setText(qAndA.split("#")[1]);

        if (myPoints != -1) {
            tvPoints = root.findViewById(R.id.skupljeni_bodovi8);
            String points = "Moji bodovi: " + String.valueOf(myPoints);
            tvPoints.setText(points);
            tvRank = root.findViewById(R.id.rang8);
            tvBest = root.findViewById(R.id.bodovi_najboljeg8);
            tvWorst = root.findViewById(R.id.bodovi_najlosijeg8);
            tvAverage = root.findViewById(R.id.prosjecni_bodovi8);
            tvMedian = root.findViewById(R.id.medijan8);
            tvInterval = root.findViewById(R.id.novi_interval8);
            tvCorrect = root.findViewById(R.id.broj_tocnih8);


            String intervalAndCorrect = dbHelper.getIntervalAndCorrect(cardID);
            String interval = "Trenutni interval: " + intervalAndCorrect.split("#")[0];
            String correct = "Broj točnih odgovora: " + intervalAndCorrect.split("#")[1];
            tvCorrect.setText(correct);
            tvInterval.setText(interval);


            OkHttpClient client = new OkHttpClient();
            String getCardStatistics = "http://18.116.203.161:5000/getCardStatistics/" + cardID + "/" + ((MainActivity) getActivity()).getCurrrentUser();

            Request request = new Request.Builder()
                    .url(getCardStatistics)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Error getting card statistics", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseString = response.body().string();
                    if (responseString.isEmpty()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String rank = "Rang: 1/1";
                                tvRank.setText(rank);
                                String best = "Bodovi najboljeg korisnika: " + String.valueOf(myPoints);
                                tvBest.setText(best);
                                String worst = "Bodovi najlošijeg korisnika: " + String.valueOf(myPoints);
                                tvWorst.setText(worst);
                                String average = "Prosječni broj bodova: " + String.valueOf(myPoints);
                                tvAverage.setText(average);
                                String medianString = "Medijan: " + String.valueOf(myPoints);
                                tvMedian.setText(medianString);
                            }
                        });
                    } else {
                        responseString += "#" + myPoints;
                        List<String> list = Arrays.asList(responseString.split("#"));
                        Collections.sort(list, Collections.reverseOrder());
                        //List<String> sortedList = list.stream().sorted().collect(Collectors.toList());
                        float avg = myPoints;
                        String min = list.get(list.size() - 1);
                        String max = list.get(0);
                        String median = list.get(list.size() / 2);
                        for (String points : list) {
                            avg += Float.parseFloat(points);
                        }
                        avg /= (list.size() + 1);
                        DecimalFormat df2 = new DecimalFormat("#.##");
                        final String avgString = df2.format(avg);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String rank = "Rang: " + (list.indexOf(String.valueOf(myPoints))+1) + "/" + list.size();
                                tvRank.setText(rank);
                                String best = "Bodovi najboljeg korisnika: " + max;
                                tvBest.setText(best);
                                String worst = "Bodovi najlošijeg korisnika: " + min;
                                tvWorst.setText(worst);
                                String average = "Prosječni broj bodova: " + avgString;
                                tvAverage.setText(average);
                                String medianString = "Medijan: " + median;
                                tvMedian.setText(medianString);
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Niste učili ovu karticu", Toast.LENGTH_SHORT).show();
        }




        btnPovratak = (Button) root.findViewById(R.id.povratak_na_temu_btn);
        btnPovratak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                int category_id = dbHelper.getCardsCategoryID(cardID);
                bundle.putInt("category_id", category_id);
                FragmentManager fragmentManager = getFragmentManager();
                CategoryFragment categoryFragment = new CategoryFragment();
                categoryFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, categoryFragment).commit();

            }
        });

        return root;
    }
}
