package com.one.russell.metroman.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.one.russell.metroman.MainViewModel
import com.one.russell.metroman.R
import kotlin.properties.Delegates
import kotlinx.android.synthetic.main.subfragment_bookmarks.*

class BookmarksSubfragment : Fragment() {

    private var count: Int = 0
    private var current: Int = 0
    private var model: MainViewModel by Delegates.notNull()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity
        if(activity != null) {
            model = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        }

        count = model.bookmarks.size

        return inflater.inflate(R.layout.subfragment_bookmarks, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(count > 0) {
            current = 1
            tvCount.text = current.toString() + "/" + count.toString()
            bookmarkValue.text = model.bookmarks[current - 1].toString()
        }

        btRightArrow.setOnClickListener {
            current++
            if(current <= count) {
                tvCount.text = current.toString() + "/" + count.toString()
                bookmarkValue.text = model.bookmarks[current - 1].toString()

                model.bpmLiveData.postValue(model.bookmarks[current - 1])
            } else {
                current--
            }
        }

        btLeftArrow.setOnClickListener {
            current--
            if(current >= 1) {
                tvCount.text = current.toString() + "/" + count.toString()
                bookmarkValue.text = model.bookmarks[current - 1].toString()

                model.bpmLiveData.postValue(model.bookmarks[current - 1])
            } else {
                current++
            }
        }

        btAdd.setOnClickListener {
            val bpm = model.bpmLiveData.value
            if(bpm != null) {
                model.bookmarks.add(bpm)
            }
            count = model.bookmarks.size
            current = count
            bookmarkValue.text = model.bookmarks[current - 1].toString()
            tvCount.text = current.toString() + "/" + count.toString()
        }

        btRemove.setOnClickListener {
            model.bookmarks.removeAt(current - 1)
            count = model.bookmarks.size
            if (current > count) {
                current = count
            }
            if(count == 0) {
                bookmarkValue.text = ""
                tvCount.text = current.toString() + "/" + count.toString()
            } else {
                bookmarkValue.text = model.bookmarks[current - 1].toString()
                tvCount.text = current.toString() + "/" + count.toString()
            }
        }

        btClear.setOnClickListener {
            model.bookmarks.clear()
            count = 0
            current = 0
            bookmarkValue.text = ""
            tvCount.text = current.toString() + "/" + count.toString()
        }
    }
}