package edu.upi.mobprogproject.content;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import edu.upi.mobprogproject.R;
import edu.upi.mobprogproject.api.mainmap.MainMap;
import edu.upi.mobprogproject.api.mainmap.MainMap_;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST = 99;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private double lon, lat;

    //    static final LatLng GIK = new LatLng(-6.860426,107.589880);
    private Toolbar toolbar4;
    MapView mapView;
    private GoogleMap map;
    //    private SupportMapFragment map;
    MainMap mainmap = new MainMap();

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        //10 detik sekali minta lokasi (10000ms = 10 detik)
        mLocationRequest.setInterval(10000);
        //tapi tidak boleh lebih cepat dari 5 detik
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

//        map = ((SupportMapFragment)getFragmentManager().findFragmentById(R.id.mapView)).getMap();

        toolbar4 = rootView.findViewById(R.id.toolbar3);
        if (toolbar4 != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar4);
        }
        //toolbar4.setTitle(null);

        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                map = mMap;
                LatLng bandung = new LatLng(-6.90389, 107.61861);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(bandung).zoom(12).build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // For showing a move to my location button
                //map.setMyLocationEnabled(true);
                /*
                // For dropping a marker at a point on the Map
                LatLng sydney = new LatLng(-6.860426, 107.589880);
                map.addMarker(new MarkerOptions().position(sydney).title("I'm Here!").snippet("Saya disini heem iyah."));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                */
            }
        });

//        googleMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapView)).getMap();
//        map.getMapAsync(this);
        createLocationRequest();
        buildGoogleApiClient();

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new AmbilData().execute("192.168.43.1/kindle/mapdata \n"); //url jadi parameter
        } else {
            // tampilkan error
            Toast t = Toast.makeText(getActivity(), "Tidak ada koneksi!", Toast.LENGTH_LONG);
            t.show();
        }
        return rootView;
    }

    private class AmbilData extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... strUrl) {
            Log.v("kdl", "mulai ambil data");
            String result = "done";
            InputStream is = null;
            try {
                URL url = new URL(strUrl[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //timeout
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);

                conn.setRequestMethod("GET");
                conn.connect();

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mainmap = mapper.readValue(conn.getInputStream(), MainMap.class);
                } catch (Exception e) {
                    Log.i("yw", e.toString());
                    result = "fail";
                } finally {
                    result = "fail";
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.i("yw", "Error closing InputStream");
                        }
                    }
                }
            } catch (MalformedURLException e) {
                result = "conn";
                e.printStackTrace();
            } catch (IOException e) {
                result = "conn";
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            //tvHasil.setText(result);

            List<MainMap_> data = mainmap.getMainMap();
            if (data != null) {
                for (MainMap_ x : data) {
                    LatLng alert = new LatLng(Double.parseDouble(x.getLat()), Double.parseDouble(x.getLon()));
                    map.addMarker(new MarkerOptions().position(alert).title(x.getNama()));
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //cek permission
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //tampilkan dialog minta ijin
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
            map.setMyLocationEnabled(true);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
        //Toast.makeText(getActivity(),"lat "+lat+" lon "+lon,Toast.LENGTH_LONG).show();
        //Log.i("loc", "lat "+lat+" lon "+lon);
        //lat -6.9414389 lon 107.722049
        //lat":"-6.860418","lon":"107.589889
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(lat, lon);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mCurrLocationMarker = map.addMarker(markerOptions);

        //move map camera
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(17));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //permission diberikan, mulai ambil lokasi
            createLocationRequest();
            buildGoogleApiClient();

        } else {
            //ijin tidak diberikan, tampilkan pesan
            AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
            ad.setMessage("Tidak mendapat ijin");
            ad.show();
        }
    }

}