package com.example.gear_kotlin.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.local.dao.UserDao
import com.example.model.RadditNews
import com.example.model.RedditNewsItem
import com.example.model.RedditNewsResponse
import com.example.model.User
import com.example.remote.NewsApi
import com.example.repository.NewsRepository
import com.example.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class ThirdFragmentViewModel @Inject constructor(var repository: UserRepository,
                                                 var newsRepository: NewsRepository,
                                                 var userDao: UserDao): ViewModel() {
    // TODO: Implement the ViewModel
    var users = MutableLiveData<List<User>>()
    var user = MutableLiveData<User>()
    var i = 0
    init {
        user.value = User(1, "william")
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                repository.deleteAll()
                insert()
                users.postValue(repository.getAllUserList())
            }
        }
    }


    fun fetchNews( after: String, limit: String = "10") = viewModelScope.launch (Dispatchers.IO) {
        try {
            val result = newsRepository.getNewsOldApi(after, limit)
            Log.i("hanhmh1203","result $result")
            val news = process(result)
//            newsState.postValue(NewsState.Success(news))
        } catch (e: Throwable) {
            Log.i("hanhmh1203","result $e")
//            newsState.postValue(NewsState.Error(e.message))
        }
    }
    private fun process(response: RedditNewsResponse): RadditNews {
        val dataResponse = response.data
        val news = dataResponse.children.map {
            val item = it.data
            RedditNewsItem(item.author, item.title, item.num_comments,
                item.created, item.thumbnail, item.url)
        }
        return RadditNews(
            dataResponse.after.orEmpty(),
            dataResponse.before.orEmpty(),
            news)
    }
    fun increaseI(){
        Log.i("increaseI","value $i")
        i++
    }
    private suspend fun insert() {
        repository.getUserDummy().forEach {
            userDao.insert(it)
        }
    }
    /**
     * postValue used in Background thread
     *
     */
    fun setUserName(otherName: String) {
        user.value!!.name = otherName
        user.postValue(user.value)
    }

    /**
     * used in UI thread
     */
    fun setUserNameWithOut(otherName: String) {
        user.value = User(1, otherName)
    }
    var increase =0
    fun click(){
        increase++
        setUserNameWithOut("hanhmh1203 $increase")
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("ThirdFragment"," onCleared user value ${user.value!!.name}")
    }
}
