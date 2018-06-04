package com.example.liza.au_2018_jvm2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.firebase.client.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : FragmentActivity(), OnMapReadyCallback, ChildEventListener, ValueEventListener {

    private val REQUEST_PLACE_PICKER = 1
    private val FIREBASE_URL = "https://au-2018-jvm2.firebaseio.com"
    private val FIREBASE_ROOT_NODE = "checkouts"

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mMap: GoogleMap? = null
    private var mFirebase: Firebase? = null
    private val mBounds = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Set up the API client for Places API
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build()
        mGoogleApiClient!!.connect()

        // Set up Google Maps
        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(data, this)

                val checkoutData = mutableMapOf<String, Any>()
                checkoutData.put("time", ServerValue.TIMESTAMP)

                mFirebase!!.child(FIREBASE_ROOT_NODE).child(place.getId()).setValue(checkoutData)
            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                Toast.makeText(this, "Places API failure! Check that the API is enabled for your key",
                        Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Map setup. This is called when the GoogleMap is available to manipulate.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Cannot display the map: permissions not granted", Toast.LENGTH_SHORT)
                    .show()
            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMyLocationChangeListener({location ->
            val ll = LatLng(location.getLatitude(), location.getLongitude())
            addPointToViewPort(ll)
            // we only want to grab the location once, to allow the user to pan and zoom freely.
            mMap!!.setOnMyLocationChangeListener(null)
        })

        // Pad the map controls to make room for the button - note that the button may not have
        // been laid out yet.
        checkout_button.viewTreeObserver.addOnGlobalLayoutListener({
                    mMap!!.setPadding(0, checkout_button.getHeight(), 0, 0) })

        // Set up Firebase
        Firebase.setAndroidContext(this)
        mFirebase = Firebase(FIREBASE_URL)
        mFirebase!!.child(FIREBASE_ROOT_NODE).addChildEventListener(this)
        mFirebase!!.child(FIREBASE_ROOT_NODE).addValueEventListener(this)
    }

    /**
     * Prompt the user to check out of their location. Called when the "Check Out!" button
     * is clicked.
     */
    fun checkOut(view: View) {
        try {
            val intentBuilder = PlacePicker.IntentBuilder()
            val intent = intentBuilder.build(this)
            startActivityForResult(intent, REQUEST_PLACE_PICKER)
        } catch (e: GooglePlayServicesRepairableException) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.connectionStatusCode,
                    REQUEST_PLACE_PICKER)
        } catch (e: GooglePlayServicesNotAvailableException) {
            Toast.makeText(this, "Please install Google Play Services!", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDataChange(dataSnapshot: DataSnapshot?) {
        dataSnapshot!!.children.forEach({child ->
            val placeId = child.getKey() ?: return
            Places.GeoDataApi.getPlaceById(mGoogleApiClient!!, placeId)
                    .setResultCallback({places ->
                        val location = places.get(0).latLng
                        addPointToViewPort(location)
                        mMap!!.addMarker(MarkerOptions().position(location));
                        places.release()
                    })

        })
    }

    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {
        placeOnMap(dataSnapshot, s)
    }

    override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
        placeOnMap(dataSnapshot, s)
    }

    override fun onChildRemoved(p0: DataSnapshot?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCancelled(p0: FirebaseError?) {
        Toast.makeText(this, p0?.message, Toast.LENGTH_LONG).show()
    }

    private fun addPointToViewPort(newPoint: LatLng) {
        mBounds.include(newPoint)
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(),
                checkout_button.getHeight()))
    }

    private fun placeOnMap(dataSnapshot: DataSnapshot?, s: String?) {
        val placeId = dataSnapshot!!.key ?: return
        Places.GeoDataApi.getPlaceById(mGoogleApiClient!!, placeId)
                .setResultCallback({places ->
                    val location = places.get(0).latLng
                    addPointToViewPort(location)
                    mMap!!.addMarker(MarkerOptions().position(location));
                    places.release()
                })
    }
}