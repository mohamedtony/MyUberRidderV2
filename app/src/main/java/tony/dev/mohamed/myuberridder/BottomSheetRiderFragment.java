package tony.dev.mohamed.myuberridder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tony.dev.mohamed.myuberridder.helper.Common;
import tony.dev.mohamed.myuberridder.models.IGoogleApiClient;

public class BottomSheetRiderFragment extends BottomSheetDialogFragment {
    private String  mLocation, mDestination;
    private IGoogleApiClient mService;
    private TextView price,location,destination;
    private boolean isTapOnmap;


    public static BottomSheetRiderFragment newInstance(String  mLocation,String mDestination,boolean isTapOnmap) {
        BottomSheetRiderFragment fragment = new BottomSheetRiderFragment();
        Bundle bundle = new Bundle();
        bundle.putString("mLocation", mLocation);
        bundle.putString("mDestination", mDestination);
        bundle.putBoolean("isTape",isTapOnmap);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("mLocation");
        mDestination = getArguments().getString("mDestination");
        isTapOnmap=getArguments().getBoolean("isTape");


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
         location=view.findViewById(R.id.txtLocation);
         destination=view.findViewById(R.id.destination);
         price=view.findViewById(R.id.price);

        mService=Common.getGoogleApi();
        getPrice(mLocation,mDestination);

        if(isTapOnmap) {
            location.setText(mLocation);
            destination.setText(mDestination);
        }
        return view;
    }

    private void getPrice(String mLocation, String mDestination) {
        String requestApi = null;

        //request to direction of the drivers and show address - calculate time and distance
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" + "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + mLocation +"&" +
                    "destination=" +mDestination+ "&" +
                    "key=" + getString(R.string.google_direction_api);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject jsonObject1 = routes.getJSONObject(0);
                        JSONArray legs = jsonObject1.getJSONArray("legs");
                        JSONObject jsonObject2 = legs.getJSONObject(0);
                        //get distance
                        JSONObject distance = jsonObject2.getJSONObject("distance");
                        String distance_text=distance.getString("text");
                        //use regex to extract double from string
                        //this regex will remove all text isn't digit
                        Double distance_value=Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]",""));
                        //get duration
                        JSONObject duration = jsonObject2.getJSONObject("duration");
                        String time_text=duration.getString("text");
                        Integer time_value=Integer.parseInt(time_text.replaceAll("\\D+",""));
                        //get address
                        String textAddress=jsonObject2.getString("end_address");

                        String final_calculation=String.format("%s + %s = $%.2f",distance_text,time_text,
                                Common.getPrice(distance_value,time_value)
                        );

                        price.setText(final_calculation);
                        if(isTapOnmap){
                            String start_address=jsonObject2.getString("start_address");
                            String end_address=jsonObject2.getString("end_address");

                            location.setText(start_address);
                            destination.setText(end_address);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(getContext(), " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("eroro",t.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("error_incustomer", e.getMessage());
            Toast.makeText(getContext(), " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
