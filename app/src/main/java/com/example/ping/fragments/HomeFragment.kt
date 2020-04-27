package com.example.ping.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.ping.R
import com.example.ping.adapters.PostListAdapter
import com.example.ping.listeners.PingListenerImpl
import com.example.ping.util.DATA_MESSAGES
import com.example.ping.util.DATA_MESSAGE_HASHTAGS
import com.example.ping.util.DATA_MESSAGE_USER_IDS
import com.example.ping.util.Message
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : PingFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
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
    }
    override fun updateList() {
        postList?.visibility = View.GONE
        currentUser?.let {
            val posts = arrayListOf<Message>()

            for (hashtag in it.followHashtags!!) {
                firebaseDB.collection(DATA_MESSAGES).whereArrayContains(DATA_MESSAGE_HASHTAGS, hashtag).get()
                    .addOnSuccessListener {list ->
                        for(document in list.documents){
                            val post = document.toObject(Message::class.java)
                            post?.let {posts.add(it)}
                        }
                        updateAdapter(posts)
                        postList?.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {e ->
                        e.printStackTrace()
                        postList?.visibility = View.GONE
                    }
            }
            for (follwedUser in it.followUsers!!) {
                firebaseDB.collection(DATA_MESSAGES).whereArrayContains(DATA_MESSAGE_USER_IDS, follwedUser).get()
                    .addOnSuccessListener {list ->
                        for(document in list.documents){
                            val post = document.toObject(Message::class.java)
                            post?.let {posts.add(it)}
                        }
                        updateAdapter(posts)
                        postList?.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {e ->
                        e.printStackTrace()
                        postList?.visibility = View.VISIBLE
                    }
            }
        }
    }
    private fun updateAdapter(posts: List<Message>) {
        val sortedPosts = posts.sortedWith(compareByDescending { it.timestamp })
        messageAdapter?.updatePosts(removeDuplication(sortedPosts))
    }

    private fun removeDuplication(originalList: List<Message>) = originalList.distinctBy { it.messageId }
}
