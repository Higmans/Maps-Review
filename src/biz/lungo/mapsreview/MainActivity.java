package biz.lungo.mapsreview;

import java.io.IOException;
import java.util.List;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CameraPosition.Builder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.location.Address;
import android.location.Geocoder;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnMapClickListener {
	public static String CLEAN;
	public static String PICK_AREA;
	public static String MAP_TYPE;
	public static String INFO;
	String standard;
	String hybrid;
	String political;
	String physical;
	String none;
	double latitude = 50.7;
	double longitude = 30.7;
	FragmentManager manager = getFragmentManager();
	TextView textInfo;

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
		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Builder builder = CameraPosition.builder();
		builder.target(new LatLng(50, 30));
		builder.zoom(10.0f);
		builder.tilt(25.0f);
		builder.bearing(45.0f);
		CameraPosition cp = builder.build();
		CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cp);
		googleMap.animateCamera(cu);
		googleMap.setOnMapClickListener(this);

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
		// create marker
		MarkerOptions marker = new MarkerOptions().position(
				new LatLng(latitude, longitude)).title("Hello Maps");

		// adding marker
		googleMap.addMarker(marker);
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
			Toast.makeText(this, item.getTitle(), Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
	}
	
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
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplication(), android.R.layout.simple_list_item_2, mapTypeKeys);
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
}