package com.acarreos.creative.CustomViews;

/**
 * Created by EnmanuelPc on 05/09/2015.
 */

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.acarreos.creative.R;

public class RadioButtonMaterial extends android.widget.RadioButton {
    private CheckeableDrawable drawable;

    public RadioButtonMaterial(Context context) {
        this(context, null);
    }

    public RadioButtonMaterial(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.radioButtonStyle);
    }

    public RadioButtonMaterial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public void init(AttributeSet attrs, int defStyleAttr) {
        if (isInEditMode())
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RadioButton, defStyleAttr, 0);

        drawable = new CheckeableDrawable(getContext(), R.raw.carbon_radiobutton_checked, R.raw.carbon_radiobutton_unchecked, R.raw.carbon_radiobutton_filled, new PointF(0, 0));
        setButtonDrawable(getResources().getDrawable(android.R.color.transparent));
        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        ColorStateList csl = a.getColorStateList(R.styleable.RadioButton_carbon_radioColor);
        if (csl != null)
            drawable.setColor(csl);

        setCheckedImmediate(isChecked());

        a.recycle();
    }

    public void setCheckedImmediate(boolean checked) {
        super.setChecked(checked);
        drawable.setCheckedImmediate(checked);
    }

}
