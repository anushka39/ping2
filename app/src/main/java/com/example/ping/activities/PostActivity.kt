package com.example.ping.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.ping.R
import com.example.ping.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*

class PostActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private var imageUrl: String? = null
    private var userId:String? = null
    private var userName:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        if(intent.hasExtra(PARAM_USER_ID)&&intent.hasExtra(PARAM_USER_NAME)){
            userId = intent.getStringExtra(PARAM_USER_ID)
            userName = intent.getStringExtra(PARAM_USER_NAME)
        }else{
            Toast.makeText(this,"Error creating message", Toast.LENGTH_SHORT).show()
            finish()
        }
        messageProgressLayout.setOnTouchListener { v, event -> true }
    }

    fun addImage(v: View){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_PHOTO)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    fun storeImage(imageUri: Uri?) {
        imageUri?.let {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            messageProgressLayout.visibility = View.VISIBLE

            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener { uri ->
                            imageUrl = uri.toString()
                            messageImage.loadUrl(imageUrl, R.drawable.logo)
                            messageProgressLayout.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailure()
                        }
                }
                .addOnFailureListener {
                    onUploadFailure()
                }
        }
    }
    fun onUploadFailure(){
        Toast.makeText( this, "Image upload failed. Please try again later.",Toast.LENGTH_SHORT).show()
        messageProgressLayout.visibility = View.GONE
    }
    fun postMessage(v: View){
        messageProgressLayout.visibility = View.VISIBLE
        val text = messageText.text.toString()
        val hashtags = getHashtags(text)

        val messageId = firebaseDB.collection(DATA_MESSAGES).document()
        val message = Message(messageId.id, arrayListOf(userId!!), userName, text, imageUrl, System.currentTimeMillis(), hashtags, arrayListOf())
        messageId.set(message)
            .addOnCompleteListener { finish() }
            .addOnFailureListener { e ->
                e.printStackTrace()
                messageProgressLayout.visibility = View.GONE
                Toast.makeText(this,"Failed to post message",Toast.LENGTH_SHORT).show()
            }
    }

    fun getHashtags(source: String): ArrayList<String> {
        val hashtags = arrayListOf<String>()
        var text = source
       // hashtags.add(text)

        while (text.contains("#")){
            var hashtag = ""
            val hash = text.indexOf("#")
            text = text.substring(hash + 1)
           // hashtags.add(text)
            val firstSpace = text.indexOf(" ")
            val firstHash = text.indexOf("#")

            if(firstSpace == -1 && firstHash == -1){
                hashtag = text.substring(0)
            }else if (firstSpace != -1 && firstSpace < firstHash ){
                hashtag = text.substring(0, firstSpace)
                text = text.substring(firstSpace + 1)
            } else {
                hashtag = text.substring(0,firstHash)
                text = text.substring(firstHash)
            }
            if (!hashtag.isNullOrEmpty()){
              hashtags.add(hashtag)
            }
          //  hashtags.add(text)
        }
        return hashtags
    }
    companion object {
        val PARAM_USER_ID = "UserId"
        val PARAM_USER_NAME = "UserName"
        fun newIntent(context: Context, userId: String?, userName: String? ):Intent {
            val intent = Intent(context, PostActivity::class.java )
            intent.putExtra(PARAM_USER_ID,userId)
            intent.putExtra(PARAM_USER_NAME, userName)
            return intent
        }
    }
}
