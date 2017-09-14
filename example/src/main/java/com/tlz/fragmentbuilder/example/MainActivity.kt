package com.tlz.fragmentbuilder.example

import android.os.Bundle
import com.tlz.fragmentbuilder.FbCompatActivity

class MainActivity : FbCompatActivity() {

    override fun frameLayoutId(): Int {
        return R.id.main_content
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fbFragmentManager.switch(TestFragment1::class.java)
    }
}
