package com.willkamp.rxar

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    textView.text = "main activity"
  }
}

@SuppressLint("SetTextI18n")
class OtherActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    textView.text = "other activity"
  }
}


