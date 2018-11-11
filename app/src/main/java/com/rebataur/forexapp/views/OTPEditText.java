package com.rebataur.forexapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.rebataur.forexapp.R;

public class OTPEditText extends LinearLayout {

    public int entryCount = 4; //count of boxes to be created
    private int currentIndex = 0;
    private static int EDITTEXT_MAX_LENGTH = 1; //character size of each editext
    private static int EDITTEXT_WIDTH = 40;
    private static int EDITTEXT_TEXTSIZE = 35; //textsize
    private boolean disableTextWatcher = false, backKeySet = false;
    private TextWatcher txtWatcher;
    private onFinishListerner mListerner;


    public OTPEditText(Context context) {
        super(context, null);
    }

    public OTPEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OTPEditText(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public OTPEditText(Context context, AttributeSet attrs, int defStyle, int defStyleRes) {
        super(context, attrs);
        init(context, attrs);
    }

    public void setOnFinishListerner(onFinishListerner listerner) {
        this.mListerner = listerner;
    }

    public interface onFinishListerner {
        void onFinish(String enteredText);
    }


    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.CustomEntryEdittext, 0, 0);

        a.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        for (int i = 0; i < entryCount; i++) {

            //creates edittext based on the no. of count
            addView(initialiseAndAddChildInLayout(i, context), i);
        }

    }

    //method focuses of previous editext
    private void getPreviousEditext(int index) {
        if (index > 0) {
            EditText edtxt = (EditText) getChildAt(index - 1);
            disableTextWatcher = true;

            edtxt.setText("");
            edtxt.requestFocus();
            disableTextWatcher = false;

        }
    }

    //method focuses of previous editext
    private void getPreviousEditextFocus(int index) {
        if (index > 0) {
            EditText edtxt = (EditText) getChildAt(index - 1);
            disableTextWatcher = true;
            edtxt.requestFocus();
            disableTextWatcher = false;
        }
    }


    //method to focus on next edittext
    private void getNextEditext(int index) {
        if (index < entryCount - 1) {
            EditText edtxt = (EditText) getChildAt(index + 1);
            edtxt.requestFocus();
        }
    }


    private View initialiseAndAddChildInLayout(int index, Context context) {
        final EditText editext = new EditText(context);
        editext.setMaxWidth(1);
        editext.setTag(index);
        editext.setGravity(Gravity.CENTER);
        editext.setTextSize(EDITTEXT_TEXTSIZE);
        editext.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        editext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(EDITTEXT_MAX_LENGTH)});
        LayoutParams param = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        editext.setLayoutParams(param);
        editext.addTextChangedListener(txtWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentIndex = Integer.parseInt(editext.getTag().toString());


                if (editext.getText().toString().length() == 1 && !disableTextWatcher) {
                    getNextEditext(currentIndex);
                } else if (editext.getText().toString().length() == 0 && !disableTextWatcher) {// && !isFirstTimeGetFocused && !backKeySet) {
                    getPreviousEditext(currentIndex);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        editext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if(currentIndex==entryCount-1 && getEnteredText().length()==entryCount)
                    {
                        mListerner.onFinish(getEnteredText());
                    }
                }
                return false;
            }
        });

        return editext;
    }


    public String getEnteredText() {
        String strEnteredValue = "";
        for (int i = 0; i < getChildCount(); i++) {
            EditText editText = (EditText) getChildAt(i);
            if (editText.getText() != null && editText.getText().toString().length() > 0)
                strEnteredValue = strEnteredValue + editText.getText().toString();

        }
        return strEnteredValue;
    }

    public void clearOTPEditText() {
        for (int i = 0; i < getChildCount(); i++) {
            EditText editText = (EditText) getChildAt(i);
            editText.setText("");
        }
        EditText editText = (EditText) getChildAt(0);
        editText.requestFocus();
    }
}
