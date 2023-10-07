package be.kuleuven.vinter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.slider.RangeSlider;

import be.kuleuven.vinter.activities.MainActivity;

public class FilterFragment extends Fragment {

    private View view;
    public Spinner myGenderFilter, myCategoryFilter, myColorFilter;
    public RangeSlider myPriceFilter, mySizeFilter;
    public EditText myBrandFilter;

    private Button myClrPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_filter, container, false);

        myGenderFilter = view.findViewById(R.id.genderFragment);
        myCategoryFilter = view.findViewById(R.id.categoryFragment);
        myColorFilter = view.findViewById(R.id.colorFragment);
        myPriceFilter = view.findViewById(R.id.seekBarPrice);
        mySizeFilter = view.findViewById(R.id.seekBarSize);
        myBrandFilter = view.findViewById(R.id.brandFilter);
        myClrPref = view.findViewById(R.id.clrPref);

        myPriceFilter.setStepSize(1);
        mySizeFilter.setStepSize(1);

        if (MainActivity.prefBrand == null) {
            myBrandFilter.setText("");
        } else {
            myBrandFilter.setText(MainActivity.prefBrand);
        }

        if (MainActivity.prefGender == null) {
            myGenderFilter.setSelection(getIndex(myGenderFilter, "Choose gender"));
        } else {
            myGenderFilter.setSelection(getIndex(myGenderFilter, MainActivity.prefGender));
        }

        if (MainActivity.prefColor == null) {
            myColorFilter.setSelection(getIndex(myGenderFilter, "Choose color"));
        } else {
            myColorFilter.setSelection(getIndex(myColorFilter, MainActivity.prefColor));
        }
        if (MainActivity.prefCategory == null) {
            myCategoryFilter.setSelection(getIndex(myCategoryFilter, "Choose color"));
        } else {
            myCategoryFilter.setSelection(getIndex(myCategoryFilter, MainActivity.prefCategory));
        }

        myPriceFilter.setValues(Float.parseFloat(MainActivity.minPrice), Float.parseFloat(MainActivity.maxPrice));
        mySizeFilter.setValues(Float.parseFloat(MainActivity.minSize), Float.parseFloat(MainActivity.maxSize));

        return view;
    }

    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button filter = view.findViewById(R.id.filterNow);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filtered = true;
                MainActivity.prefBrand = myBrandFilter.getText().toString();
                MainActivity.prefColor = myColorFilter.getSelectedItem().toString();
                MainActivity.prefCategory = myCategoryFilter.getSelectedItem().toString();
                MainActivity.prefGender = myGenderFilter.getSelectedItem().toString();
                MainActivity.minPrice = Float.toString(myPriceFilter.getValues().get(0));
                MainActivity.maxPrice = Float.toString(myPriceFilter.getValues().get(1));
                MainActivity.minSize = Float.toString(mySizeFilter.getValues().get(0));
                MainActivity.maxSize = Float.toString(mySizeFilter.getValues().get(1));
            }
        });

        myClrPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPreferences();
            }
        });
    }


    private void clearPreferences(){
        MainActivity.prefBrand = null;
        MainActivity.prefColor = null;
        MainActivity.prefCategory = null;
        MainActivity.prefGender = null;
        MainActivity.minPrice = "0";
        MainActivity.maxPrice = "500";
        MainActivity.minSize = "0";
        MainActivity.maxSize = "50";

        myBrandFilter.setText("");
        myGenderFilter.setSelection(getIndex(myGenderFilter, "Choose gender"));
        myColorFilter.setSelection(getIndex(myGenderFilter, "Choose color"));
        myCategoryFilter.setSelection(getIndex(myCategoryFilter, "Choose color"));
        myPriceFilter.setValues((float) 0, (float) 500);
        mySizeFilter.setValues((float) 0, (float) 50);
    }
}