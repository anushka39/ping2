package com.example.ping.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.ping.R
import com.example.ping.fragmentc.ChatsFragment
import com.example.ping.fragmentc.FailureCallBack
import com.example.ping.fragmentc.StatusFragment
import com.example.ping.fragmentc.StatusUpdateFragment
import com.example.ping.util.DATA_USERS
import com.example.ping.util.DATA_USER_PHONE
import com.example.ping.util.PERMISSION_REQUEST_READ_CONTACTS
import com.example.ping.util.REQUEST_NEW_CHAT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.fragment_main.view.*

class MainScreenActivity : AppCompatActivity(), FailureCallBack {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mSectionsPagerAdapter: Sectionpageradapter? = null
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val chatsFragment = ChatsFragment()
    private val statusUpdateFragment = StatusUpdateFragment()
    private val statusFragment = StatusFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        chatsFragment.setFailureCallbackListener(this)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = Sectionpageradapter(supportFragmentManager)

        containerc.adapter = mSectionsPagerAdapter
        containerc.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabsc))
        tabsc.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(containerc))
        resizeTabs()
        tabsc.getTabAt(1)?.select()
        tabsc.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        fabc.hide()
                    }
                    1 -> {
                        fabc.show()
                    }
                    2 -> {
                        fabc.hide()
                    }
                }
            }
        })
    }

    override fun onUserError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    fun resizeTabs() {
        val layout = (tabsc.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    fun onNewChat(v: View) {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(this)
                    .setTitle(" Contacts Permission")
                    .setMessage( "This app requires access to your contacts")
                    .setPositiveButton("Ask me") {dialog, which -> requestContactsPermission() }
                    .setNegativeButton("No") {dialog, which ->  }
                    .show()
            } else {
                requestContactsPermission()
            }
        }else {
            startNewActivity()
        }

    }

    fun requestContactsPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_READ_CONTACTS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_REQUEST_READ_CONTACTS -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startNewActivity()
                }
            }
        }
    }

    fun startNewActivity(){
        startActivityForResult(ContactsActivity.newIntent(this), REQUEST_NEW_CHAT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when (requestCode) {
                REQUEST_NEW_CHAT -> {
                    val name = data?.getStringExtra(PARAM_USER_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }
    private fun checkNewChatUser(name: String, phone: String){
        if(!name.isNullOrEmpty() && !phone.isNullOrEmpty()){
            firebaseDB.collection(DATA_USERS)
                .whereEqualTo(DATA_USER_PHONE, phone)
                .get()
                .addOnSuccessListener {result ->
                    if(result.documents.size > 0){
                        chatsFragment.newChat(result.documents[0].id)
                    }else {
                        AlertDialog.Builder(this)
                            .setTitle("User not found")
                            .setMessage("$name does not have an account. Send them an sms to install P!ng.")
                            .setPositiveButton("ok") { dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra("sms_body", "Hi! I'm using P!ng. You should install it so we chat here.")
                                startActivity(intent)
                            }
                            .setNegativeButton("cancel", null).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error occurred. Try again later", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class Sectionpageradapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0-> statusUpdateFragment
                1-> chatsFragment
                2-> statusFragment
                else -> statusFragment

            }
        }

        override fun getCount(): Int {
            return 3
        }

    }





    //inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {}

        companion object {

            val PARAM_USER_NAME = "UserName"
            val PARAM_PHONE = " Number"

        fun newIntent(context: Context) = Intent(context, MainScreenActivity::class.java)
        }


}

