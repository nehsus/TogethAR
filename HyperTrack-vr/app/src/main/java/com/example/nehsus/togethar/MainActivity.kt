package com.example.nehsus.togethar

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hypertrack.lib.HyperTrack
import com.hypertrack.lib.HyperTrack.getOrCreateUser
import com.hypertrack.lib.callbacks.HyperTrackCallback
import com.hypertrack.lib.models.ErrorResponse
import com.hypertrack.lib.models.SuccessResponse
import com.hypertrack.lib.models.UserParams

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HyperTrack.requestPermissions(this)
        HyperTrack.requestLocationServices(this)

        var userParams = UserParams()

        userParams.phone = "9538014699"
        userParams.name = "Sushen Kumar"

        getOrCreateUser(userParams, object: HyperTrackCallback(){
            override fun onSuccess(response: SuccessResponse) {

                HyperTrack.startMockTracking(object : HyperTrackCallback(){
                    override fun onSuccess(response: SuccessResponse) {
                        Log.v("hypertrack", response.responseObject.toString())
                    }

                    override fun onError(errorResponse: ErrorResponse) {
                        Log.e("hypertrack", errorResponse.errorMessage)
                    }

                })
            }

            override fun onError(errorResponse: ErrorResponse) {

            }

        })
    }
}