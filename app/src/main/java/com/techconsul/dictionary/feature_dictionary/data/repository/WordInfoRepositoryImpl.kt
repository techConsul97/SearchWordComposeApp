package com.techconsul.dictionary.feature_dictionary.data.repository

import com.techconsul.dictionary.feature_dictionary.data.local.WordInfoDao
import com.techconsul.dictionary.feature_dictionary.data.local.entity.WordInfoEntity
import com.techconsul.searchwordapp.core.util.Resource
import com.techconsul.searchwordapp.feature_dictionary.data.remote.DictionaryApi
import com.techconsul.searchwordapp.feature_dictionary.data.remote.dto.WordInfoDto
import com.techconsul.searchwordapp.feature_dictionary.domain.model.WordInfo
import com.techconsul.dictionary.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class WordInfoRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordInfoDao
) : WordInfoRepository {

    override fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow {
        emit(Resource.Loading())

        val wordInfos = getWordInfosFromDb(word).map { it.toWordInfo() }
        emit(Resource.Loading(data = wordInfos))

        try {
            val remoteWordInfos = getWordInfoFromApi(word)
            deleteWordInfos(remoteWordInfos.map { it.word })
            insertWordInfos(remoteWordInfos.map { it.toWordInfoEntity() })
        } catch (e: HttpException) {
            val code = e.code()
            emit(
                Resource.Error(
                    message = "Oops, something went wrong..Code:$code!",
                    data = wordInfos
                )
            )
        } catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "Couldn't reach server, check your internet connection.",
                    data = wordInfos
                )
            )
        }

        val newWordInfos = getWordInfosFromDb(word).map { it.toWordInfo() }
        emit(Resource.Success(newWordInfos))
    }

    override suspend fun getWordInfosFromDb(word: String) = dao.getWordInfos(word)
    override suspend fun getWordInfoFromApi(word:String): List<WordInfoDto> = api.getWordInfo(word)
    override suspend fun deleteWordInfos(words: List<String>) = dao.deleteWordInfos(words)
    override suspend fun insertWordInfos(words: List<WordInfoEntity>) = dao.insertWordInfos(words)

}