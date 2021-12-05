package com.myapp.preferencemanagersample

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.preferencemanagersample.data.local.preferences.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val preferenceManager: PreferenceManager
): ViewModel() {

    init {
        Log.d("preferenceManager", "---検証開始---")
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val key1 = PreferenceManager.Key.LongListKey.Long1
                val key2 = PreferenceManager.Key.LongListKey.Long2
                val start1 = preferenceManager.getList(key1)
                val start2 = preferenceManager.getList(key2)
                Log.d("preferenceManager", "start　＝　" + start1)
                Log.d("preferenceManager", "start　＝　" + start2)
                async {
                    async { threadLong(1, key1) }
                    async { threadLong(2, key2) }
                    async { threadLong(3, key1) }
                    async { threadLong(4, key2) }
                    async { threadLong(5, key1) }
                    async { threadLong(5, key2) }
                }.await()
                val end1 = preferenceManager.getList(key1)
                val end2 = preferenceManager.getList(key2)
                Log.d("preferenceManager", "end　＝　" + end1)
                Log.d("preferenceManager", "end　＝　" + end2)
                Log.d("preferenceManager", "size1　＝　" + end1.size)
                Log.d("preferenceManager", "size2　＝　" + end2.size)
            }
        }
    }

    fun threadInt(
        index: Int,
        key: PreferenceManager.Key.IntKey
    ) {
        Log.d("preferenceManager", "非同期開始　key　＝　" + key + ",index = " + index)
        for(i in 1..10000) {
            val value = preferenceManager.getInt(key) + 1
            preferenceManager.setInt(key, value)
        }
        Log.d("preferenceManager", "非同期終了　key　＝　" + key + ",index = " + index)
    }

    fun threadLong(
        index: Int,
        key: PreferenceManager.Key.LongListKey
    ) {
        Log.d("preferenceManager", "非同期開始　key　＝　" + key.name + ",index = " + index)
        val start = 1 + 1000 * (index - 1)
        val end = 1000 * index
        for(i in start..end) {
            preferenceManager.addFromList(key, i.toLong())
        }
        Log.d("preferenceManager", "非同期終了　key　＝　" + key.name + ",index = " + index)
    }
}