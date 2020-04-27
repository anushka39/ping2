package com.example.ping.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.util.Convo



class ConversationAdapter(private var messages: ArrayList<Convo>, val userId: String?): RecyclerView.Adapter<ConversationAdapter.MessagesViewHolder>() {

    companion object{
        val MESSAGE_CURRENT_USER = 1
        val MESSAGE_OTHER_USER = 2

    }

    fun addMessage(message: Convo) {
        messages.add(message)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        if(viewType == MESSAGE_CURRENT_USER){
            return MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_current_user_message, parent, false))
        } else{
            return MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_user_message, parent, false))

        }
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemViewType(position: Int): Int {
       // return super.getItemViewType(position)
        if (messages[position].sentBy.equals(userId)){
            return MESSAGE_CURRENT_USER
        }else {
            return MESSAGE_OTHER_USER
        }
    }
    class MessagesViewHolder(val view: View): RecyclerView.ViewHolder(view){
        fun bind(message: Convo){
            view.findViewById<TextView>(R.id.messageTV).text = message.message
        }
    }
}