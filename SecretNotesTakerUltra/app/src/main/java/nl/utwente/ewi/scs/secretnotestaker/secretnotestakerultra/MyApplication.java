package nl.utwente.ewi.scs.secretnotestaker.secretnotestakerultra;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}
