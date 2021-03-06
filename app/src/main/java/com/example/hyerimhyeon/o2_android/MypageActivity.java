package com.example.hyerimhyeon.o2_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DB.DBConnector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class MypageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ListView mypage_mypost, comment_lv, alarm_lv;
    FrameLayout container;
    ImageButton settingBtn, backBtn;
    Boolean buttonStateOpen;

    MypageMypostAdapterActivity mypageMypostAdapterActivity;
    MypageAlarmAdapterActivity mypageAlarmAdapterActivity;
    MypageCommentAdapterActivity mypageCommentAdapterActivity;
    NewsFeed newsFeed , newsFeed2;
    NewsFeed_Comment newsFeed3;
    ImageView profile_img;
    Handler handler;
    TextView nametv, typetv, companytv;
    String token, id;
    DrawerLayout drawer;
    private static final int REQUEST_INTERNET = 1;
    public SharedPreferences loginPreferences;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    ImageButton menu_icon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        ImageView logo = (ImageView) headerView.findViewById(R.id.nav_header_logo);

        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MypageActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });

         loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        id = loginPreferences.getString("id","");
        token = loginPreferences.getString("token","");
      //  Log.d("response" , "user id : " + id);


        nametv = (TextView) findViewById(R.id.mypage_name);
        typetv = (TextView) findViewById(R.id.mypage_type);
        companytv = (TextView) findViewById(R.id.mypage_company);
        profile_img = (ImageView) findViewById(R.id.mypage_profile_img);

     //   Log.d("response" , "myname: " + loginPreferences.getString("name" , ""));

        String name_encode = "";
        try {
            name_encode = URLDecoder.decode(loginPreferences.getString("name",""),"UTF-8");
            Log.d("name_encode : ","" + name_encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("name_encode e :","" + e.toString());
        }
        nametv.setText(name_encode);
        typetv.setText(loginPreferences.getString("type",""));
        if(loginPreferences.getString("company","")!=null){
            companytv.setText(loginPreferences.getString("company",""));
        }else{
            companytv.setText("");
        }

        if(loginPreferences.getString("profile_url","").equals("")|| loginPreferences.getString("profile_url","") == null|| loginPreferences.getString("profile_url","") == " "|| loginPreferences.getString("profile_url","") == "null"|| loginPreferences.getString("profile_url","") == "" || loginPreferences.getString("profile_url","")=="http://" || loginPreferences.getString("profile_url","")=="http://null" || loginPreferences.getString("profile_url","").equals("http://") || loginPreferences.getString("profile_url","").equals("http:/null/")){

        }else{
            int permissionCamera = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE);
            if(permissionCamera == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE}, REQUEST_INTERNET);
            } else {
                new DownLoadImageTask_profile(profile_img).execute("http://" + loginPreferences.getString("profile_url",""));

            }
            // Log.d("response2", "response img: " + uri);
            // image.setImageURI(uri);
        }
        token = loginPreferences.getString("token","");

        settingBtn = (ImageButton) findViewById(R.id.mypage_settingBtn);
        backBtn = (ImageButton) findViewById(R.id.mypage_backBtn);

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MypageActivity.this, MypageSettingActivity.class);
                startActivity(intent);
            }
        });

        TabHost host1 = (TabHost) findViewById(R.id.mypage_tabHost);
        host1.setup();

        TabHost.TabSpec spec = host1.newTabSpec("Tab One");
        spec.setContent(R.id.mypage_tab1);
        spec.setIndicator("내가 쓴 글");
        host1.addTab(spec);

        spec = host1.newTabSpec("Tab Two");
        spec.setContent(R.id.mypage_tab2);
        spec.setIndicator("알림");
        host1.addTab(spec);

        spec = host1.newTabSpec("Tab Three");
        spec.setContent(R.id.mypage_tab3);
        spec.setIndicator("내가 쓴 댓글");
        host1.addTab(spec);



        mypage_mypost = (ListView)findViewById(R.id.mypage_mypost);
        alarm_lv = (ListView)findViewById(R.id.mypage_alarm);
        comment_lv = (ListView)findViewById(R.id.mypage_comment);


        this.newsFeed = NewsFeed.getNewsFeed();
        this.mypageMypostAdapterActivity = new MypageMypostAdapterActivity(this, newsFeed, this);
        this.mypage_mypost.setAdapter(mypageMypostAdapterActivity);

        this.newsFeed2 = NewsFeed.getNewsFeed();
        this.mypageAlarmAdapterActivity = new MypageAlarmAdapterActivity(this, newsFeed2, this);
        this.alarm_lv.setAdapter(mypageAlarmAdapterActivity);

        this.newsFeed3 = NewsFeed_Comment.getNewsFeed();
        this.mypageCommentAdapterActivity = new MypageCommentAdapterActivity(this, newsFeed3, this);
        this.comment_lv.setAdapter(mypageCommentAdapterActivity);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // open or close when click a menu button
        buttonStateOpen = false;
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        menu_icon = (ImageButton) findViewById(R.id.mypage_backBtn);
        menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (buttonStateOpen == false) {
                    drawer.openDrawer(Gravity.LEFT);
                    buttonStateOpen = true;
                } else if (buttonStateOpen == true) {
                    drawer.closeDrawer(Gravity.LEFT);
                    buttonStateOpen = false;
                }

            }
        });


    }

    private class DownLoadImageTask_profile extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask_profile(ImageView imageView) {
            this.imageView = imageView;
        }

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected Bitmap doInBackground(String... urls) {
            //String urlOfImage = urls[0];
            String urlOfImage = urls[0];
            Log.d("response", "image url : " + urlOfImage);
            Bitmap logo = null;
            try {
                InputStream is = new URL(urlOfImage).openStream();
                /*
                    decodeStream(InputStream is)
                        Decode an input stream into a bitmap.
                 */
                logo = BitmapFactory.decodeStream(is);
            } catch (Exception e) { // Catch the download exception
                e.printStackTrace();
                Log.d("response", "image back : " + e.toString());
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result) {


            imageView.setImageBitmap(result);
        }
    }

    @Override
    public void onStart(){
        super.onStart();


        int permissionReadStorage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE);
        //  int permissionWriteStorage = ContextCompat.checkSelfPermission(mainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionReadStorage == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE}, REQUEST_EXTERNAL_STORAGE);
        } else {
            new GetMyPost().execute(new DBConnector());
            new GetMyCommentPost().execute(new DBConnector());
        }

    }


    private class GetMyPost extends AsyncTask<DBConnector, Long, JSONArray> {


        @Override
        protected JSONArray doInBackground(DBConnector... params) {

            //it is executed on Background thread

            return params[0].GetMyPost(token);

        }

        @Override
        protected void onPostExecute(final JSONArray jsonArray) {

            settextToAdapter(jsonArray);

            handler = new Handler();

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {

                        mypageMypostAdapterActivity.notifyDataSetChanged();

                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }, 100);


        }
    }

    public void settextToAdapter(JSONArray jsonArray) {

        //  Log.d("response" , "post : " + jsonArray.toString());

        // ArrayList<NewsfeedItem> newsfeedItems = newsFeed.newsfeedItem;
        NewsfeedItem newsfeedItem;


        if(jsonArray == null){
            Toast.makeText(getApplicationContext(), "뉴스피드가 없습니다.",
                    Toast.LENGTH_LONG).show();
        }else{

            mypageMypostAdapterActivity.clear();

            for(int i = 0 ; i<jsonArray.length(); i++){

                newsfeedItem = new NewsfeedItem();


                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject userJsonObj = jsonObject.getJSONObject("user");

                    newsfeedItem.name = userJsonObj.getString("name");
                    newsfeedItem.email = userJsonObj.getString("email");
                    newsfeedItem.profile_url = userJsonObj.getString("profile_url");

                    newsfeedItem.content = jsonObject.getString("content");
                    newsfeedItem.post_image_url = jsonObject.getString("post_image_url");
                    newsfeedItem.youtube_link = jsonObject.getString("youtube_link");
                    newsfeedItem.like_num = jsonObject.getString("like_num");
                    newsfeedItem.comment_num = jsonObject.getString("comment_num");
                    newsfeedItem.regist_date = jsonObject.getString("timestamp").substring(0,10);
                    newsfeedItem.is_like = jsonObject.getBoolean("is_like");
                    newsfeedItem.post_id = jsonObject.getString("id");
                    newsfeedItem.member_type = userJsonObj.getString("member_type");
                    newsfeedItem.company = userJsonObj.getString("company");
                    newsfeedItem.sport_type = userJsonObj.getString("sport_type");
                    newsfeedItem.mentor_type = userJsonObj.getString("mentor_type");
                    newsfeedItem.expert_type = userJsonObj.getString("expert_type");
                    newsfeedItem.member_id = userJsonObj.getString("id");
                    newsfeedItem.youtube_tite = jsonObject.getString("youtube_title");

                    if(newsfeedItem.youtube_link == null || newsfeedItem.youtube_link.equals("") ){
                        newsfeedItem.youtube_id = "";
                    }else{
                        String str = newsfeedItem.youtube_link;
                        String video_id = "";

                        if(str.toString().indexOf("youtube.com") != -1) {
                            if(str.indexOf("&") > 0){
                                video_id = str.substring(str.indexOf("=")+1 , str.indexOf("&"));
                            }else{
                                video_id = str.substring(str.indexOf("=")+1);
                            }
                        }else if(str.toString().indexOf("youtu.be") != -1 ){
                            //   Log.d("response", "youtube_str :  "+ str);
                            video_id = str.substring(17);
                            Log.d("response", "youtube_id :  "+ video_id);
                        }

                        newsfeedItem.youtube_id = video_id;

                    }


                    mypageMypostAdapterActivity.add(newsfeedItem);
                    mypageMypostAdapterActivity.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }




    private class GetMyCommentPost extends AsyncTask<DBConnector, Long, JSONArray> {


        @Override
        protected JSONArray doInBackground(DBConnector... params) {

            //it is executed on Background thread

            return params[0].GetMyCommentPost(token, id);

        }

        @Override
        protected void onPostExecute(final JSONArray jsonArray) {

            settextToAdapter_comment(jsonArray);

            handler = new Handler();

            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {

                        mypageCommentAdapterActivity.notifyDataSetChanged();

                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }, 100);


        }
    }

    public void settextToAdapter_comment(JSONArray jsonArray) {

          Log.d("response" , "comment post : " + jsonArray);

        // ArrayList<NewsfeedItem> newsfeedItems = newsFeed.newsfeedItem;
        NewsfeedItem_comment newsfeedItem;


        if(jsonArray == null){
//            Toast.makeText(getApplicationContext(), "뉴스피드가 없습니다.",
//                    Toast.LENGTH_LONG).show();
        }else{

            mypageCommentAdapterActivity.clear();

            for(int i = 0 ; i<jsonArray.length(); i++){

                newsfeedItem = new NewsfeedItem_comment();


                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    JSONObject userJsonObj = jsonObject.getJSONObject("user");

                    newsfeedItem.name = userJsonObj.getString("name");
                    newsfeedItem.email = userJsonObj.getString("email");
                    newsfeedItem.profile_url = userJsonObj.getString("profile_url");

                    newsfeedItem.content = jsonObject.getString("content");
                    newsfeedItem.post_image_url = jsonObject.getString("post_image_url");
                    newsfeedItem.youtube_link = jsonObject.getString("youtube_link");
                    newsfeedItem.like_num = jsonObject.getString("like_num");
                    newsfeedItem.comment_num = jsonObject.getString("comment_num");
                    newsfeedItem.regist_date = jsonObject.getString("timestamp").substring(0,10);
                    newsfeedItem.is_like = jsonObject.getBoolean("is_like");
                    newsfeedItem.post_id = jsonObject.getString("id");
                    newsfeedItem.member_type = userJsonObj.getString("member_type");
                    newsfeedItem.company = userJsonObj.getString("company");
                    newsfeedItem.sport_type = userJsonObj.getString("sport_type");
                    newsfeedItem.mentor_type = userJsonObj.getString("mentor_type");
                    newsfeedItem.expert_type = userJsonObj.getString("expert_type");
                    newsfeedItem.member_id = userJsonObj.getString("id");
                    newsfeedItem.youtube_tite = jsonObject.getString("youtube_title");

                    if(newsfeedItem.youtube_link == null || newsfeedItem.youtube_link.equals("") ){
                        newsfeedItem.youtube_id = "";
                    }else{
                        String str = newsfeedItem.youtube_link;
                        String video_id = "";

                        if(str.toString().indexOf("youtube.com") != -1) {
                            if(str.indexOf("&") > 0){
                                video_id = str.substring(str.indexOf("=")+1 , str.indexOf("&"));
                            }else{
                                video_id = str.substring(str.indexOf("=")+1);
                            }
                        }else if(str.toString().indexOf("youtu.be") != -1 ){
                            //   Log.d("response", "youtube_str :  "+ str);
                            video_id = str.substring(17);
                            Log.d("response", "youtube_id :  "+ video_id);
                        }

                        newsfeedItem.youtube_id = video_id;

                    }


                    mypageCommentAdapterActivity.add(newsfeedItem);
                    mypageCommentAdapterActivity.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }



        }

    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_gallery) {

            Intent intent = new Intent(this, SubjectFeedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_slideshow) {

            Intent intent = new Intent(this, ExpertFeedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_manage) {

            Intent intent = new Intent(this, InviteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_share) {

            Intent intent = new Intent(this, NoticeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_send) {

            Intent intent = new Intent(this, MypageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (id == R.id.people) {

            Intent intent = new Intent(this, MemberActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else if (id == R.id.nav_lifesport) {

            Intent intent = new Intent(this, LifeExpertFeedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case REQUEST_EXTERNAL_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(android.Manifest.permission.ACCESS_NETWORK_STATE)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.d("response", "read/write storage  permission authorized2");

                        } else {
                            Log.d("response", "read/write storage  permission denied2");

                        }
                        break;
                    }
                }
                break;
        }
    }

}
