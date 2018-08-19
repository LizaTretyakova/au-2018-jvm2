package com.example.liza.au2018jvm2

data class Museum(var name: String,
                  var description: String,
                  var url: String) {
    // The empty constructor is needed
    // for the FirebaseRecyclerAdapter
    constructor(): this("", "", "")
}