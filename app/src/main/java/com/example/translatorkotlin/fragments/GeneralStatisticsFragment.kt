package com.example.translatorkotlin.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.common.data.User
import com.example.translatorkotlin.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_general_statistics.*
import java.util.*
import kotlin.collections.ArrayList

class GeneralStatisticsFragment : Fragment(R.layout.fragment_general_statistics) {
    private val database = Firebase.database
    private var sortByQuant = true
    private lateinit var valueEventListener: ValueEventListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = setupRecycler()
        val myRef = database.getReference("users")
        setupValueListener(adapter)
        myRef.addValueEventListener(valueEventListener)

        setOnClickListeners(myRef)
    }

    private fun setOnClickListeners(myRef: DatabaseReference) {
        textSortImage.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_text_sort_active
            )
        )
        timeSortImage.setOnClickListener {
            sortByQuant = false
            timeSortImage.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_time_sort_active
                )
            )
            textSortImage.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_text_sort
                )
            )
            myRef.addValueEventListener(valueEventListener)
        }
        textSortImage.setOnClickListener {
            sortByQuant = true
            textSortImage.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_text_sort_active
                )
            )
            timeSortImage.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_time_sort
                )
            )
            myRef.addValueEventListener(valueEventListener)
        }
    }

    private fun setupValueListener(adapter: GeneralStatAdapter) {
        valueEventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("GeneralStat", "Error in loading data")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null && dataSnapshot.hasChildren()) {
                    val userArrayList = ArrayList<User>()
                    for (child in dataSnapshot.children) {
                        val user: User? = child.getValue<User>()
                        userArrayList.add(user!!)
                    }
                    if (sortByQuant) {
                        userArrayList.sortWith(Comparator { o1, o2 ->
                            o2.countWords!!.compareTo(o1.countWords!!)
                        })
                    } else {
                        userArrayList.sortWith(Comparator { o1, o2 ->
                            o2.time!!.compareTo(o1.time!!)
                        })
                    }
                    adapter.setUserList(userArrayList, context!!)
                }
            }
        }
    }

    private fun setupRecycler(): GeneralStatAdapter {
        val adapter = GeneralStatAdapter()
        recyclerUsers.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerUsers.adapter = adapter
        return adapter
    }

    class GeneralStatAdapter : RecyclerView.Adapter<GeneralStatAdapter.ViewHolder>() {
        private var userList = mutableListOf<User>()
        private var context: Context? = null

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var numberText: TextView = itemView.findViewById(R.id.numberTextView)
            var nameText: TextView = itemView.findViewById(R.id.nameUserTextView)
            var quantityText: TextView = itemView.findViewById(R.id.quantityUser)
            var timeText: TextView = itemView.findViewById(R.id.timeUser)
            var userImage: ImageView = itemView.findViewById(R.id.userImageRecycler)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false))
        }

        fun setUserList(list: List<User>, context: Context) {
            this.context = context
            userList.clear()
            userList.addAll(list)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = userList.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = userList[position]
            holder.timeText.text = user.time + " min"
            holder.numberText.text = "${(position + 1)}"
            holder.quantityText.text = user.countWords
            holder.nameText.text = user.name
            Glide.with(context!!).load(user.url).into(holder.userImage)
        }
    }
}
