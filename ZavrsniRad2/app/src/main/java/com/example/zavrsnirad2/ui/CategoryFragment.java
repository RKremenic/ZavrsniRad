package com.example.zavrsnirad2.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CategoryFragment extends Fragment {

    private Button btnSpremiNapredak;
    private ListView lvCards;
    private TextView tvPoints, tvRank, tvBest, tvWorst, tvAverage, tvMedian, tvCategory;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_category, container, false);

        String currentUser = ((MainActivity) getActivity()).getCurrrentUser();
        Bundle bundle = this.getArguments();
        int categoryID = bundle.getInt("category_id");
        lvCards = root.findViewById(R.id.lv_kartice);
        tvCategory = root.findViewById(R.id.tema7);
        DBHelper dbHelper = new DBHelper(getActivity());
        String categoryName = dbHelper.getCategoryName(categoryID);
        tvCategory.setText(categoryName);


        List<String> categoryNames = dbHelper.getCardQuestions(categoryID);
        ArrayAdapter categoryArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_selectable_list_item, categoryNames);
        lvCards.setAdapter(categoryArrayAdapter);

        float myPoints = dbHelper.getCategoryPoints(categoryID);
        if (myPoints != -1) {
            tvPoints = root.findViewById(R.id.skupljeni_bodovi7);
            String points = "Moji bodovi: " + String.valueOf(myPoints);
            tvPoints.setText(String.valueOf(points));
            tvRank = root.findViewById(R.id.rang7);
            tvBest = root.findViewById(R.id.bodovi_najboljeg7);
            tvWorst = root.findViewById(R.id.bodovi_najlosijeg7);
            tvAverage = root.findViewById(R.id.prosjecni_bodovi7);
            tvMedian = root.findViewById(R.id.medijan7);

            OkHttpClient client = new OkHttpClient();
            String getCategoryPointsURL = "http://18.116.203.161:5000/getCategoryPoints/" + categoryID + "/" + currentUser;

            Request request = new Request.Builder()
                    .url(getCategoryPointsURL)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Error getting category statistics", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), "Niste još učili ovu temu", Toast.LENGTH_SHORT).show();
        }

        lvCards.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                String question = (String) lvCards.getItemAtPosition(position);
                int cardID = dbHelper.getCardID(question);
                bundle.putInt("card_id", cardID);
                FragmentManager fragmentManager = getFragmentManager();
                CardFragment cardFragment = new CardFragment();
                cardFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, cardFragment).commit();
            }
        });
        btnSpremiNapredak = root.findViewById(R.id.spremi_napredak_btn);
        btnSpremiNapredak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myPoints != -1) {
                    Toast.makeText(getContext(), "Molimo pričekajte", Toast.LENGTH_SHORT).show();
                    String progress = dbHelper.getCategoryProgress(categoryID);
                    String saveProgressURL = "http://18.116.203.161:5000/saveProgress";

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("username", currentUser)
                            .addFormDataPart("progress", progress)
                            .build();
                    Request request = new Request.Builder()
                            .url(saveProgressURL)
                            .post(requestBody)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), "failed to save progress", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            String myResponse = response.body().string();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Bodovi su pohranjeni", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "Prvo morate učiti ovu temu", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }
}
