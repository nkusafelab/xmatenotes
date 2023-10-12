package com.example.xmatenotes.ui.play

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xmatenotes.databinding.FragmentPlayBinding
import com.example.xmatenotes.logic.model.Play

class PlayFragment : Fragment() {

    val viewModel by lazy { ViewModelProvider(this).get(PlayViewModel::class.java) }

    private var _binding: FragmentPlayBinding?=null

    private val binding get() = _binding!!

    private lateinit var adapter: PlayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        return binding.root
//        return inflater.inflate(R.layout.fragment_place, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager
        adapter = PlayAdapter(this, viewModel.playList)
        binding.recyclerView.adapter = adapter
        binding.searchPlayEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val content = s.toString()
                if (content.isNotEmpty()) {
                    //搜索匹配的活动并显示出来
//                    viewModel.searchPlaces(content)
                } else {
//                    binding.recyclerView.visibility = View.GONE
//                    binding.bgImageView.visibility = View.VISIBLE
//                    viewModel.placeList.clear()
//                    adapter.notifyDataSetChanged()
                }
            }
        })
//        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer{ result ->
//            val places = result.getOrNull()
//            if (places != null) {
//                binding.recyclerView.visibility = View.VISIBLE
//                binding.bgImageView.visibility = View.GONE
//                viewModel.placeList.clear()
//                viewModel.placeList.addAll(places)
//                adapter.notifyDataSetChanged()
//            } else {
//                Toast.makeText(activity, "未能查询打破任何地点", Toast.LENGTH_SHORT).show()
//                result.exceptionOrNull()?.printStackTrace()
//            }
//        })
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.isPlayListSaved()) {
            val playList = viewModel.getPlayList()
            for (play in playList){
                if((System.currentTimeMillis() - play.initialTime) < play.lifeDuration){
                    viewModel.addPlay(play)
                }
            }

        }
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        viewModel.savePlayList()
    }

    /**
     * 添加新活动
     */
    fun addPlay(play: Play){
        viewModel.addPlay(play)
        adapter.notifyDataSetChanged()
    }
}