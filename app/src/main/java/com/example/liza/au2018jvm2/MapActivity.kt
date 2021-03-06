package com.example.liza.au2018jvm2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import com.firebase.client.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast


class MapActivity : FragmentActivity(), OnMapReadyCallback, ChildEventListener, ValueEventListener {

    companion object {
        private const val REQUEST_PLACE_PICKER = 1
        private const val FIREBASE_URL = "https://au-2018-jvm2.firebaseio.com"
        private const val FIREBASE_ROOT_NODE = "museums"
    }

    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mMap: GoogleMap
    private lateinit var mFirebase: Firebase
    private val mBounds = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Set up the API client for Places API
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build()
        mGoogleApiClient.connect()

        // Set up Google Maps
        val mapFragment = map as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(data, this)

                val checkoutData = mutableMapOf<String, Any>()
                checkoutData["time"] = ServerValue.TIMESTAMP

                mFirebase.child(FIREBASE_ROOT_NODE).child(place.id).setValue(checkoutData)
            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                longToast("Places API failure! Check that the API is enabled for your key")
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
            toast("Cannot display the map: permissions not granted")
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationChangeListener { location ->
            val ll = LatLng(location.latitude, location.longitude)
            addPointToViewPort(ll)
            // we only want to grab the location once, to allow the user to pan and zoom freely.
            mMap.setOnMyLocationChangeListener(null)
        }

        // Set up Firebase
        Firebase.setAndroidContext(this)
        mFirebase = Firebase(FIREBASE_URL)
        mFirebase.child(FIREBASE_ROOT_NODE).addChildEventListener(this)
        mFirebase.child(FIREBASE_ROOT_NODE).addValueEventListener(this)
    }

    /*
     * For the sake of ValueEventListener
     */
    override fun onDataChange(dataSnapshot: DataSnapshot?) {
        dataSnapshot!!.children.forEach { child -> placeOnMap(child)}
    }

    /*
     * For the sake of ChildEventListener
     */
    override fun onChildMoved(dataSnapshot: DataSnapshot?, s: String?) {}

    override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {
        placeOnMap(dataSnapshot)
    }

    override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
        placeOnMap(dataSnapshot)
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot?) {}

    override fun onCancelled(firebaseError: FirebaseError?) {
        firebaseError?.message?.let { longToast(it) }
    }

    private fun addPointToViewPort(newPoint: LatLng) {
        mBounds.include(newPoint)
    }

    private fun placeOnMap(dataSnapshot: DataSnapshot?) {
        val name = dataSnapshot!!.key ?: return
        val placeId = dataSnapshot.getValue<String>(String::class.java)
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback { places ->
                    val location = places.get(0).latLng
                    addPointToViewPort(location)
                    mMap.addMarker(MarkerOptions().position(location).title(name))
                    places.release()
                }
    }
}