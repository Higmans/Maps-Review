package biz.lungo.mapsreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnMapClickListener, OnMapLongClickListener, OnClickListener {
	public static String CLEAN;
	public static String PICK_AREA;
	public static String MAP_TYPE;
	public static String INFO;
	static final int TILT_STEP = 20;
	static final  int TURN_STEP = 20;
	String standard;
	String hybrid;
	String political;
	String physical;
	String none;
	double latitude = 50.7;
	double longitude = 30.7;
	FragmentManager manager = getFragmentManager();
	TextView textInfo;
	Button tiltUp, tiltDown, turnLeft, turnRight;
	List<LatLng> llList;
	int markerCounter = 0;
	String markerNames[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
	int maxMarkersCount = markerNames.length;
	Builder builder;
	CameraPosition cp;
	CameraUpdate cu;
	static SensorManager mSensorManager;

	// Google Map
	private GoogleMap googleMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		CLEAN = getResources().getString(R.string.clean);
		PICK_AREA = getResources().getString(R.string.pick_area);
		MAP_TYPE = getResources().getString(R.string.map_type);
		INFO = getResources().getString(R.string.info);
		standard = getResources().getString(R.string.standard);
		hybrid = getResources().getString(R.string.hybrid);
		political = getResources().getString(R.string.political);
		physical = getResources().getString(R.string.physical);
		none = getResources().getString(R.string.none);
		textInfo = (TextView) findViewById(R.id.textViewInfo);
		tiltUp = (Button) findViewById(R.id.buttonTiltUp);
		tiltDown = (Button) findViewById(R.id.buttonTiltDown);
		turnLeft = (Button) findViewById(R.id.buttonTurnLeft);
		turnRight = (Button) findViewById(R.id.buttonTurnRight);
		tiltUp.setOnClickListener(this);
		tiltDown.setOnClickListener(this);
		turnLeft.setOnClickListener(this);
		turnRight.setOnClickListener(this);
		llList = new ArrayList<LatLng>();
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
		builder = CameraPosition.builder();
		//builder.zoom(10.0f);
		//cp = builder.build();
		//cu = CameraUpdateFactory.newCameraPosition(cp);
		//googleMap.moveCamera(cu);
		googleMap.setOnMapClickListener(this);
		googleMap.setOnMapLongClickListener(this);
		Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		
		final SensorEventListener sEListener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				builder.target(googleMap.getCameraPosition().target);
				builder.zoom(googleMap.getCameraPosition().zoom);
				builder.bearing(googleMap.getCameraPosition().bearing);
				textInfo.setVisibility(View.VISIBLE);
				textInfo.setText(event.values[1] * 10 + "");
				float maxTilt = getMaxTilt(googleMap.getCameraPosition().zoom);
				if (event.values[1] * 10 <= maxTilt && event.values[1] * 10 >= 0){
					builder.tilt(event.values[1] * 10);
				}
				else if (event.values[1] * 10 <= 0){
					builder.tilt(0);
				}
				else if (event.values[1] * 10 >= maxTilt){
					builder.tilt(maxTilt);
				}
				cp = builder.build();
				cu = CameraUpdateFactory.newCameraPosition(cp);
				googleMap.animateCamera(cu);
			}
			
			private float getMaxTilt(float zoom) {
				float tilt = 30.0f;				 
			    if (zoom > 15.5f) {
			        tilt = 67.5f;
			    } else if (zoom >= 14.0f) {
			        tilt = (((zoom - 14.0f) / 1.5f) * (67.5f - 45.0f)) + 45.0f;
			    } else if (zoom >= 10.0f) {
			        tilt = (((zoom - 10.0f) / 4.0f) * (45.0f - 30.0f)) + 30.0f;
			    }			 
			    return tilt;
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				
			}
		};
		mSensorManager.registerListener(sEListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Fragment f = getFragmentManager()
				.findFragmentById(R.id.map);
		if (f != null)
			getFragmentManager().beginTransaction().remove(f).commit();
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(INFO);
		menu.add(MAP_TYPE);
		menu.add(PICK_AREA);
		menu.add(CLEAN);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (INFO.equals(item.getTitle())) {
			Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
		} else if (MAP_TYPE.equals(item.getTitle())) {
			MapTypeDialog mtd = new MapTypeDialog();
			mtd.show(manager, MAP_TYPE);
		} else if (PICK_AREA.equals(item.getTitle())) {
			Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
		} else if (CLEAN.equals(item.getTitle())) {
			googleMap.clear();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("ValidFragment")
	class MapTypeDialog extends DialogFragment{
		final String mapTypeKeys[] = {standard, hybrid, political, physical, none};
		final int mapTypeValues[] = {GoogleMap.MAP_TYPE_NORMAL, 
									GoogleMap.MAP_TYPE_HYBRID, 
									GoogleMap.MAP_TYPE_TERRAIN, 
									GoogleMap.MAP_TYPE_SATELLITE, 
									GoogleMap.MAP_TYPE_NONE};
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			ListView lv = new ListView(getActivity());
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(), android.R.layout.simple_selectable_list_item, mapTypeKeys);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
					googleMap.setMapType(mapTypeValues[index]);
					dismiss();
				}
			});
			return lv;
		}
	}

	@Override
	public void onMapClick(LatLng ll) {
		double lat = ll.latitude;
		double lng = ll.longitude;
		Geocoder geocoder = new Geocoder(this);
		List<Address> fromLocation = null;
		try {
			fromLocation = geocoder.getFromLocation(lat, lng, 10);
		} catch (IOException e) {			
		}
		String addressLine1 = fromLocation.get(0).getAddressLine(0);
		String addressLine2 = fromLocation.get(0).getAddressLine(1);
		String addressLine3 = fromLocation.get(0).getAddressLine(2);
		textInfo.setVisibility(View.VISIBLE);
		textInfo.setText(addressLine1 + "\n" + addressLine2 + "\n" + addressLine3);
	}

	@Override
	public void onMapLongClick(LatLng ll) {
		if (markerCounter < maxMarkersCount) {
			MarkerOptions marker = new MarkerOptions().position(ll).title(markerNames[markerCounter]);
			llList.add(ll);
			markerCounter++;
			googleMap.addMarker(marker);
		}
		else{
			Toast.makeText(this, "No more markers left!", Toast.LENGTH_LONG).show();
		}
		if (markerCounter > 1){
			drawPath(llList);
		}
	}

	private void drawPath(List<LatLng> points) {
		int count = points.size();
		PolylineOptions polyLine = new PolylineOptions();
		polyLine.color(Color.BLUE);
		for (int i = 0; i < count; i++){
			polyLine.add(points.get(i));
		}
		polyLine.geodesic(true);
		googleMap.addPolyline(polyLine);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.buttonTiltUp:
			if (cp.tilt + TILT_STEP <= 90)
				builder.tilt(cp.tilt + TILT_STEP);
			else 
				builder.tilt(90);
			break;
		case R.id.buttonTiltDown:
			if (cp.tilt - TILT_STEP >= 0)
				builder.tilt(cp.tilt - TILT_STEP);
			else
				builder.tilt(0);
			break;
		case R.id.buttonTurnLeft:
			builder.bearing(cp.bearing - TURN_STEP);
			break;
		case R.id.buttonTurnRight:
			builder.bearing(cp.bearing + TURN_STEP);
			break;
		}
		cp = builder.build();
		//cu = CameraUpdateFactory.newCameraPosition(cp);
		//googleMap.animateCamera(cu);
	}	
}