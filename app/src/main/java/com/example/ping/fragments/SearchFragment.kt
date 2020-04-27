package com.example.ping.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.ping.R
import com.example.ping.adapters.PostListAdapter
import com.example.ping.listeners.PingListenerImpl
import com.example.ping.listeners.PostListener
import com.example.ping.util.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : PingFragment() {

    private var currentHashtag = ""
    private var hashtagFollowed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = PingListenerImpl(postList, currentUser, callBack)

        messageAdapter = PostListAdapter(userId!!, arrayListOf())
        messageAdapter?.setListener(listener)
        postList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = messageAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = false
            updateList()
        }

        followHashtag.setOnClickListener {
            followHashtag.isClickable = false
            val followed = currentUser?.followHashtags
            if (hashtagFollowed) {
                followed?.remove(currentHashtag)
            } else {
                followed?.add(currentHashtag)
            }
                firebaseDB.collection(DATA_USERS).document(userId).update(DATA_USER_HASHTAGS, followed)
                    .addOnSuccessListener {
                        callBack?.onUserUpdated()
                        followHashtag.isClickable = true
                    }
                    .addOnFailureListener {e ->
                        e.printStackTrace()
                        followHashtag.isClickable = true
                    }

        }
    }

    fun newHashtag(term: String){
        currentHashtag = term
        followHashtag.visibility = View.VISIBLE


        updateList()
    }

     override fun updateList() {
        postList?.visibility = View.GONE
        firebaseDB.collection(DATA_MESSAGES)
            .whereArrayContains(DATA_MESSAGE_HASHTAGS, currentHashtag).get()
            .addOnSuccessListener {list ->
                postList?.visibility = View.VISIBLE
                val posts = arrayListOf<Message>()
                for (document in list.documents){
                    val post = document.toObject(Message::class.java)
                    post?.let{ posts.add(it)}
                }
                val sortedMessages = posts.sortedWith(compareByDescending { it.timestamp })
                messageAdapter?.updatePosts(sortedMessages)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
         updateFollowDrawable()
    }
    private fun updateFollowDrawable(){
        hashtagFollowed = currentUser?.followHashtags?.contains(currentHashtag) == true
        context?.let {
            if (hashtagFollowed) {
                followHashtag.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.follow_hashtag_active))
            }else {
                followHashtag.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.followhashtag_inactive))
            }
        }
    }
}
