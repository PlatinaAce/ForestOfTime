package com.platinaace.forestoftime;

import android.app.Application;
import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("QhsuNJHFf15d3TBBRNrd7Hfk9nEg9He73P0slgqU")       // from Back4App
                .clientKey("pMC3PzI4Qru69Lwg4OOiJyTUL3BAzWHYR9sHFqbz")       // from Back4App
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}


//8zhtT1Ehpty5MM3oan8bcOsjc74jBhZ9tsZoh4r3
//BMsZb7ahr4BR3oQMgSP3VpvQCfQeCEmwxvFZLISi