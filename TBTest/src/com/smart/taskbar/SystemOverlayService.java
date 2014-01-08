package com.smart.taskbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.smart.taskbar.RadialMenuWidget.RadialMenuEntry;

public class SystemOverlayService extends Service implements OnTouchListener, OnClickListener {
	private View topLeftView;

	private Button overlayedButton;
	private float offsetX;
	private float offsetY;
	private int originalXPos;
	private int originalYPos;
	private boolean moving;
	private WindowManager wm;

	private RadialMenuWidget PieMenu;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		PieMenu = new RadialMenuWidget(getBaseContext());

		int xLayoutSize = wm.getDefaultDisplay().getWidth();
		int yLayoutSize = wm.getDefaultDisplay().getHeight();

		PieMenu.setAnimationSpeed(0L);
		PieMenu.setSourceLocation(xLayoutSize, yLayoutSize);
		PieMenu.setIconSize(15, 30);
		PieMenu.setTextSize(13);

		PieMenu.setCenterCircle(new Close());
		PieMenu.addMenuEntry(new Menu1());
		PieMenu.addMenuEntry(new NewTestMenu());
		PieMenu.addMenuEntry(new CircleOptions());
		PieMenu.addMenuEntry(new Menu2());
		PieMenu.addMenuEntry(new Menu3());

		overlayedButton = new Button(this);
		overlayedButton.setText("Overlay button");
		overlayedButton.setOnTouchListener(this);
		overlayedButton.setBackgroundColor(0x55fe4444);
		overlayedButton.setOnClickListener(this);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.LEFT | Gravity.TOP;
		params.x = 0;
		params.y = 0;
		wm.addView(overlayedButton, params);

		topLeftView = new View(this);
		WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
		topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
		topLeftParams.x = 0;
		topLeftParams.y = 0;
		topLeftParams.width = 0;
		topLeftParams.height = 0;
		wm.addView(topLeftView, topLeftParams);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (overlayedButton != null) {
			wm.removeView(overlayedButton);
			wm.removeView(topLeftView);
			overlayedButton = null;
			topLeftView = null;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			float x = event.getRawX();
			float y = event.getRawY();

			moving = false;

			int[] location = new int[2];
			overlayedButton.getLocationOnScreen(location);

			originalXPos = location[0];
			originalYPos = location[1];

			offsetX = originalXPos - x;
			offsetY = originalYPos - y;

		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			int[] topLeftLocationOnScreen = new int[2];
			topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

			System.out.println("topLeftY=" + topLeftLocationOnScreen[1]);
			System.out.println("originalY=" + originalYPos);
			float x = event.getRawX();
			float y = event.getRawY();

			WindowManager.LayoutParams params = (LayoutParams) overlayedButton.getLayoutParams();

			int newX = (int) (offsetX + x);
			int newY = (int) (offsetY + y);

			if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
				return false;
			}

			params.x = newX - (topLeftLocationOnScreen[0]);
			params.y = newY - (topLeftLocationOnScreen[1]);

			wm.updateViewLayout(overlayedButton, params);
			moving = true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (moving) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(this, "Overlay button click event", Toast.LENGTH_SHORT).show();

		wm.removeView(overlayedButton);
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER;

		wm.addView(PieMenu, params);

	}

	public static class Menu1 implements RadialMenuEntry {
		public String getName() {
			return "Menu1 - No Children";
		}

		public String getLabel() {
			return "Menu1\nTest";
		}

		public int getIcon() {
			return 0;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("Menu #1 Activated - No Children");
		}
	}

	public static class Menu2 implements RadialMenuEntry {
		public String getName() {
			return "Menu2 - Children";
		}

		public String getLabel() {
			return "Menu2";
		}

		public int getIcon() {
			return R.drawable.icon;
		}

		private List<RadialMenuEntry> children = new ArrayList<RadialMenuEntry>(Arrays.asList(new StringOnly(), new IconOnly(), new StringAndIcon()));

		public List<RadialMenuEntry> getChildren() {
			return children;
		}

		public void menuActiviated() {
			System.out.println("Menu #2 Activated - Children");
		}
	}

	public static class Menu3 implements RadialMenuEntry {
		public String getName() {
			return "Menu3 - No Children";
		}

		public String getLabel() {
			return null;
		}

		public int getIcon() {
			return R.drawable.icon;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("Menu #3 Activated - No Children");
		}
	}

	public static class IconOnly implements RadialMenuEntry {
		public String getName() {
			return "IconOnly";
		}

		public String getLabel() {
			return null;
		}

		public int getIcon() {
			return R.drawable.icon;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("IconOnly Menu Activated");
		}
	}

	public static class StringAndIcon implements RadialMenuEntry {
		public String getName() {
			return "StringAndIcon";
		}

		public String getLabel() {
			return "String";
		}

		public int getIcon() {
			return R.drawable.icon;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("StringAndIcon Menu Activated");
		}
	}

	public static class StringOnly implements RadialMenuEntry {
		public String getName() {
			return "StringOnly";
		}

		public String getLabel() {
			return "String\nOnly";
		}

		public int getIcon() {
			return 0;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("StringOnly Menu Activated");
		}
	}

	public static class NewTestMenu implements RadialMenuEntry {
		public String getName() {
			return "NewTestMenu";
		}

		public String getLabel() {
			return "New\nTest\nMenu";
		}

		public int getIcon() {
			return 0;
		}

		private List<RadialMenuEntry> children = new ArrayList<RadialMenuEntry>(Arrays.asList(new StringOnly(), new IconOnly()));

		public List<RadialMenuEntry> getChildren() {
			return children;
		}

		public void menuActiviated() {
			System.out.println("New Test Menu Activated");
		}
	}

	public static class CircleOptions implements RadialMenuEntry {
		public String getName() {
			return "CircleOptions";
		}

		public String getLabel() {
			return "Circle\nSymbols";
		}

		public int getIcon() {
			return 0;
		}

		private List<RadialMenuEntry> children = new ArrayList<RadialMenuEntry>(Arrays.asList(new RedCircle(), new YellowCircle(), new GreenCircle(),
				new BlueCircle()));

		public List<RadialMenuEntry> getChildren() {
			return children;
		}

		public void menuActiviated() {
			System.out.println("Circle Options Activated");
		}
	}

	public static class RedCircle implements RadialMenuEntry {
		public String getName() {
			return "RedCircle";
		}

		public String getLabel() {
			return "Red";
		}

		public int getIcon() {
			return R.drawable.red_circle;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("Red Circle Activated");
		}
	}

	public static class YellowCircle implements RadialMenuEntry {
		public String getName() {
			return "YellowCircle";
		}

		public String getLabel() {
			return "Yellow";
		}

		public int getIcon() {
			return R.drawable.yellow_circle;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("Yellow Circle Activated");
		}
	}

	public static class GreenCircle implements RadialMenuEntry {
		public String getName() {
			return "GreenCircle";
		}

		public String getLabel() {
			return "Green";
		}

		public int getIcon() {
			return R.drawable.green_circle;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("Green Circle Activated");
		}
	}

	public static class BlueCircle implements RadialMenuEntry {
		public String getName() {
			return "BlueCircle";
		}

		public String getLabel() {
			return "Blue";
		}

		public int getIcon() {
			return R.drawable.blue_circle;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {
			System.out.println("Blue Circle Activated");
		}
	}

	public class Close implements RadialMenuEntry {

		public String getName() {
			return "Close";
		}

		public String getLabel() {
			return null;
		}

		public int getIcon() {
			return android.R.drawable.ic_menu_close_clear_cancel;
		}

		public List<RadialMenuEntry> getChildren() {
			return null;
		}

		public void menuActiviated() {

			System.out.println("Close Menu Activated");
			//Need to figure out how to to the layout.removeView(PieMenu)
			//ll.removeView(PieMenu);
//			((WindowManager) PieMenu.getParent()).removeView(PieMenu);
			wm.removeView(PieMenu);
			WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
							| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
			params.gravity = Gravity.LEFT | Gravity.TOP;
			params.x = 0;
			params.y = 0;
			wm.addView(overlayedButton, params);
		}
	}
}
