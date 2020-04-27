package com.example.ping.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.listenerc.ChatClicklListener
import com.example.ping.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatsAdapter(val chats: ArrayList<String>): RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var clickListener: ChatClicklListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= ChatsViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
    )

    override fun getItemCount()= chats.size

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(chats[position], clickListener)
    }

    fun setOnItemClickListener(listener: ChatClicklListener){
        clickListener = listener
        notifyDataSetChanged()
    }

    fun updateChats(updatedchats: ArrayList<String>){
        chats.clear()
        chats.addAll(updatedchats)
        notifyDataSetChanged()
    }

    class ChatsViewHolder(view: View): RecyclerView.ViewHolder(view){

        private val firebaseDB = FirebaseFirestore.getInstance()
        private val userId = FirebaseAuth.getInstance().currentUser?.uid
        private var layout = view.findViewById<RelativeLayout>(R.id.chatLayout)
        private var chatIV =  view.findViewById<ImageView>(R.id.chatIV)
        private var chatNameTV = view.findViewById<TextView>(R.id.chatTV)
        private var progressLayout = view.findViewById<LinearLayout>(R.id.chatsprogressLayout)
        private var partnerId: String? = null
        private var chatImageUrl: String? = null
        private var chatName: String? = null

        fun bind(chatsId: String, listener: ChatClicklListener?){
            progressLayout.visibility = View.VISIBLE
            progressLayout.setOnTouchListener{v, event -> true }

            firebaseDB.collection(DATA_CHATS)
                .document(chatsId)
                .get()
                .addOnSuccessListener {document ->
                    val chatParticipants = document[DATA_CHAT_PARTICIPANTS]
                    if(chatParticipants != null) {
                        for(participant in chatParticipants as ArrayList<String>){
                            if(participant != null && !participant.equals((userId))){
                                partnerId = participant
                                firebaseDB.collection(DATA_USERS)
                                    .document(partnerId!!)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        val user = document.toObject(User::class.java)
                                        chatImageUrl = user?.imageUrl
                                        chatName = user?.username
                                        chatNameTV.text = user?.username
                                        populateImage(chatIV.context, user?.imageUrl, chatIV, R.drawable.default_user)
                                        progressLayout.visibility = View.GONE
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                        progressLayout.visibility = View.GONE
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    progressLayout.visibility = View.GONE
                }
            layout.setOnClickListener{ listener?.onChatClicked(chatsId,partnerId, chatImageUrl,chatName)}
        }
    }
}