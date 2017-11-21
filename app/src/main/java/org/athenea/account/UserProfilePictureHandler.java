package org.athenea.account;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import de.hdodenhof.circleimageview.CircleImageView;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zulaika on 20/11/17.
 */

public class UserProfilePictureHandler extends AsyncTask<Void, Void, Boolean> {

  private String email;
  private CircleImageView userProfilePicture;
  private Bitmap profilePicture;

  /**
   * Constructor for class, pass email as paramater
   * @param email
   */
  public UserProfilePictureHandler(String email, CircleImageView userProfilePicture) {

    super();
    this.email = email;
    this. userProfilePicture = userProfilePicture;

  }
  @Override
  protected Boolean doInBackground(Void... voids) {

    // Get the image based on user email (primary key)
    String requestUrl = "http://www.athenea-project.org/media/" + email;

    try {
      URL url = new URL(requestUrl);
      URLConnection conn = url.openConnection();
      profilePicture = BitmapFactory.decodeStream(conn.getInputStream());
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return profilePicture != null;
  }

  /**
   * When query executed
   *
   * @param succeed if user exists in DB
   */
  @Override
  protected void onPostExecute(Boolean succeed) {

    if(succeed) {
      userProfilePicture.setImageBitmap(profilePicture);
    }


  }

}
