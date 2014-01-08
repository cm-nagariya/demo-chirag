package com.smart.taskbar;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class TaskBarView extends Activity implements OnCheckedChangeListener {
	ToggleButton tbService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		bindComponent();
		init();
		addListener();
	}

	private void bindComponent() {
		tbService = (ToggleButton) findViewById(R.id.tbService);
	}

	private void init() {

	}

	private void addListener() {
		tbService.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isMyServiceRunning()) {
			tbService.setChecked(true);
		} else {
			tbService.setChecked(false);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			Intent svc = new Intent(this, SystemOverlayService.class);
			startService(svc);

		} else {
			Intent svc = new Intent(this, SystemOverlayService.class);
			stopService(svc);

		}
	}

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (SystemOverlayService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
