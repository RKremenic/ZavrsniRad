package com.example.zavrsnirad2.ui;

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
import androidx.fragment.app.Fragment;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.MainActivity;
import com.example.zavrsnirad2.R;
import com.example.zavrsnirad2.models.CardModel;
import com.example.zavrsnirad2.models.CategoryModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadFragment extends Fragment {

    private Button btnPreuzmi;
    private ListView lvTeme;
    private TextView test;
    private String categoryString;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_download, container, false);
        test = root.findViewById(R.id.test);

        final String[] testString = new String[1];
        lvTeme = root.findViewById(R.id.lv_teme);



        OkHttpClient client = new OkHttpClient();
        String getCategoriesURL = "http://18.116.203.161:5000/categories";

        Request request = new Request.Builder()
                .url(getCategoriesURL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                toastMethod("Failed to get category list");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseString = response.body().string();
                String[] categories = responseString.split(";");
                DBHelper dbHelper = new DBHelper(getActivity());
                List<Integer> categoryIDs = dbHelper.getCategoryIDs();

                for (String category : categories) {
                    String[] categoryAttrs = category.split("#");
                    int categoryId = Integer.parseInt(categoryAttrs[0]);
                    String categoryName = categoryAttrs[1];
                    CategoryModel categoryModel = null;
                    if (!categoryIDs.contains(Integer.parseInt(categoryAttrs[0]))){
                        try {
                            categoryModel = new CategoryModel(categoryId, categoryName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        boolean insert = dbHelper.addCategory(categoryModel);
                    } else {
                        continue;
                    }
                }

                String categoryNames = dbHelper.getCategoryNames(false);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] categories = categoryNames.split("#");
                        ArrayAdapter categoryArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, categories);
                        lvTeme.setAdapter(categoryArrayAdapter);
                    }
                });




            }
        });

        lvTeme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryString = (String) lvTeme.getItemAtPosition(position);
            }
        });


        btnPreuzmi = (Button) root.findViewById(R.id.preuzmi_btn);
        btnPreuzmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = ((MainActivity) getActivity()).getCurrrentUser();
                String getCardsURL = "http://18.116.203.161:5000/getcards/" + categoryString + "/" + user;
                String checkRelationshipsURL = "http://18.116.203.161:5000/checkRelationships/" + user + "/" + categoryString;
                String createRelationshipsURL = "http://18.116.203.161:5000/createRelationships/" + user + "/" + categoryString;
                Request request = new Request.Builder().url(checkRelationshipsURL).build();
                DBHelper dbHelper = new DBHelper(getActivity());
                String categoryNames = dbHelper.getCategoryNames(true);
                String[] categoryArray = categoryNames.split("#");
                for (String category : categoryArray) {
                    if (category.equals(categoryString)) {
                        toastMethod("Već ste preuzeli ovu temu");
                        return;
                    }
                }
                Toast.makeText(getContext(), "Molimo pričekajte", Toast.LENGTH_SHORT).show();


                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        toastMethod("Failed to check Relationships");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.body().string().equals("False")) {
                            Request request = new Request.Builder().url(createRelationshipsURL).build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    toastMethod("Failed to create Relationships");
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                    if (!response.body().string().startsWith("Added relationships for:")) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "Pogreska pri preuzimanju kartica", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                        Request request = new Request.Builder().url(getCardsURL).build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                String responseString = response.body().string();
                                String[] cards = responseString.split(";");
                                boolean success = true;
                                for (String card : cards) {
                                    String[] cardAttrs = card.split("#");
                                    int cardID = Integer.parseInt(cardAttrs[0]);
                                    String question = cardAttrs[2];
                                    String answer = cardAttrs[3];
                                    int categoryID = Integer.parseInt(cardAttrs[4]);
                                    int correct, interval;
                                    float ef;
                                    if (cardAttrs[5].equals("None")) {
                                        correct = -1;
                                        interval = -1;
                                        ef = -1;
                                    } else {
                                        correct = Integer.parseInt(cardAttrs[5]);
                                        ef = Float.parseFloat(cardAttrs[6]);
                                        interval = Integer.parseInt(cardAttrs[7]);
                                    }
                                    String date = cardAttrs[8];
                                    CardModel cardModel = null;
                                    try {
                                        cardModel = new CardModel(cardID, question, answer, categoryID, correct, ef, interval, date);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    boolean insert = dbHelper.addCards(cardModel);
                                    if (!insert) {
                                        success = false;
                                    }
                                }
                                if (success) {
                                    toastMethod("Preuzimanje kartica je uspjelo");
                                } else {
                                    toastMethod("Preuzimanje kartica nije uspjelo");
                                }
                            }
                        });
                    }
                });



            }
        });


        return root;
    }

    private void toastMethod(String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
