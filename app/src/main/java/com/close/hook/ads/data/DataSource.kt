package com.close.hook.ads.data

import android.content.Context
import com.close.hook.ads.BlockedBean
import com.close.hook.ads.data.database.UrlDatabase
import com.close.hook.ads.data.model.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DataSource(context: Context) {
    private val urlDao = UrlDatabase.getDatabase(context).urlDao

    fun getUrlList(): Flow<List<Url>> = urlDao.loadAllList()

    fun addUrl(url: Url) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!urlDao.isExist(url.type, url.url))
                urlDao.insert(url)
        }
    }

    fun removeList(list: List<Url>) {
        CoroutineScope(Dispatchers.IO).launch {
            urlDao.deleteList(list)
        }
    }

    fun removeUrl(url: Url) {
        CoroutineScope(Dispatchers.IO).launch {
            urlDao.deleteUrl(url)
        }
    }

    fun removeAll() {
        CoroutineScope(Dispatchers.IO).launch {
            urlDao.deleteAll()
        }
    }

    fun addListUrl(list: List<Url>) {
        CoroutineScope(Dispatchers.IO).launch {
            urlDao.insertAll(list)
        }
    }

    fun updateUrl(url: Url) {
        CoroutineScope(Dispatchers.IO).launch {
            urlDao.update(url)
        }
    }

    fun removeUrlString(type: String, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            urlDao.deleteUrlString(type, url)
        }
    }

    fun search(searchText: String): List<Url> {
        return urlDao.searchUrls(searchText)
    }

    fun isExist(type: String, url: String): Boolean {
        return urlDao.isExist(type, url)
    }

    fun checkIsBlocked(type: String, value: String): BlockedBean {
        val urls = urlDao.getAllUrls()
        val blockedUrl = urls.find {
            when (it.type) {
                "KeyWord" -> it.url in value

                "Domain" -> type == it.type && getDomain(value) == it.url

                "URL" -> type == it.type && value == it.url

                else -> false
            }
        }
        return blockedUrl?.let {
            BlockedBean(true, it.type, it.url)
        } ?: BlockedBean(false, null, null)
    }

    private fun getDomain(url: String): String {
        val replace = url.replace("https://", "").replace("http://", "")
        with(replace.indexOfFirst { it == '/' }) {
            return if (this == -1) replace
            else replace.substring(0, this)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DataSource? = null

        fun getDataSource(context: Context): DataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataSource(context).also { INSTANCE = it }
            }
        }
    }
}
