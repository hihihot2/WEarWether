package app.com.wearwether;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    LocationManager mgr;
    Location loc;

    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        String gpsSettings = android.provider.Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        //gps 켜기
        if(gpsSettings.indexOf("gps", 0) < 0){ //GPS Off 상태
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setTitle("GPS 연결");
            ab.setMessage("GPS가 꺼져있습니다 GPS를 켜시겠습니까?");
            ab.setPositiveButton("켜기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            ab.setNegativeButton("취소", null);
            ab.show();
        }
    }

    private void getWether(){
        String location = null;

        //현재 위치정보 확인
        //loc = gl.getLoc();
        //지역별 날씨정보 가져오기 (GPS 사용)
        tv.setText(location);
    }
}
