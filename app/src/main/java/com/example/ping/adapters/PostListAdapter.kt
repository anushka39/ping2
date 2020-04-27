package com.example.ping.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.listeners.PostListener
import com.example.ping.util.Message
import com.example.ping.util.getDate
import com.example.ping.util.loadUrl

class PostListAdapter(val userId: String, val messages: ArrayList<Message>) :
    RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {

    private var listener: PostListener? = null

    fun setListener(listener: PostListener?){
        this.listener = listener
    }

    fun updatePosts(newPosts: List<Message>){
        messages.clear()
        messages.addAll(newPosts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= PostViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.item_post,parent,false)
    )
    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(userId, messages[position],listener)
    }
    class PostViewHolder(v: View) : RecyclerView.ViewHolder(v){

        private val layout = v.findViewById<ViewGroup>(R.id.postLayout)
        private val username = v.findViewById<TextView>(R.id.postUsername)
        private val text = v.findViewById<TextView>(R.id.postText)
        private val image = v.findViewById<ImageView>(R.id.postImage)
        private val date = v.findViewById<TextView>(R.id.postDate)
        private val like = v.findViewById<ImageView>(R.id.postLike)
        private val likeCount = v.findViewById<TextView>(R.id.postLikeCount)
        private val repost = v.findViewById<ImageView>(R.id.postRepost)
        private val repostCount = v.findViewById<TextView>(R.id.postRepostCount)

        fun bind(userId: String, message: Message, listener: PostListener?){
           username.text = message.username
           text.text =message.text
            if(message.imageUrl.isNullOrEmpty()){
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                image.loadUrl(message.imageUrl)
            }
            date.text = getDate(message.timestamp)

            likeCount.text= message.likes?.size.toString()
            repostCount.text = message.userIds?.size?.minus(1).toString()

            layout.setOnClickListener { listener?.onLayoutClick(message)  }
            like.setOnClickListener { listener?.onLike(message) }
            repost.setOnClickListener { listener?.onRePost(message) }

            if (message.likes?.contains(userId) == true){
                like.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.like))
            } else {
                like.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.like_inactive))
            }
            if (message.userIds?.get(0).equals(userId)){
                repost.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.original))
                repost.isClickable = false
            }else if(message.userIds?.contains(userId) == true){
                repost.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.retweet))
            } else {
                repost.setImageDrawable(ContextCompat.getDrawable(like.context, R.drawable.retweet_inactive))
            }
        }
    }
}