package kr.co.apptronics.acube.example;


import kr.co.apptronics.atp.ATConstants;
import kr.co.apptronics.atp.bt.BluetoothService;
import kr.co.apptronics.atp.bt.DeviceListActivity;
import kr.co.apptronics.acube.example.R;
import kr.co.apptronics.atp.util.ATLog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


/*
 * 블루투스 연결부분을 구현하는 샘플
 */
public final class SearchDeviceMainActivity extends Activity {
	private final String TAG = "SearchDeviceMainActivity";
	private final boolean debug = true;
	
	private final int REQUEST_ENABLE_BLUETOOTH = 0;
	private static final int REQUEST_CONNECT_DEVICE = 1;

	
	//블루투스 인스턴스
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothService mBluetoothService;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect);
		
		//이후의 로그메세지를 표시한다.
		ATLog.setPrintable(true);
		ATLog.e(TAG, "로그 표시 테스트 라인입니다.");
	}
	
	@Override
	protected void onResume() {
		//화면 표시
		String txt = "블루투스를 연결합니다. \n 화면을 눌러주세요!";
		Button btnConnect = (Button)findViewById(R.id.btn_connect);
		btnConnect.setText(txt);
		btnConnect.setOnClickListener(mConnectListener);
		super.onResume();
	}


	private View.OnClickListener mConnectListener = new View.OnClickListener() {

		public void onClick(View v) {
			if(!isSupportBluetooth()) {
				//블루투스 기능을 지원하지 않으므로 프로그램을 종료해야 하지만, 여기서는 아무일도 하지않음.
				return;
			}
			confirmBluetoothEnable();
			connect();
		}
	};
	
	private boolean isSupportBluetooth() {
		boolean result = true;
		//폰에 있는 블루투스 어뎁터(Bluetooth adapter)를 얻어온다.
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_LONG).show();
			result = false;
		}
		return result;
	}
	
	/*
	 * Bluetooth기능이 활성화되었는지 확인하고, 활성화 되어있지 않으면 사용자에게 Bluetooth활성화 요구를 한다.
	 */
	private void confirmBluetoothEnable() {
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			//아래의 onActivityResult() 메서드를 확인
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
		}
	}
	
	private void connectBluetooth() {
		// 블루투스 디바이스 선택 화면을 표시한다.
		Intent serverIntent = new Intent(SearchDeviceMainActivity.this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}
	
	/*
	 * 블루투스 연결후 실행되는 메서드
	 * 이 메서드에 자신의 샘플 Activity로 이동하게 기술한다.
	 */
	public void transitActivity() {
		
//		Intent intent = new Intent(this, SampleListViewActivity.class);
//		startActivity(intent);
//		finish();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BLUETOOTH:
			{
				// 블루투스 활성화 요구의 결과 
				if (resultCode == Activity.RESULT_OK) {
					// 블루투스가 활성화 됨.
					connect();
					break;
				} else {
					// 사용자가 블루투스를 활성화 시키지 않음.
					ATLog.e(TAG, "블루투스 활성화에 실패하였습니다.");
					Toast.makeText(this, "블루투스 활성화에 실패하였습니다.", Toast.LENGTH_SHORT).show();
					break;
				} //if
			}
			case REQUEST_CONNECT_DEVICE:
			{
				// DeviceListActivity 화면에서 사용자가 선택한 디바이스의 MAC 주소가 리턴되어 온다.
				if (resultCode == Activity.RESULT_OK) {
					// 선택된 MAC주소를 얻는다.
					final String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					ATLog.d(TAG, "선택된 MAC (" + address + ")");
	
					//프로그래스 다이얼로그 표시 
					final ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.state_connecting), true);
					dialog.setOwnerActivity(this);
					
					//선택된 디바이스와 블루투스 연결을 시도한다. 화면을 블록킹하지 않기  위해 스레드를 사용하여 실행한다.
					new Thread(new Runnable() {
						public void run() {
							// 선택된 디바이스 (BLuetoothDevice)의 객체를 얻어온다.
							BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
							// 블루투스 연결을 시도한다.
							if (mBluetoothService.connect(device)) {
								dialog.dismiss();
								transitActivity();
							}
							dialog.dismiss();
						}
					}).start();
				} // if
				break;
			}
			default:
				break;
		}// switch
	} // onActivityResult
	
	private void connect() {
		if (mBluetoothAdapter.isEnabled()) {
			setupBluetoothService();
			connectBluetooth();
		}
	}
	
	private void setupBluetoothService() {
		ATLog.e(TAG, "[setupBluetoothService] ");
		mBluetoothService = new BluetoothService(mHandler);
	}
	
	
	// BluetoothService에서 오는 메세지를 처리한다.
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BluetoothService.MESSAGE_STATE_CHANGE:
					ATLog.e(TAG, "[mHandler] " + msg.arg1);
					String message;
					switch (msg.arg1) {
						case BluetoothService.STATE_CONNECTED:
							message = getString(R.string.state_connected);
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_CONNECTING:
							message = getString(R.string.state_connecting);
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_LISTEN:
							break;
						case BluetoothService.STATE_NONE:
							message = getString(R.string.state_not_connected);
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
							break;
						case BluetoothService.STATE_ERROR:
							message = getString(R.string.state_error);
							Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
							break;
					}
					break;
				case BluetoothService.MESSAGE_DEVICE_NAME:
					// 연결된 디바이스의 이름을 저장한다.
					String name = msg.getData().getString(BluetoothService.DEVICE_NAME);
					Toast.makeText(getApplicationContext(), "[" + name + "]에 연결되었습니다.", Toast.LENGTH_SHORT)
							.show();
					break;
				case BluetoothService.MESSAGE_TOAST:
					Toast.makeText(getApplicationContext(), msg.getData().getString("toast"), Toast.LENGTH_LONG).show();
					break;
				} //switch
		} //handleMessage
	};

	@Override
	public void onBackPressed() {

		new AlertDialog.Builder(this)
		.setTitle("Apptronics Sample")
		.setMessage("프로그램을 종료하시겠습니까?")
		.setIcon(R.drawable.icon)
		.setPositiveButton("종료", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				SearchDeviceMainActivity.super.onBackPressed();
			}
		})
		.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		})
		.show();
	}
	
} // end of class

