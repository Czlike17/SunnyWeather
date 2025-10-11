package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment: Fragment() {
    //用lazy懒加载技术获取PlaceViewModel实例
    val viewModel by lazy { ViewModelProvider(this)?.get(PlaceViewModel::class.java) }
    private lateinit var adapter: PlaceAdapter

    //加载前面编写的fragment_place布局
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_place, container, false)

        view.findViewById<Button>(R.id.btn_back_to_device).setOnClickListener {
            activity?.finish()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel?.takeIf { activity is MainActivity&& it.isPlaceSaved() }?.let { vm ->
            vm.getSavedPlace()?.let { place ->
                Intent(context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                    putExtra("place_name", place.name)
                }.run {
                    startActivity(this)
                    activity?.finish()
                    return
                }
            }
        }
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
                Toast.makeText(activity,"未能查询到任何地点",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}