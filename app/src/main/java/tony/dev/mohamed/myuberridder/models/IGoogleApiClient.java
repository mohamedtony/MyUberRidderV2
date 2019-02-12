package tony.dev.mohamed.myuberridder.models;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleApiClient {

    @GET
    Call<String> getPath(@Url String url);
}
