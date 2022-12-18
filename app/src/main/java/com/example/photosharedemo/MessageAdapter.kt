package com.example.photosharedemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.photosharedemo.databinding.MessageItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

// recyclerView adapter (inherit from FirestoreRecyclerAdapter)
// options: inclide data source queried from the database

class MessageAdapter(
    private val options: FirestoreRecyclerOptions<PostedMessage>,
    private val onItemClicked: (PostedMessage) -> Unit
): FirestoreRecyclerAdapter<PostedMessage, MessageAdapter.ViewHolder>(options) {

    class ViewHolder(val binding: MessageItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PostedMessage) {
            binding.tagText.text = item.text   //description
            binding.nameText.text = item.uploader  // uploader name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = MessageItemBinding.inflate(layoutInflater, parent, false)
        val viewHolder = ViewHolder(binding)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: PostedMessage) {
        holder.bind(model)
        // handle the item selection
        holder.itemView.setOnClickListener { onItemClicked(model) }
    }

}