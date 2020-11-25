package com.s1243808733.java2beans.util;
import android.content.res.Resources;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.s1243808733.java2beans.App;
import com.s1243808733.java2beans.R;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Tools {

	public static App getApp() {
		return App.getApp();
	}

	public static String escapeJava(String data) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < data.length(); i++) {
			char c = data.charAt(i);
			switch (c) {
				case '"':
					result.append("\\\"");
					break;
				case '\\':
					result.append("\\\\");
					break;
				case '\b':
					result.append("\\b");
					break;
				case '\t':
					result.append("\\t");
					break;
				case '\n':
					result.append("\\n");
					break;
				case '\f':
					result.append("\\f");
					break;
				case '\r':
					result.append("\\r");
					break;
				default:
					if (Character.isISOControl(c)) {
						result.append(String.format("\\u%04x", (int) c));
					} else {
						result.append(c);
					}
			}
		}
		return result.toString();
	}

	public static String millis2FitTimeSpan(long millis, int precision) {
        if (precision <= 0) return null;
        precision = Math.min(precision, 5);
		Resources res = getApp().getResources();
        String[] units = new String[]{
            res.getString(R.string.time_unit_d),
            res.getString(R.string.time_unit_h),
            res.getString(R.string.time_unit_m),
            res.getString(R.string.time_unit_s),
            res.getString(R.string.time_unit_ms)};
        if (millis == 0) return 0 + units[precision - 1];
        StringBuilder sb = new StringBuilder();
        if (millis < 0) {
            sb.append("-");
            millis = -millis;
        }
        int[] unitLen = new int[]{
            86400000,
            3600000, 
            60000, 
            1000, 
            1};
        for (int i = 0; i < precision; i++) {
            if (millis >= unitLen[i]) {
                long mode = millis / unitLen[i];
                millis -= mode * unitLen[i];
                sb.append(mode).append(units[i]);
            }
        }
        return sb.toString();
    }

    public static void copyToClipboard(CharSequence text, boolean showToast) {
        ClipboardUtils.copyText(text);
        if (showToast) ToastUtils.showShort(R.string.message_copied_to_clipboard);
    }

    public static void copyToClipboard(CharSequence text) {
        copyToClipboard(text, true);
	}

    public static String getFullStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
