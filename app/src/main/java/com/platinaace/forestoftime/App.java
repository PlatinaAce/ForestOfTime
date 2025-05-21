package com.platinaace.forestoftime;

import android.app.Application;
import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("8zhtT1Ehpty5MM3oan8bcOsjc74jBhZ9tsZoh4r3")       // from Back4App
                .clientKey("BMsZb7ahr4BR3oQMgSP3VpvQCfQeCEmwxvFZLISi")       // from Back4App
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}