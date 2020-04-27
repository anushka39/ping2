package com.example.ping.listeners

import android.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PingListenerImpl(val postList: RecyclerView, var user: User?, val callback: HomeCallBack?): PostListener {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onLayoutClick(message: Message?) {
        message?.let {
            val owner = message.userIds?.get(0)
            if(owner != userId){
               if (user?.followUsers?.contains(owner) == true) {
                  AlertDialog.Builder(postList.context)
                      .setTitle("Unfollow ${message.username}?")
                      .setPositiveButton("yes") {dialog, which ->
                          postList.isClickable = false
                          var followedUsers = user?.followUsers
                          if (followedUsers == null) {
                              followedUsers = arrayListOf()
                          }
                          followedUsers?.remove(owner)
                          firebaseDB.collection(DATA_USERS).document(userId!!).update(
                              DATA_USER_FOLLOW,followedUsers)
                              .addOnSuccessListener {
                                  postList?.isClickable = true
                                  callback?.onUserUpdated()
                              }
                              .addOnFailureListener {
                                  postList.isClickable = true
                              }
                      }
                      .setNegativeButton("cancel"){dialog, which ->  }
                      .show()
               }else {AlertDialog.Builder(postList.context)
                   .setTitle("Follow ${message.username}?")
                   .setPositiveButton("yes") {dialog, which ->
                       postList.isClickable = false
                       var followedUsers = user?.followUsers
                       if (followedUsers == null) {
                           followedUsers = arrayListOf()
                       }
                       owner?.let {
                           followedUsers?.add(owner)
                           firebaseDB.collection(DATA_USERS).document(userId!!).update(
                               DATA_USER_FOLLOW, followedUsers
                           )
                               .addOnSuccessListener {
                                   postList?.isClickable = true
                                   callback?.onUserUpdated()
                               }
                               .addOnFailureListener {
                                   postList.isClickable = true
                               }
                       }
                   }
                   .setNegativeButton("cancel"){dialog, which ->  }
                   .show()

               }
            }
        }
    }

    override fun onLike(message: Message?) {
        message?.let {
           postList.isClickable = false
            val likes = message.likes
            if(message.likes?.contains(userId) == true) {
                likes?.remove(userId)
            }else {
                likes?.add(userId!!)
            }
            firebaseDB.collection(DATA_MESSAGES).document(message.messageId!!).update(
                DATA_MESSAGE_LIKES, likes)
                .addOnSuccessListener {
                    postList.isClickable = true
                    callback?.onRefresh()
                }
                .addOnFailureListener {
                    postList.isClickable = true
                }

        }
    }

    override fun onRePost(message: Message?) {
        message?.let {
            postList.isClickable = false
            val share = message.userIds
            if (share?.contains(userId) == true) {
                share.remove(userId)
            } else {
                share?.add(userId!!)
            }
            // UPDATE Db
            firebaseDB.collection(DATA_MESSAGES).document(message.messageId!!)
                .update(DATA_MESSAGE_USER_IDS, share )
                .addOnSuccessListener {
                    postList.isClickable = true
                    callback?.onRefresh()
                }
                .addOnFailureListener {
                    postList.isClickable = true
                }
        }
    }
}