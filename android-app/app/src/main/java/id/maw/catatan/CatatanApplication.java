package id.maw.catatan;

import android.app.Application;
import id.maw.catatan.data.CatatanRepository;

public class CatatanApplication extends Application {
    private static CatatanApplication sInstance;
    private CatatanRepository mRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        mRepository = new CatatanRepository(this);
    }

    public static CatatanApplication getInstance() {
        return sInstance;
    }

    public CatatanRepository getRepository() {
        return mRepository;
    }
}
