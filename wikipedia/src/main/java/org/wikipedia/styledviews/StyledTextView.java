package org.wikipedia.styledviews;

import android.content.*;
import android.util.*;
import android.widget.*;
import org.wikipedia.*;

public class StyledTextView extends TextView {
    public StyledTextView(Context context) {
        this(context, null);
    }

    public StyledTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public StyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            setTypeface(((WikipediaApp)context.getApplicationContext()).getPrimaryType());
        }
    }
}
