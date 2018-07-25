package com.appbasic.taskgooglemapnew;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener {
    private GoogleApiClient client;
    RequestQueue requestQueue;
    private RelativeLayout direction_layout;
    private EditText location_name, location_name2;
    private Button btn_search, btn_direction;
    private ImageView direction_img;
    private TextView btn_nearplace;
    private GoogleMap mMap;
    String locationname, locationname2;
    Geocoder geocoder;
    private int width, height;
    public static final int REQUEST_LOCATION_CODE = 99;
    double latitude, longitude;
    private LocationRequest locationRequest;
    int PROXIMITY_RADIUS = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d("zzzzz", "onCreate");
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        requestQueue = Volley.newRequestQueue(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this);

        location_name = (EditText) findViewById(R.id.location_name);
        location_name.getLayoutParams().width = (width / 5) * 3;
        location_name.getLayoutParams().height = height / 10;

        location_name2 = (EditText) findViewById(R.id.location_name2);
        location_name2.getLayoutParams().width = (width / 4) * 3;
        location_name2.getLayoutParams().height = height / 10;

        btn_search = (Button) findViewById(R.id.btn_search);
        btn_search.getLayoutParams().width = width / 4;
        btn_search.getLayoutParams().height = height / 10;

        btn_direction = (Button) findViewById(R.id.btn_direction);
        btn_direction.getLayoutParams().width = width / 4;
        btn_direction.getLayoutParams().height = height / 10;

        direction_img = (ImageView) findViewById(R.id.direction_img);
        direction_img.getLayoutParams().width = width / 4;
        direction_img.getLayoutParams().height = height / 10;

        direction_layout = (RelativeLayout) findViewById(R.id.direction_layout);
        btn_nearplace = (TextView) findViewById(R.id.btn_nearplace);



        btn_direction.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        direction_img.setOnClickListener(this);
        btn_nearplace.setOnClickListener(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("zzzzz", "onMapReady");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

    }

    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("zzzzz", "onLocationChanged");
        Toast.makeText(this, "onLocationChanged", Toast.LENGTH_LONG).show();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(getCompleteAddressString(latLng.latitude, latLng.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_black_24dp));
        mMap.addMarker(options);

        if (client != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }
    }


    private void GetPolyLines(final String locationname, final String locationname2) {

        String Origin = locationname;
        String Dest = locationname2;
        String URL = "https://maps.googleapis.com/maps/api/directions/json?origin=" + Origin + "&destination=" + Dest;
        Log.d("routes", "mkjndsicvsdcdc");
        markerspos(locationname, locationname2);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), "working", Toast.LENGTH_SHORT).show();

                try {
                    JSONArray routes = response.getJSONArray("routes");

                    Log.d("routes", "mkjndsicvsdcdc" + routes.length());

                    JSONObject first = routes.getJSONObject(0);

                    JSONArray legs = first.getJSONArray("legs");

                    JSONObject second = legs.getJSONObject(0);

                    JSONArray steps = second.getJSONArray("steps");
                    //Polyline polyline;
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.width(10);
                    polylineOptions.color(Color.RED);


                    for (int i = 0; i < steps.length(); i++) {

                        JSONObject obj = steps.getJSONObject(i);


                        JSONObject line = obj.getJSONObject("polyline");


                        String lineStr = line.getString("points");
                        Log.d("lineStr", "lineStr" + lineStr);

                        polylineOptions.addAll(PolyUtil.decode(lineStr));
                    }


                    mMap.addPolyline(polylineOptions);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("ccccc", "nnnnn");
            }
        });


        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);


    }

    private void markerspos(String locationname, String locationname2) {
        try {
            List<Address> addressListnew1 = geocoder.getFromLocationName(locationname, 1);
            Address addressnew1 = addressListnew1.get(0);
            LatLng latLngnew1 = new LatLng(addressnew1.getLatitude(), addressnew1.getLongitude());
            MarkerOptions markerOptionsnew1 = new MarkerOptions()
                    .position(latLngnew1)
                    .title(getCompleteAddressString(latLngnew1.latitude, latLngnew1.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_black_24dp));
            mMap.addMarker(markerOptionsnew1);

            List<Address> addressListnew2 = geocoder.getFromLocationName(locationname2, 1);
            Address addressnew2 = addressListnew2.get(0);
            LatLng latLngnew2 = new LatLng(addressnew2.getLatitude(), addressnew2.getLongitude());
            MarkerOptions markerOptionsnew2 = new MarkerOptions()
                    .position(latLngnew2)
                    .title(getCompleteAddressString(latLngnew2.latitude, latLngnew2.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_black_24dp));
            mMap.addMarker(markerOptionsnew2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("location address", strReturnedAddress.toString());
            } else {
                Log.w("location address", "No Address returned!");
                strAdd = "Unable get Address!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("location address", "Canont get Address!");
            strAdd = "Unable get Address!";
        }
        return strAdd;
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;

        } else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (client == null) {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
        }
    }


    @Override
    public void onClick(View v) {

        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        switch (v.getId()) {

            case R.id.btn_search:
                mMap.clear();
                locationname = location_name.getText().toString().trim();

                List<Address> addressList;
                if (!locationname.equals("")) {

                    try {
                        addressList = geocoder.getFromLocationName(locationname, 5);

                        if (addressList != null) {
                            for (int i = 0; i < addressList.size(); i++) {
                                LatLng latLng = new LatLng(addressList.get(i).getLatitude(), addressList.get(i).getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(locationname);
                                mMap.addMarker(markerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;

            case R.id.btn_direction:
                mMap.clear();
                locationname = location_name.getText().toString().trim();
                locationname2 = location_name2.getText().toString().trim();
                GetPolyLines(locationname, locationname2);
                break;
            case R.id.direction_img:
                if (direction_layout.getVisibility() == View.VISIBLE) {
                    direction_layout.setVisibility(View.GONE);
                    direction_img.setImageResource(R.drawable.directionicon2);
                } else {
                    direction_layout.setVisibility(View.VISIBLE);
                    direction_img.setImageResource(R.drawable.directioniconnew);
                }
                break;

            case R.id.btn_nearplace:
                Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                mMap.clear();
                String hospital = "hospital";
                String url = getUrl(latitude, longitude, hospital);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                break;

        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    ////near By places code here...
    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + "AIzaSyCbqbZib-w7XwJUO8I1IhNStmxNZLMqdO8");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString() + "   " + latitude + "  " + longitude);
        https:
//maps.googleapis.com/maps/api/place/nearbysearch/json?location=17.4357936,78.3922214&radius=10000&type=hospital&sensor=true&key=AIzaSyDaLAZMGG3ECVkTXroUENsU5QEcb2XpHaM
        return googlePlaceUrl.toString();
    }

}
