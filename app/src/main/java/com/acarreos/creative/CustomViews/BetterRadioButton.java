package com.acarreos.creative.CustomViews;

import android.content.Context;
import android.widget.CompoundButton;

/**
 * Created by EnmanuelPc on 05/09/2015.
 */
public class BetterRadioButton extends RadioButtonMaterial {
    private CompoundButton.OnCheckedChangeListener myListener = null;
    private RadioButtonMaterial myRadioButton;

    public BetterRadioButton(Context context) {
        super(context);
    }

    public BetterRadioButton(Context context, RadioButtonMaterial radioButton) {
        this(context);
        this.myRadioButton = radioButton;
    }

    // assorted constructors here...

    @Override
    public void setOnCheckedChangeListener(
            CompoundButton.OnCheckedChangeListener listener) {
        if (this.myListener == null)
            this.myListener = listener;
        myRadioButton.setOnCheckedChangeListener(listener);
    }

    public void silentlySetChecked(boolean checked) {
        toggleListener(false);
        myRadioButton.setChecked(checked);
        toggleListener(true);
    }

    private void toggleListener(boolean on) {
        if (on) {
            this.setOnCheckedChangeListener(myListener);
        } else
            this.setOnCheckedChangeListener(null);
    }
}
