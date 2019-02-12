package tony.dev.mohamed.myuberridder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dd.processbutton.iml.ActionProcessButton;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuberridder.FirebaseService.IFCMService;
import tony.dev.mohamed.myuberridder.helper.Common;
import tony.dev.mohamed.myuberridder.helper.CustomInfoWindow;
import tony.dev.mohamed.myuberridder.models.DataMessage;
import tony.dev.mohamed.myuberridder.models.FCMResponse;
import tony.dev.mohamed.myuberridder.models.Rider;

import static tony.dev.mohamed.myuberridder.helper.Common.isDriverFound;
import static tony.dev.mohamed.myuberridder.helper.Common.mDriverId;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, ValueEventListener {

    private static final String TAG = "medoTony";
    private static final int REQUEST_CHECK_SETTINGS = 555;
    public LocationCallback mLocationCallback;
    /**
     * because fusedlocation is deprecated we updated the new one
     * to get location update
     */
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLAY_CREMENT = 10;
    //int variable for request code to pick images from gallary
    private static final int RC_PHOTO_PICKER = 2;
    //spots dialog to popup when response waiting
    private SpotsDialog dialog;
    //
    public static final int LIMIT = 3;
    //int variable for request code to FINE_LOCATION
    private static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 7000;

    /**
     * Firebase Utils
     */
    //presence system
    DatabaseReference driversAvailable;
    //vrialbes for storage
    private FirebaseStorage mStorage;
    private StorageReference mReference;

    /**
     * Google Utils
     */
    //(built in)search fragement for places suggestion
    private PlaceAutocompleteFragment locationPlace, destinationPlace;
    private String placeLocation, placeDestination;
    private LatLng currentLatlng,destinatioLatLng;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private Marker mUserMarker, markerDestination;
    //map annimation
    private MapRipple mapRipple;
/*    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mDrivers;*/

    /*private MaterialAnimatedSwitch location_switch;*/
    /*private GeoFire geoFire;*/

    private SupportMapFragment mapFragment;
    /**
     * footer view
     */
    //bottom sheet come up from the bottom of the screen
    private BottomSheetRiderFragment bottomSheetRiderFragment;
    private ImageView imageExpandele;
    //pickup request button
    private ActionProcessButton submitProcessButton;

    private int raduis = 1;
    private int distance = 1;
    //
    private IFCMService ifcmService;
    private String tookken, tokenStr;

    private AutocompleteFilter autocompleteFilter;
    private FirebaseAuth mAuth;

    /**
     * views for update rider information
     */
    private CircleImageView riderImage;
    private TextView riderName, txtStars;
    private View navigationViewLayout;

    /**
     * car type
     */
    private ImageView carUberX, carUberBlack;
    private boolean isUberX = true;

    /**
     * reciever for cancel request
     * it will be triggered when driver cancel the request
     */
    private BroadcastReceiver mCancelBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            submitProcessButton.setText("PICKUP REQUEST");
            submitProcessButton.setProgress(0);
            Common.mDriverId = "";
            Common.isDriverFound = false;
            submitProcessButton.setEnabled(true);
            if(mapRipple!=null) {
                if (mapRipple.isAnimationRunning())
                    mapRipple.stopRippleMapAnimation();
            }

            mUserMarker.hideInfoWindow();

        }
    };
    /**
     * reciever for dropOff sitaution
     * it will be triggered when the ridder click drop off button
     */
    private BroadcastReceiver mDropOffBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            submitProcessButton.setText("PICKUP REQUEST");
            Common.mDriverId = "";
            Common.isDriverFound = false;
            submitProcessButton.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /**
         * custom toolbar
         */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * register the broadcast recievers
         */
        //register for cancel broad cast
        LocalBroadcastManager.getInstance(this).registerReceiver(mCancelBroadCast, new IntentFilter(Common.CANCEL_BROADCAST_STRING));
        //register for arrived broad cast
        LocalBroadcastManager.getInstance(this).registerReceiver(mDropOffBroadCast, new IntentFilter(Common.BROADCAST_DROPOFF));
        /**
         * Navigation Drawer (Side Menue)
         */
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /**
         * google maps fragment
         */
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /**
         * image views for car types
         */
        carUberX = findViewById(R.id.carUberX);
        carUberBlack = findViewById(R.id.carUberBlack);

        //intit firebaseAuth viarible
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            /**
             * FirebaseStorage to store files , images viedeos..etc
             */
            //init storage for images
            mStorage = FirebaseStorage.getInstance();
            mReference = mStorage.getReference().child("riders_images");
            //read info and set it in nav bar
            navigationViewLayout = navigationView.getHeaderView(0);
            riderImage = navigationViewLayout.findViewById(R.id.user_imageView);
            riderName = navigationViewLayout.findViewById(R.id.ridder_name);

            txtStars = navigationViewLayout.findViewById(R.id.star_text);

            FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Rider user = dataSnapshot.getValue(Rider.class);
                    if (user != null) {
                        //  Picasso.get().load(user.getPhoto()).resize(150,150).centerCrop().into(driverImage);
                        if (user.getPhoto() != null) {
                            Glide.with(navigationViewLayout).load(user.getPhoto()).apply(new RequestOptions().override(150, 150)).into(riderImage);
                        }
                        riderName.setText(String.format("%s", user.getRates()));
                        riderName.setText(String.format("%s", user.getName()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            ifcmService = Common.getFcmService();

            // pickup request
            submitProcessButton = findViewById(R.id.pickupRequest);
            submitProcessButton.setMode(ActionProcessButton.Mode.ENDLESS);
            submitProcessButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isDriverFound) {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            if (Common.mLastLocation != null) {
                                submitProcessButton.setProgress(1);
                                requestPickup(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            } else {
                                checktheGps();
                            }
                        }
                    } else {
                        submitProcessButton.setEnabled(false);
                        sendRequestToDriver(mDriverId);
                    }

                }

                private void checktheGps() {
                   /* //  if(mAuth.getCurrentUser()!=null){
                    if (mAuth.getCurrentUser() != null) {
                        *//**
                     * check if gbs is open or not , if not
                     * will open setting to open it
                     *//*
                        final AlertDialog.Builder myBuilder = new AlertDialog.Builder(Home.this);
                        myBuilder.setMessage("Please Open the gps !");
                        myBuilder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        myBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                *//**
                     * method for
                     * check if gbs is open or not , if not
                     * will open setting to open it
                     *//*

                                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }

                            }
                        });
                        myBuilder.show();*/


                    //}

                    /*GoogleApiClient googleApiClient = new GoogleApiClient.Builder(Home.this)
                            .addApi(LocationServices.API).build();
                    googleApiClient.connect();

                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(10000 / 2);

                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);

                    PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    Log.i(TAG, "All location settings are satisfied.");
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                                    try {
                                        // Show the dialog by calling startResolutionForResult(), and check the result
                                        // in onActivityResult().
                                        status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.i(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                                    break;
                            }
                        }
                    });*/

                    //================================================================================================

                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(10000 / 2);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                           // .addLocationRequest(mLocationRequestBalancedPowerAccuracy);

                    Task<LocationSettingsResponse> result =
                            LocationServices.getSettingsClient(Home.this).checkLocationSettings(builder.build());

                    result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                        @Override
                        public void onComplete(Task<LocationSettingsResponse> task) {
                            try {
                                LocationSettingsResponse response = task.getResult(ApiException.class);
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
             //...
                            } catch (ApiException exception) {
                                switch (exception.getStatusCode()) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        // Location settings are not satisfied. But could be fixed by showing the
                                        // user a dialog.
                                        try {
                                            // Cast to a resolvable exception.
                                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                                            // Show the dialog by calling startResolutionForResult(),
                                            // and check the result in onActivityResult().
                                            resolvable.startResolutionForResult(
                                                    Home.this,
                                                    REQUEST_CHECK_SETTINGS);
                                        } catch (IntentSender.SendIntentException e) {
                                            // Ignore the error.
                                        } catch (ClassCastException e) {
                                            // Ignore, should be an impossible error.
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // Location settings are not satisfied. However, we have no way to fix the
                                        // settings so we won't show the dialog.
                    // ...
                                        break;
                                }
                            }
                        }
                    });
                    //================================================================================================
                }
            });

            locationPlace = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
            destinationPlace = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);
            // to restrict search to city
            autocompleteFilter = new AutocompleteFilter.Builder()
                    .setCountry("eg")
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .setTypeFilter(3)
                    .build();

            locationPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    currentLatlng=place.getLatLng();
                    Common.mLastLocation.setLatitude(place.getLatLng().latitude);
                    Common.mLastLocation.setLatitude(place.getLatLng().longitude);
                    placeLocation = place.getAddress().toString();
                    //clear old marker
                    mMap.clear();
                    //set new Marker
                    mMap.addMarker(new MarkerOptions()
                            .title("Pickup Here")
                            .position(place.getLatLng())
                            // .icon(BitmapDescriptorFactory.defaultMarker())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                }

                @Override
                public void onError(Status status) {

                }
            });
            destinationPlace.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    destinatioLatLng=place.getLatLng();
                    placeDestination = place.getAddress().toString();
                    //place.getLatLng()
                    //set new Marker for destination
                    mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                    );
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                           /* BottomSheetRiderFragment bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance(placeLocation, placeDestination, false);
                            bottomSheetRiderFragment.show(getSupportFragmentManager(), bottomSheetRiderFragment.getTag());*/
                            BottomSheetRiderFragment bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance(String.format("%f,%f", currentLatlng.latitude,currentLatlng.longitude), String.format("%f,%f", destinatioLatLng.latitude, destinatioLatLng.longitude), true);
                            bottomSheetRiderFragment.show(getSupportFragmentManager(), bottomSheetRiderFragment.getTag());
                        }
                    }, 3000);

                }

                @Override
                public void onError(Status status) {

                }
            });
            //intit loacation
            setUpLocation();
            //update refreshed token of the user
            updateFirebaseToken();

            //set on click listenenr to car types
            carUberX.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    isUberX = true;
                    if (isUberX) {
                        carUberX.setImageResource(R.drawable.car_cui_select);
                        carUberBlack.setImageResource(R.drawable.car_vip);
                    } else {
                        carUberX.setImageResource(R.drawable.car_cui);
                        carUberBlack.setImageResource(R.drawable.car_vip_select);
                    }
                    mMap.clear();
                    if (driversAvailable != null)
                        driversAvailable.removeEventListener(Home.this);
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl).child(isUberX ? "UberX" : "Uber Black");
                    driversAvailable.addValueEventListener(Home.this);
                    if (Common.mLastLocation != null) {
                        loadAllDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                    }

                }
            });
            carUberBlack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isUberX = false;
                    if (isUberX) {
                        carUberX.setImageDrawable(ContextCompat.getDrawable(Home.this, R.drawable.car_cui_select));
                        carUberBlack.setImageDrawable(ContextCompat.getDrawable(Home.this, R.drawable.car_vip));
                  /*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            carUberX.setImageDrawable(getDrawable(R.drawable.car_cui_select));
                            carUberBlack.setImageDrawable(getDrawable(R.drawable.car_vip));
                        }else {
                            carUberX.setImageDrawable(ContextCompat.getDrawable(Home.this,R.drawable.car_cui_select));
                            carUberBlack.setImageDrawable(ContextCompat.getDrawable(Home.this,R.drawable.car_vip));
                        }*/

                    } else {
                        carUberX.setImageDrawable(ContextCompat.getDrawable(Home.this, R.drawable.car_cui));
                        carUberBlack.setImageDrawable(ContextCompat.getDrawable(Home.this, R.drawable.car_vip_select));
                    }
                    mMap.clear();
                    if (driversAvailable != null)
                        driversAvailable.removeEventListener(Home.this);
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl).child(isUberX ? "UberX" : "Uber Black");
                    driversAvailable.addValueEventListener(Home.this);
                    if (Common.mLastLocation != null) {
                        loadAllDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                    }
                }
            });
        } else {
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCancelBroadCast);
        super.onDestroy();
    }

    private void updateFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        // Toast.makeText(Home.this, " updated success ", Toast.LENGTH_SHORT).show();
                        reference.child(Common.tokens_tb).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(task.getResult().getToken());
                    }
                    String id = task.getResult().getId();
                    //Log.d("user_id", id);
                    tokenStr = task.getResult().getToken();
                    //Log.d("user_token", tokenStr);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("error_in_token", e.getMessage());
            }
        });
    }

    private void sendRequestToDriver(final String mDriverId) {
        final DatabaseReference refrence = FirebaseDatabase.getInstance().getReference().child(Common.tokens_tb);
        refrence.orderByKey().equalTo(mDriverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("user_token2", dataSnapshot.getValue().toString());
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    //Token token=dataSnapshot1.getValue(Token.class);
                    // Token token = dataSnapshot1.getValue(Token.class);
                    String token = dataSnapshot1.getValue().toString();
                    Log.d("user_token2", token);
                    String lat_lng = new Gson().toJson(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
                    Log.d("gson_my", lat_lng);
                    /*Notification notification = new Notification(tokenStr, lat_lng);
                    Sender content = new Sender(notification, token_mine);*/
                    String riderToken = FirebaseInstanceId.getInstance().getToken();
                    Log.d("RiderToken",riderToken);
                    Map<String, String> data = new HashMap<>();
                    data.put("customer", riderToken);
                    data.put("lat", String.valueOf(Common.mLastLocation.getLatitude()));
                    data.put("lang", String.valueOf(Common.mLastLocation.getLongitude()));
                    DataMessage dataMessage = new DataMessage(token, data);

                    Log.d("RiderToken2",token);

                    if(!token.equals(riderToken)) {
                        ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
                            @Override
                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                if (response.body().success == 1)
                                    Toast.makeText(Home.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(Home.this, "Request Failed!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                Log.e("Error_in_request", t.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
       /* FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            if (mAuth.getCurrentUser() == null) {
                Intent intent = new Intent(Home.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }*/
    }

    private void requestPickup(String currentUserId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Common.pickup_request_tb);
        GeoFire geoFire = new GeoFire(reference);
        if (Common.mLastLocation != null) {
            geoFire.setLocation(currentUserId, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
            /*    submitProcessButton.setProgress(100);
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submitProcessButton.setProgress(0);
                    }
                }, 3000);
                mHandler.removeCallbacks(null);*/

                    if (mUserMarker.isVisible()) {
                        mUserMarker.remove();
                    }
                    //add new map
                    mUserMarker = mMap.addMarker(new MarkerOptions()
                            .title("Pickup Here")
                            .snippet("")
                            .position(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()))
                            // .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            //  .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))

                    );
                    mUserMarker.showInfoWindow();

                    //animation
                    // mMap is GoogleMap object, latLng is the location on map from which ripple should start
                   /* mapRipple = new MapRipple(mMap, new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), Home.this);
                    mapRipple.withNumberOfRipples(1);
                    mapRipple.withStrokeColor(Color.BLACK);
                    mapRipple.withStrokewidth(10);     // 10dp
                    mapRipple.withDistance(500);     // 2000 metres radius
                    mapRipple.withRippleDuration(1000);    //12000ms
                    mapRipple.withTransparency(0.5f);

                    mapRipple.startRippleMapAnimation();*/
                    findDrivers();
                }
            });
        }


    }

    private void findDrivers() {
        DatabaseReference reference;
        if (isUberX) {
            reference = FirebaseDatabase.getInstance().getReference().child(Common.driver_location_tbl).child("UberX");
        } else {
            reference = FirebaseDatabase.getInstance().getReference().child(Common.driver_location_tbl).child("Uber Black");
        }
        GeoFire geoFire = new GeoFire(reference);

        GeoQuery query = geoFire.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), raduis);
        query.removeAllListeners();
        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverFound) {
                    isDriverFound = true;
                    mDriverId = key;
                    submitProcessButton.setProgress(100);
                    //submitProcessButton.setText("Call Driver");
                    //Toast.makeText(Home.this, " " + key, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!isDriverFound && raduis < LIMIT) {
                    raduis++;
                    findDrivers();
                } else {
                    if (!isDriverFound) {
                        Toast.makeText(Home.this, "No driver near to you !", Toast.LENGTH_SHORT).show();
                        submitProcessButton.setText("Pickup Request");
                        submitProcessButton.setProgress(0);
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            /*if (grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission denied by user", Toast.LENGTH_SHORT).show();
            else */
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if (isGooglePlayServicesAvailable()) {
                    //buildGoogleApiClient();
                    createLocationRequest();
                    displayLocation();
                }
        }
    }

    private void setUpLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
            //  }
        } else {
            if (isGooglePlayServicesAvailable()) {
                //  buildGoogleApiClient();
                buildLocationCalback();
                createLocationRequest();
                // if(location_switch.isChecked()){
                displayLocation();
                //  }
            }
        }
    }

    public void buildLocationCalback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                displayLocation();
                Log.i("common_lastLocation", Common.mLastLocation.toString());
                Log.i("common_getLastLocation", locationResult.getLastLocation().toString());

            }

            ;
        };
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                Common.mLastLocation = location;
                if (Common.mLastLocation != null) {


                    //create new latlng object for distance
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());
                    //distance in meters
                    //heading 0 is  northSide,90 is east , 180 is south and 270 is west
                    //based on cmpact
                    LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
                    LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .build();

                    locationPlace.setBoundsBias(bounds);
                    locationPlace.setFilter(autocompleteFilter);

                    destinationPlace.setBoundsBias(bounds);
                    destinationPlace.setFilter(autocompleteFilter);


                    final double mLatitude = Common.mLastLocation.getLatitude();
                    final double mLongtude = Common.mLastLocation.getLongitude();
            /*if(FirebaseAuth.getInstance()!=null) {
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(mLatitude, mLongtude), new GeoFire.CompletionListener() {*/
                    //@Override
                    // public void onComplete(String key, DatabaseError error) {
/*            if (mUserMarker != null) {
                mUserMarker.remove();
            }
            mUserMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mLatitude, mLongtude)).title("You"));
            //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude,mLongtude),15.0f));
            LatLng latLng = new LatLng(mLatitude, mLongtude);
            Log.d("latlng", mLatitude + " " + mLongtude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mUserMarker.showInfoWindow();*/

                    // loadAllDrivers();

                    //init firebase database for presence system
                    driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_location_tbl).child(isUberX ? "UberX" : "Uber Black");
                    driversAvailable.addValueEventListener(Home.this);
                    //}
                }
            }
        });


        //   });
        //}
    }

    private void loadAllDrivers(final LatLng latLng) {
/*        //for presence system
        mMap.clear();
        //add marker to your location
        mMap.addMarker(new MarkerOptions().position(latLng).title("You"));*/

       /* if (mUserMarker != null) {
            mUserMarker.remove();
        }*/
        mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng).title("You")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))

        );
        //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLatitude,mLongtude),15.0f));
        // LatLng latLng = new LatLng(mLatitude, mLongtude);
        Log.d("latlng", latLng.toString());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));
        mUserMarker.showInfoWindow();


        DatabaseReference reference;
        if (isUberX) {
            reference = FirebaseDatabase.getInstance().getReference().child(Common.driver_location_tbl).child("UberX");
        } else {
            reference = FirebaseDatabase.getInstance().getReference().child(Common.driver_location_tbl).child("Uber Black");
        }
        GeoFire geoFire = new GeoFire(reference);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                FirebaseDatabase.getInstance().getReference().child(Common.user_driver_tbl).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            /*if (dataSnapshot.hasChild("name")) {
                                Toast.makeText(Home.this, "", Toast.LENGTH_SHORT).show();*/
                            if (isUberX) {
                                if(Common.currentRider.getVechleType()!=null)
                                if (Common.currentRider.getVechleType().equals("UberX")) {
                                    mMap.addMarker(new MarkerOptions().title(dataSnapshot.child("name").getValue().toString())
                                            .snippet("Driver ID: " + dataSnapshot.getKey())
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                            .flat(true)
                                    );
                                    // Toast.makeText(Home.this, "driver", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (Common.currentRider.getVechleType().equals("Uber Black")) {
                                    mMap.addMarker(new MarkerOptions().title(dataSnapshot.child("name").getValue().toString())
                                            .snippet("Driver ID: " + dataSnapshot.getKey())
                                            .position(new LatLng(location.latitude, location.longitude))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                            .flat(true)
                                    );
                                    //Toast.makeText(Home.this, "driver", Toast.LENGTH_SHORT).show();
                                }
                            }

                            //}
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (distance <= LIMIT) {
                    distance++;
                    loadAllDrivers(latLng);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLAY_CREMENT);
    }
/*

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
*/

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_update_info) {
            showChangeInformationDialog();
        } else if (id == R.id.nav_signout) {
            signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangeInformationDialog() {
        //creating aleart dialog builder object
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting cancelable to false
        builder.setCancelable(false);
        //set title
        builder.setTitle("CHANGE INFORMATION");
        //set message
        builder.setMessage("Please Enter All Required Fields !");
        /**
         * getting layoutinflater object
         *
         */
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //convert xml file into corespondings views objects
        final View view = inflater.inflate(R.layout.layout_update_information, null);
        //intialize old pass edit text
        final AppCompatEditText user_name = view.findViewById(R.id.name_edit_info);
        //intialize new pass edit text
        final AppCompatEditText user_emial = view.findViewById(R.id.email_edit_ifo);
        final AppCompatEditText user_phone = view.findViewById(R.id.phone_edit_ifo);
        final LinearLayout linearLayout = view.findViewById(R.id.linear_layout);
        final ProgressBar progressBar = view.findViewById(R.id.progress_circular);
        final ImageButton userImage = view.findViewById(R.id.add_user_image);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        //setting cancel button
        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        //getting information
        FirebaseDatabase mUserDatabase = FirebaseDatabase.getInstance();
        mUserDatabase.getReference(Common.user_rider_tbl).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Rider user = dataSnapshot.getValue(Rider.class);
                // String name = dataSnapshot.child("name").getValue().toString();
                if (user != null) {
                    progressBar.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);

                    user_name.setText(user.getName());
                    user_phone.setText(user.getPhone());
                    // user_name.setText(mUser.getDisplayName());
                    user_emial.setText(mUser.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //hide the dailog
                dialogInterface.dismiss();
            }
        });
        //seeting reset button
        builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //showing spots dialog
                dialog = new SpotsDialog(Home.this, "Please Wait...");
                dialog.show();
                //getting old pass form user
                String name = user_name.getText().toString();
                //getting old pass form user
                final String email = user_emial.getText().toString();
                final String phone = user_phone.getText().toString();
                //checking if the email is empty
                final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mUser != null) {
                    Map map = new HashMap();
                    map.put("name", name);
                    map.put("email", email);
                    map.put("phone", phone);
                    FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(mUser.getUid()).updateChildren(map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            dialog.dismiss();
                                            Toast.makeText(Home.this, " information updated successfully!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(Home.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            }
        });
        //set the custom view
        builder.setView(view);
        //show the dialog
        builder.show();
    }

    private void signOut() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d("out_sign_out", "in_sign_out");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                //mStorage.getReference().child("driver_images").child(mAuth.getCurrentUser().getUid()).
                Uri selecteImage = data.getData();
                if (selecteImage != null) {
                    dialog = new SpotsDialog(Home.this, "Please Wait...");
                    dialog.show();
                    uploadFile(selecteImage);
                }
            }
        }
    }

    private void uploadFile(Uri selectedImageUri) {
        //these three line of code only for to give the uploaded file name
        //File f = new File(String.valueOf(selectedImageUri.getLastPathSegment()));
        //String imageName = f.getName();
        String imageName = UUID.randomUUID().toString();
        final StorageReference reference = mReference.child(imageName);
       /* StorageReference mStorageRefrence = mStorage.getReference().child("driver_images");
        final StorageReference storageReference = mStorageRefrence.child(imageName);*/
        reference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(navigationViewLayout).load(uri).apply(new RequestOptions().override(150, 150)).into(riderImage);

                        Map map = new HashMap();
                        map.put("photo", uri.toString());
                        FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(mAuth.getCurrentUser().getUid()).updateChildren(map, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    //   Log.d("database_eror",databaseError.getMessage());
                                    dialog.dismiss();
                                    Toast.makeText(Home.this, " information updated successfully!", Toast.LENGTH_LONG).show();

                                } else {

                                    dialog.dismiss();
                                    Toast.makeText(Home.this, " uploading fialed !", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                dialog.setMessage("uploading " + (int) progress + "%");

            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(Home.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
       /* .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        })*/

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccessful = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_style_map));
            if (!isSuccessful) {
                Log.e("Error", "eroro");
            }
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerDestination != null) {
                    markerDestination.remove();
                }
                markerDestination = mMap.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                        .title("Destination")
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f));

                //show bottom sheet
                if (Common.mLastLocation != null) {
                    BottomSheetRiderFragment bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance(String.format("%f,%f", Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()), String.format("%f,%f", latLng.latitude, latLng.longitude), true);
                    bottomSheetRiderFragment.show(getSupportFragmentManager(), bottomSheetRiderFragment.getTag());
                }
            }
        });
        mMap.setOnInfoWindowClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }


    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onInfoWindowClick(Marker marker) {

        //if the marker your maker info :don't applay
        if (!marker.getTitle().equals("You")) {
            //call the (CallDriver)Activity
            Intent intent = new Intent(Home.this, CallDriver.class);
            //intent.putExtra("driverId",marker.getSnippet().replaceAll("\\D+",""));
            intent.putExtra("driverId", marker.getSnippet());

            intent.putExtra("lat", Common.mLastLocation.getLatitude());
            intent.putExtra("lng", Common.mLastLocation.getLongitude());
            startActivity(intent);

        }
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (Common.mLastLocation != null) {
            loadAllDrivers(new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
