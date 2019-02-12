package tony.dev.mohamed.myuberridder.FirebaseService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCMClient {
    //to prevent creating new instance from FCMClient
    private FCMClient(){
    };
    //object for retrofit client and intializing it to null at first
    private static Retrofit retrofitClient=null;

    /**
     * a method for getiing a retrofit client for networking
     *
     * @param baseUrl a string url to making a connection to api
     * @return retrofitClient object
     */
    public static Retrofit getClient(String baseUrl){
        if(retrofitClient==null){
            retrofitClient=new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitClient;
    }
}
