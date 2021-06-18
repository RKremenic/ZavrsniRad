package com.example.zavrsnirad2.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.zavrsnirad2.MainActivity.PREFS_NAME;

public class LogoutFragment extends Fragment {

    private Button btnOdjaviSe, btnPovratak;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_logout, container, false);


        btnPovratak = (Button) root.findViewById(R.id.povratak_btn2);
        btnPovratak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                fragmentTransaction.commit();

            }
        });

        btnOdjaviSe = (Button) root.findViewById(R.id.odjavi_se_btn);
        btnOdjaviSe.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DBHelper dbHelper = new DBHelper(getActivity());
                String categoryNames = dbHelper.getCategoryNames(true);
                if (!categoryNames.startsWith("Niste")) {
                    String[] categories = categoryNames.split("#");
                    String currentUser = ((MainActivity) getActivity()).getCurrrentUser();
                    for (String category : categories) {
                        int categoryID = dbHelper.getCategoryID(category);
                        float myPoints = dbHelper.getCategoryPoints(categoryID);
                        if (myPoints != -1) {
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

                                }
                            });
                        }
                    }
                }
                Context context = getContext();
                context.deleteDatabase("zavrsni.db");

                SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("currentUser", ""); // value to store
                editor.commit();
                ((MainActivity) getActivity()).setCurrrentUser("");
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                fragmentTransaction.commit();

            }
        });

        return root;
    }
}
