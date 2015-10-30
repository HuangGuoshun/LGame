/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package loon.android;

import loon.Asyn;
import loon.Json;
import loon.LGame;
import loon.LSetting;
import loon.LSystem;
import loon.LSystemView;
import loon.Support;
import loon.utils.json.JsonImpl;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class AndroidGame extends LGame {

	final private static String BULID_BRAND, BULID_MODEL, BULIDM_PRODUCT,
			BULIDM_RELEASE, BULIDM_DEVICE;

	private static int BULIDM_SDK;

	public static boolean USE_BITMAP_MEMORY_HACK;

	private static boolean EMULATOR;

	public static final boolean DEBUG_LOGS = false;

	public final static int ICE_CREAM_SANDWICH = 14;

	private final static Support support = new AndroidSupport();

	static {
		BULID_BRAND = Build.BRAND.toLowerCase();
		BULID_MODEL = Build.MODEL.toLowerCase();
		BULIDM_PRODUCT = Build.PRODUCT.toLowerCase();
		BULIDM_RELEASE = Build.VERSION.RELEASE;
		try {
			BULIDM_SDK = Integer.parseInt(String.valueOf(Build.VERSION.class
					.getDeclaredField("SDK").get(null)));
		} catch (Exception ex) {
			try {
				BULIDM_SDK = Build.VERSION.class.getDeclaredField("SDK_INT")
						.getInt(null);
			} catch (Exception e) {
				BULIDM_SDK = 3;
			}
		}
		BULIDM_DEVICE = Build.DEVICE;
		EMULATOR = BULID_BRAND.indexOf("generic") != -1
				&& BULID_MODEL.indexOf("sdk") != -1;
		USE_BITMAP_MEMORY_HACK = BULIDM_SDK < ICE_CREAM_SANDWICH;
	}
	LSystemView game;
	Loon activity;

	private enum State {
		RUNNING, PAUSED, EXITED
	};

	private State state = State.RUNNING;

	protected final AndroidAssets assets;
	protected final AndroidAsyn syn;
	protected final AndroidGraphics graphics;
	protected final AndroidInputMake input;
	protected final AndroidLog log;
	protected final AndroidSave save;
	protected final Json json;
	protected final long start = System.nanoTime();

	public AndroidGame(Loon game, LSetting config) {
		super(config, game);
		this.activity = game;
		this.log = new AndroidLog(config.appName);
		this.syn = new AndroidAsyn(log, frame, activity) {
			@Override
			protected boolean isPaused() {
				return state == State.PAUSED;
			}
		};
		this.graphics = new AndroidGraphics(this,
				activity.preferredBitmapConfig());
		this.assets = new AndroidAssets(this);
		this.json = new JsonImpl();
		this.input = new AndroidInputMake(this);
		this.save = new AndroidSave(this);
		this.initProcess();
	}

	static void debugLog(String message) {
		if (DEBUG_LOGS) {
			Log.d(LSystem.APP_NAME, message);
		}
	}

	@Override
	public Type type() {
		return Type.ANDROID;
	}

	@Override
	public double time() {
		return System.currentTimeMillis();
	}

	@Override
	public int tick() {
		return (int) ((System.nanoTime() - start) / 1000000L);
	}

	@Override
	public void openURL(String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		activity.startActivity(browserIntent);
	}

	@Override
	public AndroidAssets assets() {
		return assets;
	}

	@Override
	public AndroidGraphics graphics() {
		return graphics;
	}

	@Override
	public AndroidInputMake input() {
		return input;
	}

	@Override
	public AndroidLog log() {
		return log;
	}

	@Override
	public AndroidSave save() {
		return save;
	}

	@Override
	public Asyn asyn() {
		return syn;
	}

	@Override
	public Json json() {
		return json;
	}

	void onPause() {
		state = State.PAUSED;
		status.emit(Status.PAUSE);
	}

	void onResume() {
		state = State.RUNNING;
		status.emit(Status.RESUME);
	}

	void onExit() {
		state = State.EXITED;
		status.emit(Status.EXIT);
	}

	void processFrame() {
		emitFrame();
	}

	@Override
	public Support support() {
		return support;
	}

	/**
	 * 判断手机驱动
	 * 
	 * @param d
	 * @return
	 */
	public static boolean isDevice(String d) {
		return BULIDM_DEVICE.equalsIgnoreCase(d);
	}

	/**
	 * 设定LayoutParams为全屏模式
	 * 
	 * @return
	 */
	public static LayoutParams createFillLayoutParams() {
		return new LayoutParams(0xffffffff, 0xffffffff);
	}

	/**
	 * 生成一个对应指定位置的RelativeLayout
	 * 
	 * @param location
	 * @return
	 */
	public static RelativeLayout.LayoutParams createRelativeLayout(
			AndroidLocation location) {
		return createRelativeLayout(location, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	/**
	 * 生成一个对应指定位置的RelativeLayout
	 * 
	 * @param location
	 * @return
	 */
	public static RelativeLayout.LayoutParams createRelativeLayout(
			AndroidLocation location, int w, int h) {
		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				w, h);
		if (location == AndroidLocation.LEFT) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.RIGHT) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.TOP) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.BOTTOM) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.BOTTOM_LEFT) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.BOTTOM_RIGHT) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.CENTER) {
			relativeParams.addRule(RelativeLayout.CENTER_VERTICAL,
					RelativeLayout.TRUE);
			relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_BASELINE) {
			relativeParams.addRule(RelativeLayout.ALIGN_BASELINE,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_LEFT) {
			relativeParams.addRule(RelativeLayout.ALIGN_LEFT,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_TOP) {
			relativeParams.addRule(RelativeLayout.ALIGN_TOP,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_RIGHT) {
			relativeParams.addRule(RelativeLayout.ALIGN_RIGHT,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_BOTTOM) {
			relativeParams.addRule(RelativeLayout.ALIGN_BOTTOM,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_PARENT_LEFT) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_PARENT_TOP) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_PARENT_RIGHT) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.ALIGN_PARENT_BOTTOM) {
			relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.CENTER_IN_PARENT) {
			relativeParams.addRule(RelativeLayout.CENTER_IN_PARENT,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.CENTER_HORIZONTAL) {
			relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
		} else if (location == AndroidLocation.CENTER_VERTICAL) {
			relativeParams.addRule(RelativeLayout.CENTER_VERTICAL,
					RelativeLayout.TRUE);
		}

		return relativeParams;
	}

	/**
	 * 判定当前Android系统版本是否高于指定的版本
	 * 
	 * @param ver
	 * @return
	 */
	public static boolean isAndroidVersionHigher(final int ver) {
		return BULIDM_SDK >= ver;
	}

	public static String getModel() {
		return BULID_MODEL;
	}

	public static String getProductName() {
		return BULIDM_PRODUCT;
	}

	public static String getOSVersion() {
		return BULIDM_RELEASE;
	}

	public static int getSDKVersion() {
		return BULIDM_SDK;
	}

	public static String getBRANDName() {
		return BULID_BRAND;
	}

	public static boolean isEmulator() {
		return EMULATOR;
	}

	public static boolean isHTC() {
		return BULID_BRAND.indexOf("htc") != -1;
	}

	public static boolean isSamsungGalaxy() {
		final boolean isSamsung = BULID_BRAND.indexOf("samsung") != -1;
		final boolean isGalaxy = BULID_MODEL.indexOf("galaxy") != -1;
		return isSamsung && isGalaxy;
	}

	public static boolean isDroidOrMilestone() {
		final boolean isMotorola = BULID_BRAND.indexOf("moto") != -1;
		final boolean isDroid = BULID_MODEL.indexOf("droid") != -1;
		final boolean isMilestone = BULID_MODEL.indexOf("milestone") != -1;
		return isMotorola && (isDroid || isMilestone);
	}

}
