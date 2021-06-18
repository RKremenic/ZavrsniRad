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

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.R;
import com.example.zavrsnirad2.ui.HomeFragment;

public class EndFragment extends Fragment {

    private Button btnPovratak;
    private TextView tvEnd;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_end, container, false);
        Bundle bundle = this.getArguments();
        int categoryID = bundle.getInt("categoryID");
        DBHelper dbHelper = new DBHelper(getActivity());
        String categoryName = dbHelper.getCategoryName(categoryID);
        tvEnd = root.findViewById(R.id.sve_za_danas);
        String endString = "Završili ste s učenjem teme \"" + categoryName + "\" za danas";
        tvEnd.setText(endString);

        btnPovratak = (Button) root.findViewById(R.id.povratak_btn);
        btnPovratak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, new HomeFragment());
                fragmentTransaction.commit();
            }
        });

        return root;
    }
}
