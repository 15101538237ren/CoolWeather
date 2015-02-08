package com.buaa.renhonglei.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	public static final int DEBUG=1;
	public static final int RELEASE=2;
	public static int MODE=RELEASE;
	public static void makeText(Context context,CharSequence sequence,int duration) {
		if(MODE==DEBUG)//只在调试模式下弹出
			Toast.makeText(context, sequence, duration).show();
	}
}
