package org.athenea;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.File;
import org.athenea.account.UserProfilePictureHandler;
import org.athenea.login.LoginActivityMaterial;
import org.athenea.map.MapsActivity;
import org.athenea.register.SignupActivityMaterial;
import org.athenea.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  private CircleImageView userProfilePicture;

  // To upload files
  public static final int PICK_IMAGE = 1;   // image picker
  private Uri fileUri;
  String picturePath;
  Uri selectedImage;
  Bitmap photo;
  String ba1;
  public static String URL = "Paste your URL here";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    View drawerHeader = navigationView.getHeaderView(0);

    // Get user Views in order to give value to them

    userProfilePicture = drawerHeader.findViewById(R.id.userIcon);
    TextView userName = drawerHeader.findViewById(R.id.username);
    TextView userEmail = drawerHeader.findViewById(R.id.userEmail);



    // Extract user data from previous call
    Bundle userBundle = getIntent().getExtras();
    Log.d("BUNDLE", userBundle.toString());

    // if there is user data then assign it to views
    // TODO: what if there is no user data?
    if (userBundle.getString("username") != null && userBundle.getString("useremail") != null) {

      userName.setText(userBundle.getString("username"));
      userEmail.setText(userBundle.getString("useremail"));

    }

    // If user picture doesn't exist download from server
    File file = new File("drawable/" + userEmail);
    if(!file.exists()){
      Log.d("IMAGE", "GETTING PROFILE PICTURE");
      UserProfilePictureHandler userProfilePictureHandler = new UserProfilePictureHandler(userBundle.getString("useremail"), userProfilePicture);
      userProfilePictureHandler.execute();
    }

    // Add listener to profile picture
    userProfilePicture.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        // Open file chooser https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
      }
    });

    navigationView.getMenu().getItem(0).setChecked(true); // Set Courses menu item active
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    // Associate searchable configuration with the SearchView
    SearchManager searchManager =
        (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    SearchView searchView =
        (SearchView) menu.findItem(R.id.search).getActionView();
    searchView.setSearchableInfo(
        searchManager.getSearchableInfo(getComponentName()));
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    /** if (id == R.id.action_settings) {
      return true;
    } */

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_account) {

    } else if (id == R.id.nav_courses) {

    //} else if (id == R.id.nav_course_collections) {

    } else if (id == R.id.nav_map) {

      Intent intent = new Intent(this, MapsActivity.class);
      startActivity(intent);

    //} else if (id == R.id.nav_settings) {

      //} else if (id == R.id.nav_share) {

      //} else if (id == R.id.nav_help) {

    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // If activity result is pick image, then set it as profile picture and upload
    if (requestCode == PICK_IMAGE && resultCode == this.RESULT_OK) {
      selectedImage = data.getData();
      photo = (Bitmap) data.getExtras().get("data");

      // Cursor to get image uri to display

      String[] filePathColumn = {MediaStore.Images.Media.DATA};
      Cursor cursor = getContentResolver().query(selectedImage,
          filePathColumn, null, null, null);
      cursor.moveToFirst();

      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      picturePath = cursor.getString(columnIndex);
      cursor.close();

      Log.d("USER PICTURE", "CHANGING USER PICTURE");
      Log.d("USER PICTURE", picturePath);
      //userProfilePicture.setImageBitmap(photo);
    }
  }

  // TODO: Separate from this class
  public class uploadToServer extends AsyncTask<Void, Void, Boolean> {

    private ProgressDialog pd = new ProgressDialog(MainActivity.this);
    protected void onPreExecute() {
      super.onPreExecute();
      pd.setMessage("Wait image uploading!");
      pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      return true;
    }

    protected void onPostExecute(Boolean result) {
      super.onPostExecute(result);
      pd.hide();
      pd.dismiss();
    }
  }
}
