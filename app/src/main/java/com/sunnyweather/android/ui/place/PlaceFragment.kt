package com.sunnyweather.android.ui.place

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import androidx.core.widget.addTextChangedListener

class PlaceFragment: Fragment() {
    //用lazy懒加载技术获取PlaceViewModel实例
    private val viewModel by lazy { ViewModelProvider(this)?.get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter

    //加载前面编写的fragment_place布局
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager=LinearLayoutManager(activity)
        val recyclerView=view.findViewById<RecyclerView>(R.id.recyclerView)
        val bgImageView=view.findViewById<ImageView>(R.id.bgImageView)
        val searchPlaceText=view.findViewById<EditText>(R.id.searchPlaceEdit)
        recyclerView.layoutManager=layoutManager
        adapter= PlaceAdapter(this,viewModel!!.placeList)
        recyclerView.adapter=adapter
        //监听搜索框的变化，获取新内容
        searchPlaceText.addTextChangedListener {text: Editable? ->
            val content=text.toString()
            if (content.isNotEmpty()){
                viewModel!!.searchPlaces(content)
            }else{
                recyclerView.visibility=View.GONE
                bgImageView.visibility=View.VISIBLE

                viewModel!!.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        viewModel!!.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
            val places=result.getOrNull()
            if (places!=null){
                recyclerView.visibility=View.VISIBLE
                bgImageView.visibility=View.GONE
                viewModel!!.placeList.clear()
                viewModel!!.placeList.addAll(places)
                adapter.notifyDataSetChanged()
            }else{
                Toast.makeText(activity,"nonononononoon",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}