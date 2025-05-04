package com.huanli233.biliterminal2.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class ExposedDropdownMenu extends MaterialAutoCompleteTextView {

	public ExposedDropdownMenu(@NonNull final Context context, @Nullable final AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	public boolean getFreezesText() {
		return false;
	}
}