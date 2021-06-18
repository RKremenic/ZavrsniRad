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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.zavrsnirad2.DBHelper;
import com.example.zavrsnirad2.MainActivity;
import com.example.zavrsnirad2.R;

public class HomeFragment extends Fragment {

    private Button btnZapocni;
    private ListView lvTeme;
    private String categoryString;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        if (((MainActivity) getActivity()).getCurrrentUser().isEmpty()) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment, new LoginFragment()).commit();
        }

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        lvTeme = root.findViewById(R.id.lv_teme_home);

        DBHelper dbHelper = new DBHelper(getActivity());
        String categoryNames = dbHelper.getCategoryNames(true);
        String[] categories = categoryNames.split("#");
        ArrayAdapter categoryArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_selectable_list_item,
                categories);
        lvTeme.setAdapter(categoryArrayAdapter);


        lvTeme.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                String categoryName = (String) lvTeme.getItemAtPosition(position);
                int categoryID = dbHelper.getCategoryID(categoryName);
                bundle.putInt("category_id", categoryID);
                FragmentManager fragmentManager = getFragmentManager();
                CategoryFragment categoryFragment = new CategoryFragment();
                categoryFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, categoryFragment).commit();
                return true;
            }
        });

        lvTeme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                categoryString = (String) lvTeme.getItemAtPosition(position);
            }
        });

        btnZapocni = (Button) root.findViewById(R.id.zapocni_ucenje_btn);
        btnZapocni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                int categoryID = dbHelper.getCategoryID(categoryString);
                bundle.putInt("category_id", categoryID);
                FragmentManager fragmentManager = getFragmentManager();
                StudyOptionsFragment studyOptionsFragment = new StudyOptionsFragment();
                studyOptionsFragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, studyOptionsFragment).commit();
            }
        });
        return root;
    }
}