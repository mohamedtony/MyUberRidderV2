package tony.dev.mohamed.myuberridder;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuberridder.FirebaseService.IFCMService;
import tony.dev.mohamed.myuberridder.helper.Common;
import tony.dev.mohamed.myuberridder.models.DataMessage;
import tony.dev.mohamed.myuberridder.models.FCMResponse;
import tony.dev.mohamed.myuberridder.models.Rider;

import static tony.dev.mohamed.myuberridder.helper.Common.mDriverId;
import static tony.dev.mohamed.myuberridder.helper.Common.mLastLocation;

public class CallDriver extends AppCompatActivity {
    //circle image view
    private CircleImageView driver_image;
    //text view for driver name
    private TextView driverName;
    //text view for driver phone
    private TextView driverPhone;
    //text view for driver rates
    private TextView driverRates;
    //service for sending a message
    private IFCMService ifcmService;
    //button for call driver by app
    private Button mCallDriverByApp;
    //button for call driver by phone
    private Button mCallDriverByPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        //init the service
        ifcmService= Common.getFcmService();
        //intit views
        driver_image=findViewById(R.id.driver_image);
        driverName=findViewById(R.id.driver_name);
        driverPhone=findViewById(R.id.driver_phone);
        driverRates=findViewById(R.id.driver_rate);
        mCallDriverByApp=findViewById(R.id.call_driver_by_app);
        mCallDriverByPhone=findViewById(R.id.call_driver_by_phone);
        //get the exras from the intent
        if(getIntent()!=null){
            String myDriverId=getIntent().getStringExtra("driverId").toString();
            mDriverId = myDriverId.substring(myDriverId.lastIndexOf(":") + 1).trim();
            double lat=getIntent().getDoubleExtra("lat",-1.0);
            double lng=getIntent().getDoubleExtra("lng",-1.0);

            mLastLocation=new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);
            loadDriverInfo(mDriverId);
        }

        mCallDriverByApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here call the function to send message to the driver
                sendRequestToDriver(mDriverId);
            }
        });
        mCallDriverByPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+driverPhone.getText().toString()));
                startActivity(intent);
            }
        });




    }

    private void loadDriverInfo(String mDrived) {
        Log.d("userDriver",mDrived);
        FirebaseDatabase.getInstance().getReference().child(Common.user_driver_tbl).child(mDrived)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Rider userDriver=dataSnapshot.getValue(Rider.class);
                if(userDriver!=null){
                    Log.d("userDriver",""+userDriver.getName()+" "+userDriver.getPhone()+" "+userDriver.getRates());
                    if(!TextUtils.isEmpty(userDriver.getPhoto())) {
                        Glide.with(CallDriver.this).load(userDriver.getPhoto()).into(driver_image);
                    }
                    driverName.setText(userDriver.getName());
                    driverPhone.setText(userDriver.getPhone());
                    driverRates.setText(userDriver.getRates());
                }else{
                    Log.d("userDriver","null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendRequestToDriver(String mDriverId) {
        final DatabaseReference refrence = FirebaseDatabase.getInstance().getReference().child(Common.tokens_tb);
        refrence.orderByKey().equalTo(mDriverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    //Token token=dataSnapshot1.getValue(Token.class);
                    // Token token = dataSnapshot1.getValue(Token.class);
                    String token=dataSnapshot1.getValue().toString();
                    String lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    /*Notification notification = new Notification(tokenStr, lat_lng);
                    Sender content = new Sender(notification, token_mine);*/
                    String riderToken= FirebaseInstanceId.getInstance().getToken();
                    Map<String,String> data=new HashMap<>();
                    data.put("customer",riderToken);
                    data.put("lat",String.valueOf(mLastLocation.getLatitude()));
                    data.put("lang",String.valueOf(mLastLocation.getLongitude()));
                    DataMessage dataMessage=new DataMessage(token,data);


                    ifcmService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1)
                                Toast.makeText(CallDriver.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(CallDriver.this, "Request Failed!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.e("Error_in_request", t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
