package com.example.zavrsnirad2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.R;
import com.example.zavrsnirad2.models.CategoryModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterFragment extends Fragment {

    private EditText etUsername, etEmail, etPassword, etRepeat;
    private Button btnRegister;
    private String[] users;
    private String usernames, emails;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        etUsername = root.findViewById(R.id.username);
        etEmail = root.findViewById(R.id.email);
        etPassword = root.findViewById(R.id.password);
        etRepeat = root.findViewById(R.id.password2);

        OkHttpClient client = new OkHttpClient();
        String addUserURL = "http://18.116.203.161:5000/addUser";
        String getUsersURL = "http://18.116.203.161:5000/getUsers";

        Request request = new Request.Builder()
                .url(getUsersURL)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Error getting users", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseString = response.body().string();
                users = responseString.split(";");
                usernames = "";
                emails = "";

                for (String user : users){

                    String[] userAttrs = user.split("#");
                    usernames += userAttrs[0] + "#";
                    emails += userAttrs[1] + "#";
                }
                usernames = usernames.substring(0, usernames.length() - 1);
                emails = emails.substring(0, emails.length()-1);
            }
        });

        btnRegister = root.findViewById(R.id.register_btn5);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> usernameList = new ArrayList<>(Arrays.asList(usernames.split("#")));
                List<String> emailList = new ArrayList<>(Arrays.asList(emails.split("#")));
                if (etUsername.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Morate ispuniti sve podatke", Toast.LENGTH_SHORT).show();
                } else if (!etPassword.getText().toString().equals(etRepeat.getText().toString())) {
                    Toast.makeText(getActivity(), "Lozinke se moraju podudarati", Toast.LENGTH_SHORT).show();
                } else if (etUsername.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Morate ispuniti sve podatke", Toast.LENGTH_SHORT).show();
                } else if (usernameList.contains(etUsername.getText().toString())) {
                    Toast.makeText(getActivity(), "Upisano korisničko ime se več koristi", Toast.LENGTH_SHORT).show();
                } else if (emailList.contains(etEmail.getText().toString())) {
                    Toast.makeText(getActivity(), "Upisana email adresa se već koristi", Toast.LENGTH_SHORT).show();
                }else {
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("username", etUsername.getText().toString())
                            .addFormDataPart("email", etEmail.getText().toString())
                            .addFormDataPart("password", etPassword.getText().toString())
                            .build();
                    Request request = new Request.Builder()
                            .url(addUserURL)
                            .post(requestBody)
                            .build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), "failed to register", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "Registracjia je uspješna", Toast.LENGTH_SHORT).show();
                                    FragmentManager fragmentManager = getFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.nav_host_fragment, new LoginFragment()).commit();
                                }
                            });
                        }
                    });
                }
            }
        });

        return root;
    }
}
