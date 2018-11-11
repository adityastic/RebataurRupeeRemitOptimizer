package com.rebataur.forexapp.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.rebataur.forexapp.R;
import com.scottyab.showhidepasswordedittext.ShowHidePasswordEditText;

/**
 * A placeholder fragment containing a simple view.
 */
public class ResetPasswordFragment extends Fragment {

    public EditText pass;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pass = view.findViewById(R.id.password);
    }

    public ResetPasswordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_2, container, false);

    }

    public static Fragment newInstance() {
        return new ResetPasswordFragment();
    }
}
