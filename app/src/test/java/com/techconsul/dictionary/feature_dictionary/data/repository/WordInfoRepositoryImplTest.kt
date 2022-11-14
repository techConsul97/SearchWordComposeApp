package com.techconsul.dictionary.feature_dictionary.data.repository

import android.util.Log
import com.techconsul.dictionary.feature_dictionary.data.local.WordInfoDao
import com.techconsul.dictionary.feature_dictionary.data.local.entity.WordInfoEntity
import com.techconsul.searchwordapp.core.util.Resource
import com.techconsul.searchwordapp.feature_dictionary.data.remote.DictionaryApi
import com.techconsul.searchwordapp.feature_dictionary.data.remote.dto.WordInfoDto
import com.techconsul.searchwordapp.feature_dictionary.domain.model.WordInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WordInfoRepositoryImplTest {

    private lateinit var repository: WordInfoRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()
    private val dummyDB = mutableListOf<WordInfoEntity>()

    @Mock
    private lateinit var dao: WordInfoDao

    @Mock
    private lateinit var api: DictionaryApi

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        repository = WordInfoRepositoryImpl(api, dao)

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /* First we will test if the first emission of the getWordInfo function from Repository is emitting Loading State
            Second, we will simulate a response from the Api, Failed, because of Internet Connection and check if the emission
            is Failed
            Third, we will test with an HTTPEXception, and expect Failed
            Fourth, we will simulate a Successful Response, when that happens InsertWord function will be called to save the Word into DB
            To avoid that, we will save it locally, into dummyDB variable
         */

    @Test
    fun getWordInfoTest() = runBlocking {
        assert(repository.getWordInfo("isItLoading").first() is Resource.Loading)
        assertFalse(repository.getWordInfo("isItLoading").first() is Resource.Error)
        assertFalse(repository.getWordInfo("isItLoading").first() is Resource.Success)
    }
    @Test
    fun testSecondScenario() = runBlocking {
        whenever(dao.getWordInfos("SecondScenario")).thenReturn(emptyList())
        whenever(api.getWordInfo("SecondScenario")).then{throw IOException()}
        repository.getWordInfo("SecondScenario")
            .filter{it is Resource.Error}
            .collect {
                assert(it is Resource.Error)
                assert(it.message.equals("Couldn't reach server, check your internet connection."))
            }
    }

    @Test
    fun testThirdScenario() = runBlocking {
        whenever(dao.getWordInfos("ThirdScenario")).thenReturn(emptyList())
        whenever(api.getWordInfo("ThirdScenario")).doAnswer { throw HttpException(Response.error<WordInfo>(12000,"Got it now?".toResponseBody())) }
        repository.getWordInfo("ThirdScenario")
            .filter {it is Resource.Error}
            .collect{
            assert(it is Resource.Error)
                assert(it.message.equals("Oops, something went wrong..Code:12000!"))
        }
    }

    @Test
    fun testFourthScenario() = runBlocking {
        val dummyData = WordInfoEntity(word = "test",null,null,null,4)
        whenever(dao.getWordInfos("FourthScenario")).thenReturn(emptyList())
        whenever(api.getWordInfo("FourthScenario")).thenReturn(
            listOf(WordInfoDto(listOf(),"Test","test", listOf(),"FourthCase"))
        )
        whenever(dao.insertWordInfos(listOf())).then{ dummyDB.add(dummyData) }
        whenever(dao.deleteWordInfos(dummyDB.map { it.word!! })).then{ dummyDB.add(dummyData) }


        repository.getWordInfo("FourthScenario")
            .filter {
                it is Resource.Success

            }
            .collect{
                Log.d("State", it.toString())
                assert(it is Resource.Success)
                assert(it.data!!.size == 1)
            }

    }

}

