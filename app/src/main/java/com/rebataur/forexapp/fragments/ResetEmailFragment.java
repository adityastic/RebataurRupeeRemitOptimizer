package com.rebataur.forexapp.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.rebataur.forexapp.R;
/**
 * A placeholder fragment containing a simple view.
 */
public class ResetEmailFragment extends Fragment {

    public EditText email;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        email = view.findViewById(R.id.textEmail);
    }

    public ResetEmailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_1, container, false);

    }

    public static Fragment newInstance() {
        return new ResetEmailFragment();
    }
}
