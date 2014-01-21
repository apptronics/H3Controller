package kr.co.apptronics.acube.example;

import kr.co.apptronics.atp.atb.Motor;
import kr.co.apptronics.atp.atb.MotorPort;
import kr.co.apptronics.atp.bt.BluetoothService;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.math.MathUtils;

import android.bluetooth.BluetoothAdapter;
import android.opengl.GLES20;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga
 *
 * @author Nicolas Gramlich
 * @since 00:06:23 - 11.07.2010
 */
public class H3ControlActivity extends SimpleBaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

//	private static final int CAMERA_WIDTH = 480;
//	private static final int CAMERA_HEIGHT = 320;
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private Camera mCamera;

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mFaceTextureRegion;

	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenLeftControlBaseTextureRegion;
	private ITextureRegion mOnScreenRightControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;
	
	private int TX_flag1=0,TX_flag2=0,TX_flag3=0,TX_flag4=0,TX_flag5=0,TX_flag6=0;
	
	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	
	@Override
	public EngineOptions onCreateEngineOptions() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		if(MultiTouch.isSupported(this)) {
			if(MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_SHORT).show();
			} else {
				this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
				Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
		}

		return engineOptions;
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 50, 50, TextureOptions.BILINEAR);
		this.mFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_box.png", 0, 0);
		this.mBitmapTextureAtlas.load();

		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 500, 250, TextureOptions.BILINEAR);
		this.mOnScreenLeftControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base2.png", 0, 0);
		this.mOnScreenRightControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 250, 0);
		this.mOnScreenControlTexture.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene();
		scene.setBackground(new Background(0.0f, 0.0f, 0.0f));

//		final float centerX = (CAMERA_WIDTH - this.mFaceTextureRegion.getWidth()) / 2;
//		final float centerY = (CAMERA_HEIGHT - this.mFaceTextureRegion.getHeight()) / 2;
//		final Sprite face = new Sprite(centerX, centerY, this.mFaceTextureRegion, this.getVertexBufferObjectManager());
//		final PhysicsHandler physicsHandler = new PhysicsHandler(face);
//		face.registerUpdateHandler(physicsHandler);
//		scene.attachChild(face);

		/* Velocity control (left). */
		final float x1 = 100;
		final float y1 = (CAMERA_HEIGHT - this.mOnScreenLeftControlBaseTextureRegion.getHeight())/2;
		final AnalogOnScreenControl velocityOnScreenControl = new AnalogOnScreenControl(x1, y1, this.mCamera, this.mOnScreenLeftControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
//				physicsHandler.setVelocity(pValueX * 100, pValueY * 100);
				Log.e("velocityOnScreenControl", "x->" + pValueX + "  y->"+pValueY);
				if(pValueY == 0 && TX_flag1==0) {
					mLMotor.stop();
					mLMotor.stop();
					TX_flag1=1;
					TX_flag2=0;
					TX_flag3=0;
				} else if(pValueY < 0 && TX_flag2==0) {
					
					mLMotor.setPower(power).forward();
					mLMotor.setPower(power).forward();
					TX_flag2=1;
					TX_flag1=0;
					TX_flag3=0;
				} else if(pValueY > 0 && TX_flag3==0) {
					mLMotor.setPower(power).backward();
					mLMotor.setPower(power).backward();
					TX_flag3=1;
					TX_flag1=0;
					TX_flag2=0;
					
				}
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
			}
		});
//		velocityOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//		velocityOnScreenControl.getControlBase().setAlpha(0.5f);

		scene.setChildScene(velocityOnScreenControl);


		/* Rotation control (right). */
		final float y2 = (this.mPlaceOnScreenControlsAtDifferentVerticalLocations) ? 0 : y1;
		final float x2 = CAMERA_WIDTH - this.mOnScreenRightControlBaseTextureRegion.getWidth() - 100;
		final AnalogOnScreenControl rotationOnScreenControl = new AnalogOnScreenControl(x2, y2, this.mCamera, this.mOnScreenRightControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				if(pValueX == 0 && TX_flag4==0) {
					mRMotor.stop();
					mRMotor.stop();
					TX_flag4=1;
					TX_flag5=0;
					TX_flag6=0;
				} else if(pValueX < 0 && TX_flag5==0) {
					mRMotor.setPower(power).forward(); //왼쪽
					mRMotor.setPower(power).forward(); //왼쪽
					TX_flag5=1;
					TX_flag4=0;
					TX_flag6=0;
					
				} else if(pValueX > 0 && TX_flag6==0) {
					mRMotor.setPower(power).backward(); //오른쪽
					mRMotor.setPower(power).backward(); //오른쪽
					TX_flag6=1;
					TX_flag4=0;
					TX_flag5=0;
					
				}
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
				//Log.e("velocityOnScreenControl", "onControlClick!");
			}
		});
//		rotationOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
//		rotationOnScreenControl.getControlBase().setAlpha(0.5f);

		velocityOnScreenControl.setChildScene(rotationOnScreenControl);

		return scene;
	}

	
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ===========================================================
	// Controller 
	// ===========================================================
	private int power = 250;
	final Motor mLMotor = new Motor(MotorPort.A); 	//전후진 
	final Motor mRMotor = new Motor(MotorPort.B);	//방향버튼 forward-왼쪽   backword-오른쪽
	Handler handler = new Handler();
	
}