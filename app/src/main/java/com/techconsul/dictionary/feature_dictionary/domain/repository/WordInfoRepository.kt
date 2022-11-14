package com.techconsul.dictionary.feature_dictionary.domain.repository

import com.techconsul.dictionary.feature_dictionary.data.local.entity.WordInfoEntity
import com.techconsul.searchwordapp.core.util.Resource
import com.techconsul.searchwordapp.feature_dictionary.data.remote.dto.WordInfoDto
import com.techconsul.searchwordapp.feature_dictionary.domain.model.WordInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface WordInfoRepository {

    fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow{}

    suspend fun getWordInfosFromDb(word:String): List<WordInfoEntity>
    suspend fun deleteWordInfos(words:List<String>)
    suspend fun insertWordInfos(words:List<WordInfoEntity>)

    suspend fun getWordInfoFromApi(word:String):List<WordInfoDto>
}