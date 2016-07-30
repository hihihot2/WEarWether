package app.com.wearwether;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    LocationManager mgr;
    Location loc;

    TextView tv;
    Button btn;

    String[] dayArr = new String[]{"오늘","내일","모레"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.wether_tv);
        btn = (Button) findViewById(R.id.wether_btn);
        btn.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        if(v.getId() == btn.getId()){
            new getWetherData().execute( "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=4119079000");
        }
    }

    private class getWetherData extends AsyncTask<String, Void, String> {
        URL url = null;

        @Override
        protected String doInBackground(String... urls) {
            try {
                url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection == null)
                    return null;

                urlConnection.setConnectTimeout(10000); // 최대 10초가 연결 대기
                urlConnection.setUseCaches(false); //캐쉬 사용안함 > 서버에서 매번 읽어오기
                StringBuilder sb = new StringBuilder(); // 문제열 결합체

                if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    InputStream is = urlConnection.getInputStream();

                    //XmlPullParser 사용
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);

                    XmlPullParser xpp = factory.newPullParser(); //파서 객체 생성
                    xpp.setInput(is, "utf-8");
                    int eventType = xpp.getEventType();

                    boolean houtTag = false; //hour 태그 처리 여부
                    List<String> flist = new ArrayList<String>();
                    flist.add("day");
                    flist.add("hour");
                    flist.add("temp");
                    flist.add("wfKor");
                    int fIndex = -1;

                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if(eventType == XmlPullParser.START_TAG){
                            fIndex = flist.indexOf(xpp.getName());
                        }else if(eventType == XmlPullParser.TEXT){
                            String text = xpp.getText();
                            switch (fIndex){
                                case 0: // <day>tag
                                    int dayIndex = Integer.parseInt(text);
                                    sb.append(dayArr[dayIndex]);
                                    break;
                                case 1: // <hour> tag
                                    int hour = Integer.parseInt(text);
                                    sb.append(""+(hour-1)+"시~"+hour+"시");
                                    break;
                                case 2: // <temp> tag
                                    sb.append(""+text+"도");
                                    break;
                                case 3: // <wfKor> tag
                                    sb.append(""+text+"\n");
                                    break;
                            }
                            fIndex = -1;
                            houtTag = false;
                        }
                        eventType = xpp.next();
                    }
                    is.close();
                }
                return sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            tv.setText(s);
        }
    }
}
