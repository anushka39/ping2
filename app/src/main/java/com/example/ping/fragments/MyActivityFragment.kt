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
import com.example.ping.util.DATA_MESSAGE_USER_IDS
import com.example.ping.util.Message
import kotlinx.android.synthetic.main.fragment_my_activity.*

class MyActivityFragment : PingFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_activity, container, false)
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
        val posts = arrayListOf<Message>()

        firebaseDB.collection(DATA_MESSAGES).whereArrayContains(DATA_MESSAGE_USER_IDS, userId!!).get()
            .addOnSuccessListener {list ->
                for (document in list.documents) {
                    val post = document.toObject(Message::class.java)
                    post?.let { posts.add(post)
                    }
                }
                val sortedList = posts.sortedWith(compareByDescending { it.timestamp })
                messageAdapter?.updatePosts(sortedList)
                postList?.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                postList?.visibility = View.VISIBLE
            }
    }

}
