package tcnr06.com;



import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import tcnr06.com.R;
import tcnr06.com.providers.FriendsContentProvider;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class M1801 extends FragmentActivity implements OnItemSelectedListener,
                                                       LocationListener,
                                                       InfoWindowAdapter,
                                                       OnCameraChangeListener,
                                                       OnMapReadyCallback,
                                                       OnMyLocationButtonClickListener,
                                                       OnMarkerClickListener{

	static LatLng VGPS ;
	private GoogleMap map;
	private static String[] locations,groupData;
	private static String[] mapType;
	private Spinner mSpnLocation, mSpnMaptype;
	private TextView stateText,infoContent,infoTitle,texZoom;
	private Double LatGps = 0.0;
	private Double LngGps = 0.0;
	private BitmapDescriptor image_des=null; // 圖標顯示
	private int icosel = 1,loginPage=1;

	private LocationManager locationMgr;

	private String provider;
	private long minTime = 5000;// ms
	private float minDist = 5.0f;// meter

	private float zoomsize = 17; // // 設定放大倍率1(地球)-21(街景)
	private float currentZoom = 17;
	private Marker markerMe,vgps;
	/** 記錄軌跡 */
	private ArrayList<LatLng> mytrace; //追蹤我的位置
	
	private EditText userName,userGroup;
	private CheckBox checkControl;
	private ScrollView scrollCheckBox;
	private UiSettings mUiSettings;

	private static ContentResolver mContRes;
	private String[] MYCOLUMN = new String[] { "id", "name", "grp", "address" };
	private LinearLayout loginLayout;
	
	String TAG = "tcnr6==>";
	private TextView nowUser;
	
	 SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	 private Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.m1701);
		setupviewcomponent();
//		initMap();
//		showloc();
//		locationMgr=(LocationManager)getSystemService(LOCATION_SERVICE);
//		locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
//		Criteria criteria=new Criteria();
//		provider=locationMgr.getBestProvider(criteria, true);
		db_mySQL();

	}
//--------------------匯入mySQL資料------------------
	private void db_mySQL() {//
		// -------------------------
		mContRes = getContentResolver();
		// -----------------------------		

		try {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("query_string", "import"));

			String result = DBConnector.executeQuery("import", params);
			/**************************************************************************
			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
			 * jsonData = new JSONObject(result);
			 **************************************************************************/
			String r = result.toString().trim();
			// 以下程式碼一定要放在前端藍色程式碼執行之後，才能取得狀態碼
			// 存取類別成員 DBConnector.httpstate 判定是否回應 200(連線要求成功)
			// Log.d(TAG, "httpstate="+DBConnector.httpstate );
			if (DBConnector.httpstate == 200) {
				JSONArray jsonArray = new JSONArray(result);
				// ---
				// Log.d(TAG, jsonArray.toString());
				if (jsonArray.length() > 0) {
					SQLiteWriter.sqlDelete(mContRes, FriendsContentProvider.CONTENT_URI, MYCOLUMN);//清空SQLite資料
				}

				ContentValues newRow = new ContentValues();

				for (int i = 0; i < jsonArray.length(); i++) {

					JSONObject jsonData = jsonArray.getJSONObject(i);
					// Log.d(TAG, "mySpinNum->"+i);
					// 取出 jsonObject 中的字段的值的空格Iterator功能進四cursor
					Iterator itt = jsonData.keys();// ID用mySQL的ID
					while (itt.hasNext()) {

						String key = itt.next().toString();
						String value = jsonData.getString(key);
						if (value == null) {
							continue;
						} else if ("".equals(value.trim())) {
							continue;
						} else {
							newRow.put(key, value.trim());
						}

					}
					SQLiteWriter.wirteToSQLite(mContRes, FriendsContentProvider.CONTENT_URI, MYCOLUMN, newRow);//寫入資料
				}
				Toast.makeText(getBaseContext(), "已經完成由伺服器會入資料", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getBaseContext(), "伺服器無回應，請稍後在試", Toast.LENGTH_LONG).show();
			}

			

		} catch (Exception e) {
			Log.d(TAG, "error->" + e.toString());
		}
		
		SQLiteData();
	}
//---------------------------------------------------------------------
	
	//----------------------取出SQLite裡面的DATA----------------------------------
	private void SQLiteData() {
		locations=null;groupData=null;
		mContRes=getContentResolver();
		String[][] getSQLiteDate=SQLiteWriter.getSQLiteData(mContRes, FriendsContentProvider.CONTENT_URI, MYCOLUMN);//透過自訂義method取出所有data
		locations=new String[getSQLiteDate.length];//開啟政列位置
		groupData=new String[getSQLiteDate.length];
		Log.d(TAG, "location.length"+locations.length);
		for (int i = 0; i < getSQLiteDate.length; i++) //逐筆取出data
		{
		   locations[i]=getSQLiteDate[i][1]+","+getSQLiteDate[i][3];//將取出的地點名稱根LatLng用 地點,lat,lng 的格式紀錄進陣列
		   groupData[i]=getSQLiteDate[i][2];//紀錄相對應位置的group
		}
		Log.d(TAG, "groupdata.length"+groupData.length);
		//---------設定spinner-----
		mSpnLocation = (Spinner) this.findViewById(R.id.spnLocation);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

//		locations = getResources().getStringArray(R.array.location);
		for (int i = 0; i < locations.length; i++) {
			String[] splits = locations[i].split(",");
			adapter.add(splits[0]);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpnLocation.setAdapter(adapter);
		mSpnLocation.setOnItemSelectedListener(this);
		//----------------------------
	}
//-------------------------------------------------------------
	//-----------------開啟map---------------------------
	private void initMap() {
		if (map == null) 
		{
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	  	    map.setInfoWindowAdapter(this);
	  	    map.setOnCameraChangeListener(this);
	  	    map.setOnMarkerClickListener(this);
			if (map != null) 
			{
				// 設定地圖類型--------------------
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
				// --------------------------
				map.setMyLocationEnabled(true); // 顯示自己位置
				mUiSettings = map.getUiSettings();
				// Keep the UI Settings state in sync with the checkboxes.
				// 顯示縮放按鈕
				mUiSettings.setZoomControlsEnabled(isChecked(R.id.zoom_buttons_toggle));
				// 顯示指北針
				mUiSettings.setCompassEnabled(isChecked(R.id.compass_toggle));
				// 顯示我的位置按鈕
				mUiSettings.setMyLocationButtonEnabled(isChecked(R.id.mylocationbutton_toggle));
				// 顯示我的位置圖示
				map.setMyLocationEnabled(isChecked(R.id.mylocationlayer_toggle));
				mUiSettings.setScrollGesturesEnabled(isChecked(R.id.scroll_toggle));
				mUiSettings.setZoomGesturesEnabled(isChecked(R.id.zoom_gestures_toggle));
				mUiSettings.setTiltGesturesEnabled(isChecked(R.id.tilt_toggle));
				mUiSettings.setRotateGesturesEnabled(isChecked(R.id.rotate_toggle));
				// ----設定地圖初始值大小
				// 移動地圖鏡頭到指定座標點,並設定地圖縮放等級
//				map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));
				// --------------------------------
				
				
			}
		}
	}
	//----------------------------------------------
	//------------------------------------------------
	 /** Control 控制項設定 */
	 private boolean isChecked(int id) {
	  return ((CheckBox) findViewById(id)).isChecked();
	 }
	 // ---檢查 Google Map 是否正確開啟
	 private boolean checkReady() {
	  if (map == null) {
	   Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
	   return false;
	  }
	  return true;
	 }
	//-------------------------------------------------
	 public void setZoomButtonsEnabled(View v) {
		  if (!checkReady()) {
		   return;
		  }
		  // Enables/disables zoom controls (+/- buttons in the bottom right of
		  // the map).
		  mUiSettings.setZoomControlsEnabled(((CheckBox) v).isChecked());
		 }
	// ---------------設定指北針----------------------------------------------
	 public void setCompassEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	  // Enables/disables the compass (icon in the top left that indicates the
	  // orientation of the
	  // map).
	  mUiSettings.setCompassEnabled(((CheckBox) v).isChecked());
	 }

	 // ----設定我的位置按鈕------
	 public void setMyLocationButtonEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	/*   Enables/disables the my location button (this DOES NOT enable/disable
	   the my location
	   dot/chevron on the map). The my location button will never appear if
	   the my location
	   layer is not enabled.*/
	  mUiSettings.setMyLocationButtonEnabled(((CheckBox) v).isChecked());
	 }

	 // -----顯示 我的位置座標圖示
	 public void setMyLocationLayerEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	/*   Enables/disables the my location layer (i.e., the dot/chevron on the
	   map). If enabled, it
	   will also cause the my location button to show (if it is enabled); if
	   disabled, the my
	   location button will never show.*/
	  map.setMyLocationEnabled(((CheckBox) v).isChecked());
	 }

	 // ---- 可用手勢操控
	 public void setScrollGesturesEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	  // Enables/disables scroll gestures (i.e. panning the map).
	  mUiSettings.setScrollGesturesEnabled(((CheckBox) v).isChecked());
	 }

	 // ---- 按兩下 按一下 或兩指拉大拉小----
	 public void setZoomGesturesEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	  // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
	  mUiSettings.setZoomGesturesEnabled(((CheckBox) v).isChecked());
	 }

	 public void setTiltGesturesEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	  // Enables/disables tilt gestures.
	  mUiSettings.setTiltGesturesEnabled(((CheckBox) v).isChecked());
	 }

	 public void setRotateGesturesEnabled(View v) {
	  if (!checkReady()) {
	   return;
	  }
	  // Enables/disables rotate gestures.
	  mUiSettings.setRotateGesturesEnabled(((CheckBox) v).isChecked());
	 }
//-------------------------------------
//----------------開啟各種Button TextView--------------
	private void setupviewcomponent() {
//		mSpnLocation = (Spinner) this.findViewById(R.id.spnLocation);
		mSpnMaptype = (Spinner) this.findViewById(R.id.spnMapType);
		stateText = (TextView) findViewById(R.id.m1701_T001);
		texZoom=(TextView)findViewById(R.id.texZoom);
		nowUser=(TextView)findViewById(R.id.nowUser);
		
		checkControl=(CheckBox)findViewById(R.id.checkcontrol);
		scrollCheckBox=(ScrollView)findViewById(R.id.scrollCheckBox);
		loginLayout=(LinearLayout)findViewById(R.id.loginLayout);
		
		
		loginLayout.setVisibility(View.INVISIBLE);
        
		
		checkControl.setOnCheckedChangeListener(checkOn);
		scrollCheckBox.setVisibility(View.INVISIBLE);
		// mMapView = (MapView) findViewById(R.id.map);

		// mMapCtrl = mMapView.getController();
		// mMapCtrl.setZoom(16);

//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
//
//		locations = getResources().getStringArray(R.array.location);
//		for (int i = 0; i < locations.length; i++) {
//			String[] splits = locations[i].split(",");
//			adapter.add(splits[0]);
//		}
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		mSpnLocation.setAdapter(adapter);
//		mSpnLocation.setOnItemSelectedListener(this);
      //------------------設定mapType Spinner--------
		ArrayAdapter<String> mapTypeadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		mapType = getResources().getStringArray(R.array.mapType);
		for (int i = 0; i < mapType.length; i++) {
			mapTypeadapter.add(mapType[i]);
		}
		mapTypeadapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		mSpnMaptype.setAdapter(mapTypeadapter);
		mSpnMaptype.setOnItemSelectedListener(mapTypeOn);
      //------------------------------------------
		loginPage();//開啟設定更入頁面的method
	}
	//----------------------------------------------------
	//-----------------------設定登入頁面--------------------------
	private void loginPage() {
		Button btnBack=(Button)findViewById(R.id.btnBack);
		Button btnRegUser=(Button)findViewById(R.id.btnRegUser);
		Button btnLogin=(Button)findViewById(R.id.btnLogin);
		 userName=(EditText)findViewById(R.id.editTexUserName);
		 userGroup=(EditText)findViewById(R.id.editTexUserGroup);
		
		btnBack.setOnClickListener(btnOn);
		btnRegUser.setOnClickListener(btnOn);
		btnLogin.setOnClickListener(btnOn);
		
	}
	//---------------------------------------------------
	//-------------------登入頁面按鈕行為設定----------
	private String getUserName;
	private long startTime;
	private Button.OnClickListener btnOn=new Button.OnClickListener()
	{	
		@Override
		public void onClick(View v) {
			loginLayout.setVisibility(View.VISIBLE);//show登入頁面
			
			switch (v.getId()) {
			case R.id.btnRegUser://按下註冊按鈕
				
				String Addr=getmyGPSlat+","+getmyGPSlng;
				String myName=userName.getText().toString().trim();
				String myGrp=userGroup.getText().toString().trim();
				if(!myName.equals("")&&!myGrp.equals(""))
				{
					
					insertSQL( myName,  myGrp, Addr);
					loginLayout.setVisibility(View.INVISIBLE);
					userName.setText("");
					userName.setHint("請輸入使用者姓名...");
					userGroup.setText("");
					userGroup.setHint("請輸入群組...");
					nowUser.setText("使用者:"+myName);
					db_mySQL();
					
					 handler.postDelayed(updateTimer, 5000);//開啟自動匯入
					 startTime = System.currentTimeMillis();
//					M1801.this.recreate();
				}else 
				{
				Toast.makeText(M1801.this, "請輸入帳號/群組",Toast.LENGTH_SHORT).show();//欄位沒有點入時候	
				return;
				}
				
				
				break;
			case R.id.btnLogin://按下登入按鈕
				getUserName=userName.getText().toString().trim();
				if(getUserName.equals(""))
				{
					Toast.makeText(M1801.this,"請輸入使用者姓名..", Toast.LENGTH_SHORT).show();//沒有填入使用者名稱
					return;
				}else if(!getUserName.equals(""))
				{
					int check=selectSQL(getUserName);//檢查是否有這個使用者
					if(check==1)//有這使用者
					{
					
						 handler.postDelayed(updateTimer, 5000);
						 startTime = System.currentTimeMillis();
						 
						loginLayout.setVisibility(View.INVISIBLE);
						userName.setText("");
						userName.setHint("請輸入使用者姓名...");
						userGroup.setText("");
						userGroup.setHint("請輸入群組...");
						nowUser.setText("使用者:"+getUserName);
					}else if(check==0)
					{
						Toast.makeText(M1801.this,"查無使用者請從新輸入",Toast.LENGTH_SHORT).show();
						userName.setText("");
						userName.setHint("請輸入使用者姓名...");
						userGroup.setText("");
						userGroup.setHint("請輸入群組...");
						
					}

				}
				
				break;

			case R.id.btnBack:
				
				loginLayout.setVisibility(View.INVISIBLE);
				userName.setText("");
				userName.setHint("請輸入使用者姓名...");
				userGroup.setText("");
				userGroup.setHint("請輸入群組...");
				break;
			}
			
		}
		
	};
	//-------------------------------------------------------------
	//-----------------------自動更新----------------------
	int update_time=0;
	private Runnable updateTimer = new Runnable() {
		@Override
		public void run() {
			Long spentTime = System.currentTimeMillis() - startTime;
			// 計算目前已過分鐘數
			Long minius = (spentTime / 1000) / 60;
			// 計算目前已過秒數
			Long seconds = (spentTime / 1000) % 60;
			handler.postDelayed(this, 30 * 1000); // 真正延遲的時間
			Date curDate = new Date(System.currentTimeMillis()); //  獲取當前時間
			String str = formatter.format(curDate);
//			Log.d(TAG, "run:" + str);
			myAddr=null;
			// -------執行匯入MySQL
			myAddr=getmyGPSlat+","+getmyGPSlng;
			mySQL_update(myID, myName, myGrp, myAddr);
			

			++update_time;
            Toast.makeText(M1801.this,"(每"+30+"秒)"+ str + " " +"\n目前更新了你的位置:"+ minius + ":" + seconds+" ("+update_time+"次)" , Toast.LENGTH_LONG).show();
//			nowtime.setText(getString(R.string.now_time) +"(每"+autotime+"秒)"+ str + " " +"\n目前更新了:"+ minius + ":" + seconds+" ("+update_time+"次)");
//			mSpnLocation.setSelection(old_index, true); // spinner 小窗跳到第幾筆
			// ---------------------------
            try {
				Thread.sleep(5000);
				db_mySQL();
				Thread.sleep(1000);
//				M1801.this.recreate();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	};
	//-----------------------------------------------------------
	//-----------------------使用者更新位置-----------------
	private void mySQL_update(String tid, String tname, String tgrp, String taddr) {

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		  nameValuePairs.add(new BasicNameValuePair("query_string", "update"));
		  nameValuePairs.add(new BasicNameValuePair("id", tid));
		  nameValuePairs.add(new BasicNameValuePair("name", tname));
		  nameValuePairs.add(new BasicNameValuePair("grp", tgrp));
		  nameValuePairs.add(new BasicNameValuePair("address", taddr));
		  String result = DBConnector.executeUpdate("update", nameValuePairs);
		  Log.d(TAG, "result:" + result);
		  Toast.makeText(getApplicationContext(), "mySQL:"+result, Toast.LENGTH_LONG).show();
		
	}
	//-------------------------------------------------------------
	//-------------------------寫入新的使用者----------------------
	private void insertSQL(String myName, String myGrp, String myAddr) {
		// -------------抓取遠端資料庫設定執行續------------------------------
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
				.detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(
				new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
		// ---------------------------------------------------------------------
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("query_string", "insert"));
		nameValuePairs.add(new BasicNameValuePair("name", myName));
		nameValuePairs.add(new BasicNameValuePair("grp", myGrp));
		nameValuePairs.add(new BasicNameValuePair("address", myAddr));
		try {
			Thread.sleep(500); // 延遲Thread 睡眠0.5秒
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String result = DBConnector.executeInsert("insert", nameValuePairs);

	}
	//---------------------------------------------------------
	//-------------------使用者登入時候 確認-----------------------
	private String myID,myName,myGrp,myAddr;
	private int selectSQL(String myName2) {
         int check=0;//檢察使否有這個使用者使用 有=1 無=0
		// --------------執行緒區段--------------------------
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
				.detectNetwork().penaltyLog().build());
		StrictMode.setVmPolicy(
				new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath().build());
		// --------------執行緒區段--------------------------
		try {
			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("query_string", "query"));
			params.add(new BasicNameValuePair("name", myName2));

			String result = DBConnector.executeQuery("query", params);
			/**************************************************************************
			 * SQL 結果有多筆資料時使用JSONArray 只有一筆資料時直接建立JSONObject物件 JSONObject
			 * jsonData = new JSONObject(result);
			 **************************************************************************/
			Log.d(TAG, "result->" + result);
			if (result.equals("查無姓名")) {
				Toast.makeText(M1801.this, "查無姓名" + myName, Toast.LENGTH_SHORT).show();
               check=0;
			} else {
				myID = null;myName = null;myGrp = null;myAddr = null;
				JSONArray jsonArray = new JSONArray(result);
				JSONObject jsonData = jsonArray.getJSONObject(0);
				//將登入者的ID NAME grp取出之後記錄起來
				myID = jsonData.getString("id").toString();
				myName = jsonData.getString("name").toString();
				myGrp = jsonData.getString("grp").toString();
				// myAddr=jsonData.getString("address");
                check=1;
			}
	
		} catch (Exception e) {
			// TODO: handle exception
		}
		return check;	

	}
	   
	private CheckBox.OnCheckedChangeListener checkOn=new CheckBox.OnCheckedChangeListener()
			{

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					if(checkControl.isChecked())
					{
						scrollCheckBox.setVisibility(View.VISIBLE);
					}else
					{
						scrollCheckBox.setVisibility(View.INVISIBLE);
					}
					
				}
		
			};
			// -----------------------------------------------------

	
	private OnItemSelectedListener mapTypeOn = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			switch (position) {
			case 0:
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 道路地圖。
				break;
			case 1:
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星空照圖
				break;
			case 2:
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); // 地形圖
				break;
			case 3:
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID); // 道路地圖混合空照圖
				break;
			case 4:// 顯示路況
				map.setTrafficEnabled(true);
				break;
			case 5:// 隱藏路況
				map.setTrafficEnabled(false);
				break;
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		map.clear();
		mytrace=null; //情除軌跡圖
		setupMapLocation();
		
		
	}
	private void setupMapLocation() {
		showloc(); //刷新所有景點
		LatGps=null;LngGps=null;
		int iSelect = mSpnLocation.getSelectedItemPosition();
		
		String[] sLocation= locations[iSelect].split(",");
		 LatGps = Double.parseDouble(sLocation[1]); // 南北緯
		 LngGps = Double.parseDouble(sLocation[2]); // 東西經
		 Toast.makeText(M1801.this, "你選擇的位置:\nlat"+LatGps+"\nlng:"+LngGps, Toast.LENGTH_SHORT).show();
		 
		 String idName = "q" + String.format("%02d", iSelect );
		 int resID = getResources().getIdentifier(idName, "raw",getPackageName());
		 
		 String settitle = sLocation[0]+"#"+resID;  //地名
		 
		VGPS=new LatLng(LatGps, LngGps);
		
		// --------------------------
//		  map.setMyLocationEnabled(true); // 顯示自己位置
		  // map.setTrafficEnabled(true); // 顯示交通資訊

//		  map.getUiSettings().setZoomControlsEnabled(true); // 顯示縮放按鈕
//		  map.getUiSettings().setCompassEnabled(true); // 顯示指北針
		//  map.getUiSettings().setScrollGesturesEnabled(true); // 開啟地圖捲動手勢
		//  map.getUiSettings().setZoomGesturesEnabled(true); // 開啟地圖縮放手勢
		//  map.getUiSettings().setTiltGesturesEnabled(true); // 開啟地圖傾斜手勢
		//  map.getUiSettings().setRotateGesturesEnabled(true); // 開啟地圖旋轉手勢 
		 // 层次(室內)缩放 map.getUiSettings().setIndoorLevelPickerEnabled(boolean)
		 // 地图工具（点击 marker 后会出现的小图标）map.getUiSettings().setMapToolbarEnabled(boolean).

		// --------------------------------
		//--- 根據所選位置項目顯示地圖/標示文字與圖片 ---//
		// 顯示圖標文字與照片
		// 用自己的圖片, 照片放到 raw目錄
		  if (vgps!=null) {
				vgps.remove();
		  }
		  image_des =  BitmapDescriptorFactory
	                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN); //使用系統水滴
		
		 vgps=map.addMarker(new MarkerOptions()
				                     .position(VGPS)
				                     .title(settitle)
				                     .snippet("座標:\nLat:"+LatGps+"\nLng"+LngGps)
				                     .icon(image_des)
				                     .infoWindowAnchor(3.2f,1f)
				                     );
		                             //.draggable(true)
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, 12));
	}
	  private void showloc() {
		  
		  int imgNum=0;
	   // 將所有景點位置顯示
	   for (int i = 0; i < locations.length; i++) {
		   String[] sLocation = null;BitmapDescriptor image = null;
		   String infoPic=null, vtitle=null;int infoimgID=0;
//	    String[] sLocation = locations[i].split(",");
//	    LatGps = Double.parseDouble(sLocation[1]); // 南北緯
//	    LngGps = Double.parseDouble(sLocation[2]); // 東西經
	    
	    // --- 設定所選位置之當地圖片 ---//
			switch (icosel) {
			case 0:
				sLocation = new String[3];
			   sLocation = locations[i].split(",");
			    LatGps = Double.parseDouble(sLocation[1]); // 南北緯
			    LngGps = Double.parseDouble(sLocation[2]); // 東西經
				image = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE); // 使用系統水滴
				
				infoPic="p0" + String.format("%02d", i+10 );
			      infoimgID=getResources().getIdentifier(infoPic, "raw",getPackageName());				      
			      vtitle = sLocation[0]+"#"+infoimgID;
				break;
			case 1:
				// 運用巨集
				sLocation = new String[3];
				 sLocation = locations[i].split(",");
				    LatGps = Double.parseDouble(sLocation[1]); // 南北緯
				    LngGps = Double.parseDouble(sLocation[2]); // 東西經
				if (!groupData[i].equals("A")) {//當group不等於A的時候(這是使用者的DATA)
	
					
				    infoimgID=getResources().getIdentifier("userconfig", "raw",getPackageName());				      
				     vtitle = sLocation[0]+"#"+infoimgID;
				      
					int resID = getResources().getIdentifier("p150", "raw", getPackageName());
					image = BitmapDescriptorFactory.fromResource(resID);
				} else if (groupData[i].equals("A")) {//
					
					 infoPic="q" + String.format("%02d", i );
				      infoimgID=getResources().getIdentifier(infoPic, "raw",getPackageName());				      
				      vtitle = sLocation[0]+"#"+infoimgID;
					
					String idName = "t" + String.format("%02d", imgNum);
					int resID = getResources().getIdentifier(idName, "raw", getPackageName());

					image = BitmapDescriptorFactory.fromResource(resID);// 使用照片
					imgNum++;
				}
				break;
			case 2:
				if (groupData[i].equals("A")) {
					sLocation = new String[3];
					 sLocation = locations[i].split(",");
					    LatGps = Double.parseDouble(sLocation[1]); // 南北緯
					    LngGps = Double.parseDouble(sLocation[2]); // 東西經
					String idName = "t" + String.format("%02d", imgNum);
					int resID = getResources().getIdentifier(idName, "raw", getPackageName());

					image = BitmapDescriptorFactory.fromResource(resID);// 使用照片
					imgNum++;
					
				      infoPic="q" + String.format("%02d", i );
				       infoimgID=getResources().getIdentifier(infoPic, "raw",getPackageName());				      
				      vtitle = sLocation[0]+"#"+infoimgID;
				}
				break;
			case 3:
				if (!groupData[i].equals("A")) {
					sLocation = new String[3];
					    sLocation = locations[i].split(",");
					    LatGps = Double.parseDouble(sLocation[1]); // 南北緯
					    LngGps = Double.parseDouble(sLocation[2]); // 東西經
					int resID = getResources().getIdentifier("p150", "raw", getPackageName());
					image = BitmapDescriptorFactory.fromResource(resID);
					
				     infoimgID=getResources().getIdentifier("userconfig", "raw",getPackageName());				      
				     vtitle = sLocation[0]+"#"+infoimgID;
				}
				break;
			}
			
			if (sLocation!=null) {
				 VGPS = new LatLng(LatGps,LngGps);//更新成欲顯示的地圖座標   
			      
//			      String infoPic="q" + String.format("%02d", i );
//			      int infoimgID=getResources().getIdentifier(infoPic, "raw",getPackageName());
//			      
//			      String vtitle = sLocation[0]+"#"+infoimgID;
			   
			     vgps = map.addMarker(new MarkerOptions()
			    		                       .position(VGPS)
			                                   .alpha(1f)
			                                   .title(vtitle)
			                                   .snippet("Location:\nLat:" + LatGps + "\nLng:" + LngGps)
			                                   .icon(image)
			                                   .draggable(true)
			                                   .infoWindowAnchor(1.5f, 0.2f));// 顯示圖標文字與照片 alpha 透明度 0~1
			}
	   }
	  }
//---------------------取出監聽到的位置---------------
	private double getmyGPSlat;
	private double getmyGPSlng;
		private void getGPSlocation(Location location) {
			String where = "";
			if(location!=null){
				double lat = location.getLatitude();
			    double	lng = location.getLongitude();
			    
			    float speed = location.getSpeed();
				long time = location.getTime();
				String timeStr = getTimeString(time);
				where = "經度：" + lng + "\n緯度：" + lat + "\n速度：" + speed + "\n時間：" + timeStr;

				// 標記"我的位置"
				showMarkerMe(lat, lng);
				cameraFocusOnMe(lat, lng);
				trackMe(lat, lng);
				
				getmyGPSlat=0;getmyGPSlng=0;//規0
				
				getmyGPSlat=lat;getmyGPSlng=lng;
				
//				map.addMarker(new MarkerOptions().position(VGPS)
//						.title("目前位置").snippet("座標:" + lat + "," + lng)
//						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//						.rotation(90));	//.showInfoWindow()
			}else{
				where = "GPS位置訊號消失！";
			}
			
		    stateText.setText(where);			
			
		}

		private void trackMe(double lat, double lng) {
			if (mytrace == null) {
				mytrace = new ArrayList<LatLng>();
			}
			mytrace.add(new LatLng(lat, lng));
			PolylineOptions polylineOpt = new PolylineOptions();
			for (LatLng latlng : mytrace) {
				polylineOpt.add(latlng);
			}
			polylineOpt.color(Color.RED); // 軌跡顏色
			Polyline line = map.addPolyline(polylineOpt);
			line.setWidth(10);  // 軌跡寬度
			
		}



		private void showMarkerMe(double lat, double lng) {
			if (markerMe != null) {
				markerMe.remove();
			}
			int  resID = getResources().getIdentifier("p025", "raw",
				      getPackageName());
			
			MarkerOptions markerOpt = new MarkerOptions().position(new LatLng(lat, lng))
					                                     .title("目前位置"+"#"+resID)
					                                     .snippet("GPS座標:\nLat:" + lat + "\nLng:" + lng)
					                                     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
					                                     ).infoWindowAnchor(2.5f, 1f);
			markerMe=map.addMarker(markerOpt);
//			locations[0]="目前位置,"+lat+","+lng;			
		}
		private void cameraFocusOnMe(double lat, double lng) { /* 移動地圖鏡頭 */
			CameraPosition camPosition = new CameraPosition.Builder()
					.target(new LatLng(lat, lng)).zoom(zoomsize).build();
			map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
		}


		private String getTimeString(long time) {
			
			SimpleDateFormat Dateformatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String sysTime = Dateformatter.format(time);
			return sysTime;
		}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			getGPSlocation(location);
		}

	}





	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		  case LocationProvider.OUT_OF_SERVICE:
		   Log.v(TAG, "Status Changed: Out of Service");
		   break;
		  case LocationProvider.TEMPORARILY_UNAVAILABLE:
		   Log.v(TAG, "Status Changed: Temporarily Unavailable");
		   break;
		  case LocationProvider.AVAILABLE:
		   Log.v(TAG, "Status Changed: Available");
		   break;
		  }

	}

	@Override
	public void onProviderEnabled(String provider) {
		locationMgr.requestLocationUpdates(provider,minTime, minDist, this);

	}

	@Override
	public void onProviderDisabled(String provider) {
		locationMgr.removeUpdates(this);
		getGPSlocation(null);
		Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
		startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));	//開啟設定頁面

	}
	/*-------------------------------------------------------
	   Marker GoogleMap.addMarker(MarkerOptions options)
	     依據MarkerOptions物件內容，在地圖加上地標。
	     options            MarkerOptions物件，用來描述地標內容。
	        地標內容是以MarkerOptions物件來描述，包括以下屬性：
	     position    LatLng物件，放置地標的地圖座標，此為必要項。
	     title       地標名稱，當使用者點擊地標時，會開啟訊息視窗顯示標題。
	     snippet     地標之附加說明文字，會顯示在標題文字下方。
	     draggable   是否允許使用者拖曳地標，預設值為false。
	     visible     是否顯示地標，預設值為true。
	     anchor      地標圖示與position座標值位置之對齊方式，預設是將地標圖示下緣中央貼齊position座標值。
	     icon        BitmapDescriptor物件，用來描述做為地標圖案之圖示。
	     例如下列程式碼在地圖中加入1個地標：
	       m_map = ((SupportMapFragment)
	       getSupportFragmentManager().findFragmentById(R.id.map)).getMap();  
	       MarkerOptions marker1 = new MarkerOptions().
	          position(new LatLng(23.95666, 120.68585)).
	          title("幸福之家").
	          snippet("中區雲端天堂之路入口,歡迎光臨.").
	          icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
	       m_map.addMarker(marker1); 
	       marker = mMap.addMarker(new MarkerOptions()
	                    .position(latlng)
	                    .icon(BitmapDescriptorFactory
	                            .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
	---------------------------------------------------------------*/
	//----------------------------------
	private boolean initLocationProvider() {// 取得現在位置
		locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;// 取得GPS資料
			return true;
		}

		return false;
	}
	private void nowaddress() {
		// TODO Auto-generated method stub
		// 取得上次已知位置
		Location location = locationMgr.getLastKnownLocation(provider);
		getGPSlocation(location);
		// 取得GPS listener
		locationMgr.addGpsStatusListener(gpsListener);

		// Location Listener

		locationMgr.requestLocationUpdates(provider, minTime, minDist, this);

	}
	GpsStatus.Listener gpsListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				
				break;

			case GpsStatus.GPS_EVENT_STOPPED:
				
				break;

			case GpsStatus.GPS_EVENT_FIRST_FIX:
				
				break;

			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				
				break;
			}
		}
		
	};
	
	
	
	@Override
	protected void onStart() {
		initMap();
		if (initLocationProvider()) {
			nowaddress();

		} else {
			stateText.setText("GPS未開啟，請開啟GPS...");

		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		initMap();
		if (initLocationProvider()) {
			nowaddress();

		} else {
			stateText.setText("GPS未開啟，請開啟GPS...");

		}
//		locationMgr.requestLocationUpdates(provider,minTime, minDist,this);
		super.onResume();
	}

	@Override
	protected void onStop() {
		locationMgr.removeUpdates(this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		locationMgr.removeUpdates(this);
		super.onDestroy();
	}
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.m1701, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.imgItem1:
			if(icosel!=1&&icosel!=0)
			{
				icosel=1;
			}
			map.clear();//清除icon
			if(icosel==1){
				icosel=0;
				showloc();
			}else if (icosel==0) {
				icosel=1;
				showloc();
			}
			
			break;
		case R.id.locItem:
			map.clear();
			icosel=2;
			showloc();
			break;
		case R.id.memberItem:
			map.clear();
			icosel=3;
			showloc();
			break;
		case R.id.userItem:
//			if (loginPage==1) {
				loginLayout.setVisibility(View.VISIBLE);
//				loginPage=0;
//			}
			
			break;
		case R.id.action_settings:
			break;
		
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public View getInfoContents(Marker marker) {
//		String[] getTitle=marker.getTitle().split("#");
//		
//		 View infoWindow = getLayoutInflater().inflate(R.layout.infowindow_style, null);
//		 
//		 infoTitle = (TextView) infoWindow.findViewById(R.id.infoTitle);
//		 infoTitle.setText(getTitle[0]);
//		 
//		 infoContent = (TextView) infoWindow.findViewById(R.id.infoContent);
//		 infoContent.setText(marker.getSnippet());
//		 
//		 ImageView infoImg=(ImageView)infoWindow.findViewById(R.id.infoImg);
//		 infoImg.setImageResource(Integer.parseInt(getTitle[1]));
		 
		return null;
	}



	@Override
	public View getInfoWindow(Marker marker) {
		String[] getTitle=marker.getTitle().split("#");
		
		 View infoWindow = getLayoutInflater().inflate(R.layout.infowindow_style, null);
		 
		 infoTitle = (TextView) infoWindow.findViewById(R.id.infoTitle);
		 infoTitle.setText(getTitle[0]);
		 
		 infoContent = (TextView) infoWindow.findViewById(R.id.infoContent);
		 infoContent.setText(marker.getSnippet());
		 
		 ImageView infoImg=(ImageView)infoWindow.findViewById(R.id.infoImg);
		 infoImg.setImageResource(Integer.parseInt(getTitle[1]));
		 
		return infoWindow;
	}



	@Override
	public void onCameraChange(CameraPosition camPosition) {
		Log.d(TAG, "onCameraChange");
		
		currentZoom = camPosition.zoom; // here you get zoom level
		zoomsize = currentZoom;
		texZoom.setText("目前Zoom:"+currentZoom);
	}
	@Override
	public void onMapReady(GoogleMap gmap) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onMyLocationButtonClick() {
		
		return false;
	}
	@Override
	public boolean onMarkerClick(final Marker marker_Animation) {
		Log.d(TAG, "非GPS移動位置");
		   // 設定動畫
		   final Handler handler = new Handler();
		   final long start = SystemClock.uptimeMillis();
		   final long duration = 1500;

		   final Interpolator interpolator = new BounceInterpolator();

		   handler.post(new Runnable() {
		    @Override
		    public void run() {
		     long elapsed = SystemClock.uptimeMillis() - start;
		     float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
		     marker_Animation.setAnchor(0.5f, 1.0f + 2 * t);

		     if (t > 0.0) {
		      // Post again 16ms later.
		      handler.postDelayed(this, 16);
		     }
		    }
		   });
		return false;
	}











	





}
