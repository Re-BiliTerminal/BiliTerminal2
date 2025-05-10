package com.huanli233.biliterminal2.ui.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import com.huanli233.biliterminal2.R;

public class MaterialSwitchPreference extends SwitchPreferenceCompat {

    public MaterialSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MaterialSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_MaterialSwitchPreference);
    }

    @SuppressLint("PrivateResource")
    public MaterialSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.materialSwitchPreferenceStyle);
    }

    public MaterialSwitchPreference(@NonNull Context context) {
        this(context, null);
    }

}