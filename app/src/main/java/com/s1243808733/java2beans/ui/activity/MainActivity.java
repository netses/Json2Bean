package com.s1243808733.java2beans.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import androidx.annotation.Keep;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.mixiaoxiao.fastscroll.FastScrollScrollView;
import com.s1243808733.java2beans.AppSettings;
import com.s1243808733.java2beans.R;
import com.s1243808733.java2beans.common.activity.BaseActivity;
import com.s1243808733.java2beans.ui.view.BaseActivityViewHolder;
import com.s1243808733.java2beans.ui.widget.CustomButton;
import com.s1243808733.java2beans.ui.widget.SPCheckBox;
import com.s1243808733.java2beans.ui.widget.SPRadioGroup;
import com.s1243808733.java2beans.util.GsonUtils;
import com.s1243808733.java2beans.util.StringEscapeUtils;
import com.s1243808733.java2beans.util.Tools;
import com.s1243808733.library.json2bean.AnnotationStyle;
import com.s1243808733.library.json2bean.JavaBean;
import com.s1243808733.library.json2bean.Json2Bean;
import com.s1243808733.library.json2bean.OutputType;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity implements ActionBar.TabListener {

    private static final int CODE_REQUEST_WRITE_STORAGE_PERMISSION = 100;

	private final List<ActionBarTab> mActionBarTabs = new ArrayList<>();

	private BasicTabFragment   mBasicFragment;

	private ConfigTabFragment  mConfigFragment;

	private OutputTabFragment  mOutputFragment;

    private Json2Bean.Builder  mBuilder;

    private GenerationResult   mGenerationResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBuilder = new Json2Bean.Builder()
            .setUseReturnThis(true);

        initActionBar();
        initTabs();
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		try {
			Resources res = getResources();
			View decorView = getWindow().getDecorView();
			View action_bar_container = decorView.findViewById(res.getIdentifier("action_bar_container", "id", "android"));

			TypedValue typedValue = new TypedValue();
			getTheme().resolveAttribute(android.R.attr.actionBarStyle, typedValue, true);

			int[] attrs=new int[]{
				android.R.attr.background,
				android.R.attr.elevation
			};
			TypedArray array = getTheme().obtainStyledAttributes(typedValue.resourceId, attrs);
			action_bar_container.setBackgroundDrawable(array.getDrawable(0));
			action_bar_container.setElevation(array.getDimension(1, 4f));
			array.recycle();

		} catch (Throwable e) {
            LogUtils.e(e);
        }

    }

	private void initTabs() {
		ActionBar actionBar=getActionBar();

		mBasicFragment = new BasicTabFragment(this);
		mConfigFragment = new ConfigTabFragment(this);
		mOutputFragment = new OutputTabFragment(this);

		addActionBarTab(new ActionBarTab(actionBar.newTab()
										 .setTag(BasicTabFragment.TAG)
										 .setText(R.string.tab_basic)
										 , mBasicFragment));

		addActionBarTab(new ActionBarTab(actionBar.newTab()
										 .setTag(ConfigTabFragment.TAG)
										 .setText(R.string.tab_config)
										 , mConfigFragment));

		addActionBarTab(new ActionBarTab(actionBar.newTab()
										 .setTag(OutputTabFragment.TAG)
										 .setText(R.string.tab_output)
										 , mOutputFragment));

		onTabSelected(mActionBarTabs.get(0).getTab(), null);
	}

	private void selectTab(int index) {
		selectTab(mActionBarTabs.get(index));
	}

	private void selectTab(String tag) {
		for (int i = 0; i < mActionBarTabs.size(); i++) {
			ActionBarTab tab = mActionBarTabs.get(i);
			if (tag.equals((String)tab.getTab().getTag())) {
				selectTab(tab);
				return;
			}
		}
	}

	private void selectTab(ActionBarTab tab) {
		tab.getTab().select();
	}

	private void addActionBarTab(ActionBarTab tab) {
		mActionBarTabs.add(tab);
		ActionBar actionBar = getActionBar();
		actionBar.addTab(tab.getTab().setTabListener(this));
	}

	@Override
	public void onTabSelected(ActionBar.Tab p1, FragmentTransaction p2) {
		for (ActionBarTab value : mActionBarTabs) {
			ActionBar.Tab tab = value.getTab();
			value.getFragment().setVisibility(p1 == tab);
		}	
	}

	@Override
	public void onTabUnselected(ActionBar.Tab p1, FragmentTransaction p2) {

	}

	@Override
	public void onTabReselected(ActionBar.Tab p1, FragmentTransaction p2) {

	}

    private void generation() {
        updateBuilder(mBuilder);
        try {
            generation(mBuilder.create());
        } catch (Throwable e) {
            switch (e.getMessage()) {
                case "JSON parse failed":
                    ToastUtils.showShort(R.string.message_json_parse_exception);
                    break;
                case "Keyless array conversion is not supported":
                    ToastUtils.showShort(R.string.message_keyless_array_exception);
                    break;
                default:
                    ToastUtils.showShort(e.getMessage());
                    break;
            }
        }

    }

    private void generation(Json2Bean json2Bean) throws JSONException, IOException  {
        String json = mBasicFragment.viewHolder.et_json.getText().toString();
		if (StringUtils.isTrimEmpty(json)) {
			json = "{}";
		}

        JavaBean bean = json2Bean.toBean(json);
        String java = bean.toJava().trim();

        mGenerationResult = new GenerationResult();
        mGenerationResult.bean = bean;
        mGenerationResult.java = java;

		mOutputFragment.viewHolder.fastScrollView.scrollTo(0, 0);
		mOutputFragment.viewHolder.et_result.setText(java);

		selectTab(OutputTabFragment.TAG);
    }

	public boolean checkJsonIsEmpty() {
		if (TextUtils.isEmpty(mBasicFragment.viewHolder.et_json.getText())) {
            ToastUtils.showShort(R.string.message_please_input_json);
            return true;
        }
		return false;
	}

	private static class TabFragment {

		public final Activity activity;

		private boolean visibility;

		public TabFragment(Activity activity) {
			this.activity = activity;
		}

		public void setVisibility(boolean visibility) {
			this.visibility = visibility;
			BaseActivityViewHolder viewHolder = getViewHolder();
			if (viewHolder != null) {
				final View view = viewHolder.getContentView();
				if (visibility) {
					view.post(new Runnable(){

							@Override
							public void run() {
								view.requestFocus();
							}
						});
				}
				view.setVisibility(visibility ?View.VISIBLE: View.INVISIBLE);
			}
		}

		public boolean isVisibility() {
			return visibility;
		}

		public BaseActivityViewHolder getViewHolder() {
			return null;
		}

		public void bindOnclick(final Object target, View view) {
			if (view instanceof CustomButton) {
				CustomButton btn = (CustomButton)view;
				if (!TextUtils.isEmpty(btn.getClickMethodName())) {
					btn.bindClick(target);
				}
			} else if (view instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup)view;
				for (int i = 0;i < vg.getChildCount(); i++) {
					bindOnclick(target, vg.getChildAt(i));
				}
			}
		}
	}

	private class BasicTabFragment extends TabFragment implements View.OnClickListener {

        private static final String TAG = "BasicTabFragment";

		public final BasicActivityViewHolder viewHolder;

		public BasicTabFragment(Activity activity) {
			super(activity);
			viewHolder = new BasicActivityViewHolder(activity, R.id.tab_main);
			bindOnclick(this, viewHolder.getContentView());
			viewHolder.btn_more.setOnClickListener(this);
		}

        @Keep
		public void onCompressClick(View view) {
			if (checkJsonIsEmpty())return;
			try {
				String result = GsonUtils.compressionJson(mBasicFragment.viewHolder.et_json.getText().toString());
				if (!"null".equals(result)) {
					mBasicFragment.viewHolder.et_json.setText(result);
				}
			} catch (Throwable e) {
                ToastUtils.showShort(e.getMessage());
			}
		}

        @Keep
		public void onFormatClick(View view) {
			if (checkJsonIsEmpty())return;
			try {
				String result = GsonUtils.formatJson(mBasicFragment.viewHolder.et_json.getText().toString());
				if (!"null".equals(result)) {
					mBasicFragment.viewHolder.et_json.setText(result);
				}
			} catch (Throwable e) {
                ToastUtils.showShort(e.getMessage());
			}
		}

        @Keep
		public void onPasteClick(View view) {
            mBasicFragment.viewHolder.et_json.setText(ClipboardUtils.getText());
        }

        @Keep
		public void onLoadFromNetworkClick(View view) {
			Button btn = (Button) view;

			final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle(btn.getText())
				.setView(R.layout.dialog_input_url)
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null)
				.setNeutralButton(R.string.paste, null)
				.create();
			dialog.show();
			final EditText edit = dialog.findViewById(R.id.edit);
			final DialogPositiveButtonClickListener l = new DialogPositiveButtonClickListener();
			dialog.getButton(dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						l.onClick(dialog, dialog.BUTTON_POSITIVE);
					}
				});
			dialog.getButton(dialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
                        edit.setText(ClipboardUtils.getText());
					}
				});

			edit.setHint(R.string.hint_input_url);
			edit.addTextChangedListener(new TextWatcher(){

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(s.length() != 0);
					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});
			edit.setText(null);
			edit.post(new Runnable(){

					@Override
					public void run() {
						KeyboardUtils.showSoftInput(edit);
					}
				});

		}

        @Keep
		public void onClearClick(View view) {
			viewHolder.et_json.setText(null);
			viewHolder.et_json.setError(null);
		}

        @Keep
		public void onCopyClick(View view) {
			Tools.copyToClipboard(mBasicFragment.viewHolder.et_json.getText());
		}

		private class DialogPositiveButtonClickListener implements DialogInterface.OnClickListener {

            private Callback.Cancelable cancelable;

            public DialogPositiveButtonClickListener() {
            }

			@Override
			public void onClick(final DialogInterface dia, int with) {
				final AlertDialog dialog = (AlertDialog)dia;
				EditText edit = dialog.findViewById(R.id.edit);
				String input = edit.getText().toString();
				if (input.startsWith("/") || input.startsWith("file://")) {
					if (checkPermissionAndRequest()) return;
				}
				RequestParams params = new RequestParams(input);

				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                cancelable = x.http().get(params, new Callback.CommonCallback<String>() {

						@Override
						public void onSuccess(String result) {
							mBasicFragment.viewHolder.et_json.setText(result);
							dia.dismiss();
						}

						@Override
						public void onError(Throwable ex, boolean isOnCallback) {   
                            LogUtils.eTag("onError", ex);
                            ToastUtils.showShort(ex.getMessage());
                        }

						@Override
						public void onCancelled(CancelledException cex) {
						}

						@Override
						public void onFinished() {
							Button btn= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
							if (btn != null)btn.setEnabled(true);
						}
					});

				dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){

						@Override
						public void onDismiss(DialogInterface dia) {
							if (cancelable != null && !cancelable.isCancelled())cancelable.cancel();
						}
					});
			}

		}

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.btn_more:
					showMoreToolsDialog();
					break;
			}
		}

		private void showMoreToolsDialog() {
			AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle(R.string.btn_more)
				.setItems(R.array.more_tools, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dia, int which) {
						String json = viewHolder.et_json.getText().toString();
						try {
							switch (getResources().getStringArray(R.array.more_tools_key)[which]) {
								case "escape":{
										viewHolder.et_json.setText(StringEscapeUtils.escapeJava(json));
									}
									break;
								case "unescape":
									viewHolder.et_json.setText(StringEscapeUtils.unescapeJava(json));
									break;
								case "unicodeToStr":{
										viewHolder.et_json.setText(StringEscapeUtils.unicode2String(json));
									}
									break;
								case "strTounicode":
									viewHolder.et_json.setText(StringEscapeUtils.string2Unicode(json));
									break;
							}
						} catch (Throwable e) {
                            ToastUtils.showShort(e.getMessage());
						}
					}

				})
				.setPositiveButton(R.string.close, null)
				.create();
			dialog.show();
		}

		@Override
		public BaseActivityViewHolder getViewHolder() {
			return viewHolder;
		}

	}

	private class ConfigTabFragment extends TabFragment {

        private static final String TAG = "ConfigTabFragment";

		public final ConfigActivityViewHolder viewHolder;

		public ConfigTabFragment(Activity activity) {
			super(activity);
			viewHolder = new ConfigActivityViewHolder(activity, R.id.tab_main_config);
			bindOnclick(this, viewHolder.getContentView());
		}

        @Keep
		public void onResetClick(View view) {
			Field[] fields = mConfigFragment.viewHolder.getClass().getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				try {
					Object value = field.get(mConfigFragment.viewHolder);
					if (value instanceof SPCheckBox) {
						SPCheckBox cb = (SPCheckBox)value;
						if (cb.getSp() != null && !TextUtils.isEmpty(cb.getKey())) {
							cb.getSp().edit().remove(cb.getKey()).commit();
							cb.up();
						}
					} else if (value instanceof SPRadioGroup) {
						SPRadioGroup rg = (SPRadioGroup) value;
						rg.check(rg.getDefCheck());
					}
				} catch (Throwable ignore) {
				}
			}
		}

		@Override
		public BaseActivityViewHolder getViewHolder() {
			return viewHolder;
		}

	}

	private class OutputTabFragment extends TabFragment {

        private static final String TAG = "OutputTabFragment";

		public final OutputFragmentViewHolder viewHolder;

		public OutputTabFragment(Activity activity) {
			super(activity);
			viewHolder = new OutputFragmentViewHolder(activity, R.id.tab_main_output);
			bindOnclick(this, viewHolder.getContentView());
			viewHolder.et_out_dir.setText(getOutputDir());
		}

        private String getOutputDir() {
            return AppSettings.getJsonOutputDir().getAbsolutePath();
        }

        @Keep
		public void onCopyClick(View view) {
			Tools.copyToClipboard(viewHolder.et_result.getText());
		}

        @Keep
		public void onOutputWholeClick(View view) {
			if (checkGeneration())return;
			if (checkPermissionAndRequest())return;

			EditText edit = viewHolder.et_out_dir;

			final File outDir = new File(edit.getText().toString());
			if (outDir.isFile()) {
				edit.setError(getString(R.string.message_same_filename_exists));
				return;
			}

			showOutputDialog(getString(R.string.btn_out_whole), new File(outDir, String.format("%s.java" , mBasicFragment.viewHolder.et_cls.getText().toString())), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dia, int with) {
						final EditText edit = ((Dialog)dia).findViewById(R.id.edit);
						File outFile = new File(edit.getText().toString());
						if (outFile.isDirectory()) {
							edit.setError(getString(R.string.message_same_foldername_exists));
							return;
						}
						try {
							FileIOUtils.writeFileFromString(outFile, mGenerationResult.java);
                            ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
                            dia.dismiss();
						} catch (Throwable e) {
                            ToastUtils.showShort(e.getMessage());
						}							
					}});

		}

        @Keep
		public void onSplistOutputClick(View view) {
			if (checkGeneration())return;
			if (checkPermissionAndRequest())return;

			EditText edit = viewHolder.et_out_dir;
			try {
				File outDir = new File(edit.getText().toString()); 
				if (outDir.isFile()) {
					edit.setError(getString(R.string.message_same_filename_exists));
					return;
				}

				mGenerationResult.bean.output(outDir, OutputType.SPLIST);
                ToastUtils.showShort(getString(R.string.message_format_exported, outDir.getAbsolutePath()));
			} catch (Throwable e) {
                ToastUtils.showShort(e.getMessage());
			}
		}

        @Keep
		public void onOutputZipClick(View view) {
			if (checkGeneration())return;
			if (checkPermissionAndRequest())return;

			EditText edit = viewHolder.et_out_dir;

			final File outDir=new File(edit.getText().toString());
			if (outDir.isFile()) {
				edit.setError(getString(R.string.message_same_filename_exists));
				return;
			}

			showOutputDialog(getString(R.string.btn_out_zip), new File(outDir, String.format("%s.zip" , mBasicFragment.viewHolder.et_cls.getText().toString())), new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dia, int with) {
						final EditText edit = ((Dialog)dia).findViewById(R.id.edit);
						File outFile = new File(edit.getText().toString());
						if (outFile.isDirectory()) {
							edit.setError(getString(R.string.message_same_foldername_exists));
							return;
						}
						try {
							mGenerationResult.bean.output(outFile, OutputType.ZIP);
                            ToastUtils.showShort(getString(R.string.message_format_exported, outFile.getAbsolutePath()));
							dia.dismiss();
						} catch (Throwable e) {
                            ToastUtils.showShort(e.getMessage());
						}

					}
				});

		}

        @Keep
		public void onClearClick(View view) {
			viewHolder.et_result.setText(null);
		}

		private void showOutputDialog(CharSequence title, final File out, final DialogInterface.OnClickListener l) {
			final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle(title)
				.setView(R.layout.dialog_input_url)
				.setPositiveButton(R.string.output, null)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
			dialog.show();

			final EditText edit = dialog.findViewById(R.id.edit);
            edit.setText(out.getAbsolutePath());

			dialog.getButton(dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						l.onClick(dialog, dialog.BUTTON_POSITIVE);
					}
				});

			edit.setHint(R.string.hint_filename);
			edit.addTextChangedListener(new TextWatcher(){

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						dialog.getButton(dialog.BUTTON_POSITIVE).setEnabled(s.length() != 0);
					}

					@Override
					public void afterTextChanged(Editable s) {

					}
				});
			edit.post(new Runnable(){

					@Override
					public void run() {
						String name = out.getName();
						int start = out.getAbsolutePath().length() - name.length();
						int end = out.getAbsolutePath().lastIndexOf(".");
						if (end < 0) {
							end = out.getAbsolutePath().length();
						}
						edit.setSelection(start, end);
						KeyboardUtils.showSoftInput(edit);
					}
				});

		}

		@Override
		public void setVisibility(boolean visibility) {
			super.setVisibility(visibility);
			if (!visibility) {
			}
		}

		@Override
		public BaseActivityViewHolder getViewHolder() {
			return viewHolder;
		}

	}

    private boolean checkPermissionAndRequest() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_REQUEST_WRITE_STORAGE_PERMISSION);  
            return true;
        }
        return false;
    }

    private boolean checkGeneration() {
        if (mGenerationResult == null) {
            ToastUtils.showShort(R.string.message_not_generate);
            return true;
        }
        return false;
    }

    private void updateBuilder(Json2Bean.Builder builder) {
		ConfigActivityViewHolder holder = mConfigFragment.viewHolder;

        switch (holder.rg_field_modifier.getCheckedRadioButtonId()) {
            case R.id.rb_private:
                builder.setFieldModifiers(Modifier.PRIVATE);
                break;
            case R.id.rb_public:
                builder.setFieldModifiers(Modifier.PUBLIC);
                break;
            case R.id.rb_protected:
                builder.setFieldModifiers(Modifier.PROTECTED);
                break;
        }

        builder.setPackageName(mBasicFragment.viewHolder.et_pkg.getText().toString());
        builder.setClassName(mBasicFragment.viewHolder.et_cls.getText().toString());

        builder.setUseSetter(holder.use_setter.isChecked());
        builder.setUseGetter(holder.use_getter.isChecked());
        builder.setUseReturnThis(holder.use_return_this.isChecked());
        builder.setUsePrimitiveTypes(holder.use_primitive_types.isChecked());
        builder.setUseLongIntegers(holder.use_long_integers.isChecked());
        builder.setInitializeCollections(holder.initialize_collections.isChecked());
        builder.setAcceptNullValue(holder.accept_null_value.isChecked());
        builder.setMakeClassesParcelable(holder.make_classes_parcelable.isChecked());
        builder.setMakeClassesSerializable(holder.make_classes_serializable.isChecked());
        builder.setOverrideEquals(holder.override_equals.isChecked());
        builder.setOverrideHashCode(holder.override_hashCode.isChecked());
        builder.setOverrideToString(holder.override_toString.isChecked());

        if (holder.annotation_gson.isChecked()) {
            builder.addAnnotationStyle(AnnotationStyle.GSON);
        } else {
            builder.removeAnnotationStyle(AnnotationStyle.GSON);
        }

        if (holder.annotation_fastjson.isChecked()) {
            builder.addAnnotationStyle(AnnotationStyle.FASTJSON);  
        } else {
            builder.removeAnnotationStyle(AnnotationStyle.FASTJSON);  
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastUtils.showShort(R.string.message_not_write_storage_permission);
            }
        }
    }

    private long firstBack = 0L;
    @Override
    public void onBackPressed() {
		actionBar: {
			ActionBar actionBar = getActionBar();
			if (!actionBar.getTabAt(0).equals(actionBar.getSelectedTab())) {
				actionBar.getTabAt(0).select();
				return;
			}
		}

        if (System.currentTimeMillis() - firstBack > 2000) {
            ToastUtils.showShort(R.string.message_press_again_to_exit);
            firstBack = System.currentTimeMillis();
            return;
        }

        ToastUtils.cancel();
        super.onBackPressed();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_main, menu);

        MenuItem menuItemGenerate = menu.findItem(R.id.menuItemGenerate); {
            TypedArray a = obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
            int colorAccent = a.getColor(0, Color.RED);
            a.recycle();
            SpannableString ss = new SpannableString(menuItemGenerate.getTitle());
            ss.setSpan(new ForegroundColorSpan(colorAccent), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            menuItemGenerate.setTitle(ss);
        }
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemGenerate: {
                    generation();
                }
				break;
            case R.id.menuItemAbout: {
                    showAboutDialog();
                }
                break;
			case R.id.menuItemFinish: {
					finish();
                }
				break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setIcon(AppUtils.getAppIcon())
            .setTitle(AppUtils.getAppName())
            .setMessage(R.string.app_introduction)
            .setPositiveButton(android.R.string.ok, null)
            .create();
        dialog.show();
    }

    private class GenerationResult {
        public JavaBean bean;
        public String java;
    }

	private class ActionBarTab {

		private final ActionBar.Tab tab;

		private final TabFragment fragment;

		public ActionBarTab(ActionBar.Tab tab, TabFragment fragment) {
			this.tab = tab;
			this.fragment = fragment;
		}

		public ActionBar.Tab getTab() {
			return tab;
		}

		public TabFragment getFragment() {
			return fragment;
		}

	}

    private class BasicActivityViewHolder extends BaseActivityViewHolder {

        @ViewInject(R.id.et_pkg)
        public EditText et_pkg;

        @ViewInject(R.id.et_cls)
        public EditText et_cls;

        @ViewInject(R.id.et_json)
        public EditText et_json;

        @ViewInject(R.id.scrollview)
        public FastScrollScrollView fastScrollView;

        @ViewInject(R.id.btn_more)
        public ImageButton btn_more;

		public BasicActivityViewHolder(Activity activity, int viewId) {
            super(activity, viewId);
        }

    }

    private class ConfigActivityViewHolder extends BaseActivityViewHolder {

        @ViewInject(R.id.rg_field_modifier)
		private RadioGroup rg_field_modifier;

        @ViewInject(R.id.use_setter)
        public CheckBox use_setter;

        @ViewInject(R.id.use_getter)
        public CheckBox use_getter;

        @ViewInject(R.id.use_return_this)
        public CheckBox use_return_this;

        @ViewInject(R.id.use_primitive_types)
        public CheckBox use_primitive_types;

        @ViewInject(R.id.use_long_integers)
        public CheckBox use_long_integers;

        @ViewInject(R.id.initialize_collections)
        public CheckBox initialize_collections;

        @ViewInject(R.id.accept_null_value)
        public CheckBox accept_null_value;

        @ViewInject(R.id.make_classes_parcelable)
        public CheckBox make_classes_parcelable;

        @ViewInject(R.id.make_classes_serializable)
        public CheckBox make_classes_serializable;

        @ViewInject(R.id.override_equals)
        public CheckBox override_equals;

        @ViewInject(R.id.override_hashCode)
        public CheckBox override_hashCode;

        @ViewInject(R.id.override_toString)
        public CheckBox override_toString;

        @ViewInject(R.id.annotation_gson)
        public CheckBox annotation_gson;

        @ViewInject(R.id.annotation_fastjson)
        public CheckBox annotation_fastjson;

        @ViewInject(R.id.scrollview)
        public FastScrollScrollView fastScrollView;

        public ConfigActivityViewHolder(Activity activity, int viewId) {
            super(activity, viewId);
        }

    }

    private class OutputFragmentViewHolder extends BaseActivityViewHolder {

        @ViewInject(R.id.scrollview)
        public FastScrollScrollView fastScrollView;

        @ViewInject(R.id.et_out_dir)
        public EditText et_out_dir;

        @ViewInject(R.id.et_result)
        public EditText et_result;

        public OutputFragmentViewHolder(Activity activity, int viewId) {
            super(activity, viewId);
        }

    }

} 
