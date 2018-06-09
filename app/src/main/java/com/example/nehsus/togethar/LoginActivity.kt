package com.example.nehsus.togethar
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.hypertrack.lib.HyperTrack
import com.hypertrack.lib.callbacks.HyperTrackCallback
import com.hypertrack.lib.models.ErrorResponse
import com.hypertrack.lib.models.SuccessResponse
import com.hypertrack.lib.models.UserParams
import android.provider.Settings
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.gson.GsonBuilder;
import com.hypertrack.lib.internal.common.util.HTTextUtils;
import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.ActionParamsBuilder;
import com.hypertrack.lib.models.Place;
import com.hypertrack.lib.models.User;

class LoginActivity : BaseActivity() {
    private var nameText: EditText? = null
    private var phoneNumberText: EditText? = null
    private var uniqueIdText: EditText? = null
    private var loginBtnLoader: LinearLayout? = null

    private val user: User?
        get() {
            val sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY,
                    Context.MODE_PRIVATE)
            val jsonString = sharedPreferences.getString("user", "null")
            if (HTTextUtils.isEmpty(jsonString)) {
                return null
            }
            var user: User? = null
            try {

                user = GsonBuilder().create().fromJson<User>(jsonString, User::class.java!!)
            } catch (e: Exception) {
                return null
            }

            return user
        }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if user is logged in
        if (user != null) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
            return
        }

        // Initialize Toolbar
        initToolbar(getString(R.string.login_activity_title))

        // Initialize UI Views
        initUIViews()
    }

    /**
     * Call this method to initialize UI views and handle listeners for these
     * views
     */
    private fun initUIViews() {
        // Initialize UserName Views
        nameText = findViewById<EditText>(R.id.login_name) as EditText

        // Initialize Password Views
        phoneNumberText = findViewById<EditText>(R.id.login_phone_number) as EditText

        //Initialize uniqueIdText
        uniqueIdText = findViewById<EditText>(R.id.login_unique_id) as EditText
        val UUID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        uniqueIdText!!.setText(UUID)

        // Initialize Login Btn Loader
        loginBtnLoader = findViewById<EditText>(R.id.login_btn_loader) as LinearLayout
    }

    /**
     * Call this method when User Login button has been clicked.
     * Note that this method is linked with the layout file (content_login.xml)
     * using this button's layout's onClick attribute. So no need to invoke this
     * method or handle login button's click listener explicitly.
     *
     * @param view
     */
    fun onLoginButtonClick(view: View) {
        // Check if Location Settings are enabled, if yes then attempt
        // DriverLogin
        checkForLocationSettings()
    }

    /**
     * Call this method to check Location Settings before proceeding for User
     * Login
     */
    private fun checkForLocationSettings() {
        // Check for Location permission
        // Refer here for more detail
        // https://docs.hypertrack.com/sdks/android/reference/hypertrack.html#boolean-checklocationpermission
        if (!HyperTrack.checkLocationPermission(this)) {
            HyperTrack.requestPermissions(this)
            return
        }

        // Check for Location settings
        // Refer here for more detail
        // https://docs.hypertrack.com/sdks/android/reference/hypertrack.html#boolean-checklocationservices
        if (!HyperTrack.checkLocationServices(this)) {
            HyperTrack.requestLocationServices(this)
        }

        // Location Permissions and Settings have been enabled
        // Proceed with your app logic here i.e User Login in this case
        attemptUserLogin()
    }

    /**
     * Call this method to attempt user login. This method will create a User
     * on HyperTrack Server and configure the SDK using this generated UserId.
     */
    private fun attemptUserLogin() {

        // Show Login Button loader
        loginBtnLoader!!.visibility = View.VISIBLE

        // Get User details, if specified
        val name = nameText!!.text.toString()
        val phoneNumber = phoneNumberText!!.text.toString()
        val uniqueId = if (!HTTextUtils.isEmpty(uniqueIdText!!.text.toString()))
            uniqueIdText!!.text.toString()
        else
            phoneNumber

        val userParams = UserParams().setName(name).setPhone(phoneNumber).setUniqueId(uniqueId)
        /**
         * Get or Create a User for given uniqueId on HyperTrack Server here to
         * login your user & configure HyperTrack SDK with this generated
         * HyperTrack UserId.
         * OR
         * Implement your API call for User Login and get back a HyperTrack
         * UserId from your API Server to be configured in the HyperTrack SDK.
         *
         * Refer here for more detail
         * https://docs.hypertrack.com/sdks/android/reference/user.html#getorcreate-user
         */
        HyperTrack.getOrCreateUser(userParams, object : HyperTrackCallback() {
            override fun onSuccess(successResponse: SuccessResponse) {
                // Hide Login Button loader
                loginBtnLoader!!.visibility = View.GONE

                val user = successResponse.responseObject as User
                saveUser(user)
                val userId = user.getId()
                // Handle createUser success here, if required
                // HyperTrack SDK auto-configures UserId on createUser API call,
                // so no need to call HyperTrack.setUserId() API

                // On UserLogin success
                onUserLoginSuccess()
            }

            override fun onError(errorResponse: ErrorResponse) {
                // Hide Login Button loader
                loginBtnLoader!!.visibility = View.GONE

                Toast.makeText(this@LoginActivity, R.string.login_error_msg + " " +
                        errorResponse.errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Call this method when user has successfully logged in
     */
    private fun onUserLoginSuccess() {

        //Refer here for more detail
        // https://docs.hypertrack.com/sdks/android/reference/hypertrack.html#void-starttracking
        val actionParamsBuilder = ActionParamsBuilder()
        actionParamsBuilder.setType(Action.TYPE_VISIT)
        actionParamsBuilder.setExpectedPlace(Place().setAddress("HyperTrack").setCountry("India"))
        HyperTrack.createAction(actionParamsBuilder.build(), object : HyperTrackCallback() {
            override fun onSuccess(response: SuccessResponse) {
                val action = response.responseObject as Action
                saveAction(action)
                val mainActivityIntent = Intent(this@LoginActivity,
                        MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(mainActivityIntent)
                finish()
                Log.d(TAG, "onSuccess:  Action Created")
            }

            override fun onError(errorResponse: ErrorResponse) {
                Log.e(TAG, "onError:  Action Creation Failed: " + errorResponse.errorMessage)
            }
        })
    }

    /**
     * Handle on Grant Location Permissions request accepted/denied result
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings()

            } else {
                // Handle Location Permission denied error
                Toast.makeText(this, "Location Permission denied.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handle on Enable Location Services request accepted/denied result
     *
     * @param requestCode
     * @param resultCode
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HyperTrack.REQUEST_CODE_LOCATION_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if Location Settings are enabled to proceed
                checkForLocationSettings()

            } else {
                // Handle Enable Location Services request denied error
                Toast.makeText(this, R.string.enable_location_settings,
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUser(user: User) {
        val sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY,
                Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user", GsonBuilder().create().toJson(user))
        editor.apply()
    }

    private fun saveAction(action: Action) {
        val sharedPreferences = getSharedPreferences(HT_QUICK_START_SHARED_PREFS_KEY,
                Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("action_id", action.getId())
        editor.apply()
    }

    companion object {

        private val TAG = LoginActivity::class.java.simpleName
        val HT_QUICK_START_SHARED_PREFS_KEY = "com.hypertrack.quickstart:SharedPreference"
    }
}