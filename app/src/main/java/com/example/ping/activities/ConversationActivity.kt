package com.example.ping.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ping.R
import com.example.ping.adapters.ConversationAdapter
import com.example.ping.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        chatId = intent.extras.getString(PARAM_CHAT_ID)
        imageUrl = intent.extras.getString(PARAM_IMAGE_URL)
        chatName = intent.extras.getString(PARAM_CHAT_NAME)
        otherUserId = intent.extras.getString(PARAM_OTHER_USER_ID)
        Log.d("TAG", " "+chatId+ " " +imageUrl+ " "+chatName+ " "+otherUserId)


        if (chatId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "chat room error", Toast.LENGTH_LONG).show()
            finish()
        }


        topNameTV.text = chatName
        populateImage(this, imageUrl, topPhotoIV, R.drawable.default_user)

        messagesRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
//        trying to fetch info from firebase using similar method to push in chats adapter
//        firebaseDB.collection(DATA_CHATS)
//            .document(chatId!!)
//            .collection(DATA_CHAT_MESSAGES)
//         //   .orderBy(DATA_CHAT_MESSAGE_TIME)
//            .get()
//            .addOnSuccessListener { document ->
//                val message = document.toObject(Convo::class.java)
//                if (message != null) {
//                    conversationAdapter.addMessage(message)
//                }
//            }
//            .addOnFailureListener { e ->
//                e.printStackTrace()
//            }

        firebaseDB.collection(DATA_CHATS)
            .document(chatId!!)
            .collection(DATA_CHAT_MESSAGES)
            .orderBy(DATA_CHAT_MESSAGE_TIME)
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    Log.d("TAG", "inside if "+firebaseFirestoreException.toString())
                    firebaseFirestoreException.printStackTrace()
                    return@addSnapshotListener
                }else{
                    Log.d("TAG", "inside else loop")
                    if (querySnapshot != null) {
                        Log.d("TAG", "inside querysnapshot "+querySnapshot.documentChanges)
                        for (change in querySnapshot.documentChanges) {
                            Log.d("TAG", "before when "+change)
                            when (change.type) {
                                DocumentChange.Type.ADDED -> {
                                    Log.d("TAG", "inside documentchange "+change)
                                    val message = change.document.toObject(Convo::class.java)
                                    Log.d("TAG", ""+message)
                                    if (message != null) {
                                        conversationAdapter.addMessage(message)
                                        messagesRV.smoothScrollToPosition(conversationAdapter.itemCount - 1)
                                    }
                                }
                            }
                        }

                    }
                }
            }
    }
        fun onSend(v: View) {
            if (!messageET.text.isNullOrEmpty()){
               val message = Convo(userId, messageET.text.toString(), System.currentTimeMillis())
                Log.d("TAG", ""+message )
                //Toast.makeText(this, text: ""+message , Toast.LENGTH_LONG).show()
                firebaseDB.collection(DATA_CHATS).document(chatId!!)
                    .collection(DATA_CHAT_MESSAGES)
                    .document()
                    .set(message)
                messageET.setText("",TextView.BufferType.EDITABLE)
            }

        }

    companion object {
        private val PARAM_CHAT_ID = "Chat id"
        private val PARAM_IMAGE_URL = "Image url"
        private val PARAM_OTHER_USER_ID = "Other user id"
        private val PARAM_CHAT_NAME = "Chat name"

        fun newIntent(context: Context?, chatId: String?, imageUrl: String?, otherUserId: String?, chatName: String?): Intent{
            val intent = Intent(context,ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID,chatId)
            intent.putExtra(PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID, otherUserId)
            intent.putExtra(PARAM_CHAT_NAME, chatName)
            return intent
        }
    }
}
