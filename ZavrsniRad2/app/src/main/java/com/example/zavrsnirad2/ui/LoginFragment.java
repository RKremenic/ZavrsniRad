package com.example.zavrsnirad2.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.MainActivity;
import com.example.zavrsnirad2.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.Certificate;
import javax.security.cert.CertificateException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.zavrsnirad2.MainActivity.PREFS_NAME;

public class LoginFragment extends Fragment {
    private EditText etUsername, etPassword;
    private TextView tvRegister;
    private Button btnLogin;
    private String[] users;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        etUsername = root.findViewById(R.id.username);
        etPassword = root.findViewById(R.id.password);
        tvRegister = root.findViewById(R.id.tvRegister);
        btnLogin = root.findViewById(R.id.login_btn);

        OkHttpClient client = new OkHttpClient();
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
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, new RegisterFragment()).commit();
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean exists = false;
                String inputUsername = etUsername.getText().toString();
                String inputPassword = etPassword.getText().toString();
                for (String user : users) {
                    String[] userAttrs = user.split("#");
                    String username = userAttrs[0];
                    String password = userAttrs[2];
                    if (username.equals(inputUsername) && password.equals(inputPassword)) {
                        exists = true;
                        SharedPreferences preferences = getActivity().getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("currentUser", inputUsername); // value to store
                        editor.commit();
                        ((MainActivity) getActivity()).setCurrrentUser(inputUsername);
                        break;
                    }
                }
                if (!exists) {
                    Toast.makeText(getActivity(), "Pogrešno korisničko ime i/ili lozinka", Toast.LENGTH_SHORT).show();
                } else {
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                }
            }
        });

        return root;
    }


}
