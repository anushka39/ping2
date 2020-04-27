package com.example.ping.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ping.R
import com.example.ping.adapters.ContactAdapter
import com.example.ping.listenerc.ContactsClickListener
import com.example.ping.util.Contact
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity(), ContactsClickListener {

    private val contactsList = ArrayList<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        getContacts()
    }
    private fun getContacts(){
        contactsprogressLayout.visibility = View.GONE
        contactsList.clear()
        val newList =ArrayList<Contact>()
        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,null,null)
        while(phones!!.moveToNext()){
            val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            newList.add(Contact(name,phoneNumber))
        }
        contactsList.addAll(newList)
        phones.close()

        setupList()
    }

    fun setupList(){
        contactsprogressLayout.visibility = View.GONE
        val contactsAdapter = ContactAdapter(contactsList)
        contactsAdapter.setOnItemClickListener(this)
        contactsRV.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contactsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onContactClicked(name: String?, phone: String?) {
      val intent = Intent()
        intent.putExtra(MainScreenActivity.PARAM_USER_NAME, name)
        intent.putExtra(MainScreenActivity.PARAM_PHONE, phone)
        setResult(Activity.RESULT_OK , intent)
    }

    companion object {
        fun newIntent(context: Context)= Intent(context, ContactsActivity::class.java )
    }



}
