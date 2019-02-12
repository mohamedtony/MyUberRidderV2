package tony.dev.mohamed.myuberridder;

import android.app.Application;
import android.support.v7.widget.AppCompatButton;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MyUberRidder extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //for custom font to the whole app
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
