package com.example.liza.au_2018_jvm2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.client.Firebase
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_all_museums.*

class AllMuseumsActivity : AppCompatActivity() {
    private val FIREBASE_ROOT_NODE = "descriptions2"

    private var mMuseumsReference: DatabaseReference? = null
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<Museum, AllMuseumsViewHolder>? = null
    private var mOptions: FirebaseRecyclerOptions<Museum>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_all_museums)

        mMuseumsReference = FirebaseDatabase.getInstance().getReference(FIREBASE_ROOT_NODE);

        val query = mMuseumsReference!!
        mOptions = FirebaseRecyclerOptions.Builder<Museum>()
                .setQuery(query, Museum::class.java)
                .build() // maybe  need customizable parser
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<Museum, AllMuseumsViewHolder>(mOptions!!) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllMuseumsViewHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.all_museums_view_holder, parent, false)
                return AllMuseumsViewHolder(view)
            }

            override fun onBindViewHolder(holder: AllMuseumsViewHolder, position: Int, model: Museum) {
                holder.bindMuseum(model)
            }
        }
        all_museums!!.setHasFixedSize(true)
        all_museums!!.layoutManager = LinearLayoutManager(this)
        all_museums!!.adapter = mFirebaseAdapter
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAdapter!!.stopListening()
    }
}
