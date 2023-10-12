package com.example.xmatenotes.ui.play

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.xmatenotes.R
import com.example.xmatenotes.logic.model.Play

class PlayAdapter(private val fragment: Fragment, private val playList: List<Play>) : RecyclerView.Adapter<PlayAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val initialTime: TextView = view.findViewById(R.id.intialTime)
        val author: TextView = view.findViewById(R.id.author)
        var remainingTime: TextView = view.findViewById(R.id.remainingTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.play_item, parent, false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val play = playList[position]
            val activity = fragment.activity
            if (activity is PlayShowActivity) {
                activity.closeDrawers()
                activity.viewModel.savePlay(play)
//                activity.viewModel.locationLng = place.location.lng
//                activity.viewModel.locationLat = place.location.lat
//                activity.viewModel.placeName = place.name
                activity.refreshPlay()
            } else {
                var bundle = Bundle()
                bundle
                val intent = Intent(parent.context, PlayShowActivity::class.java).apply {
//                    putExtra("location_lng", place.location.lng)
//                    putExtra("location_lat", place.location.lat)
//                    putExtra("place_name", place.name)
                    putExtra("playTitle", PlayShowViewModel.getPlayTitle(play))
                    putExtra("enumData", PlayShowViewModel.getEnumText(play))
                }
                fragment.startActivity(intent)
//                activity?.finish()
            }
//            fragment.viewModel.savePlace(place)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val play = playList[position]
        holder.title.text = play.title
        holder.author.text = play.role
        holder.initialTime.text = play.initialTime.toString()
        holder.remainingTime.text = play.remainingTime.toString()

//        holder.placeName.text = place.name
//        holder.placeAddress.text = place.address
    }

    override fun getItemCount() = playList.size
}