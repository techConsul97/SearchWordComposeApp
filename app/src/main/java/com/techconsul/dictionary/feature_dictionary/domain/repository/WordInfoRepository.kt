package com.techconsul.searchwordapp.feature_dictionary.domain.repository

import com.techconsul.searchwordapp.core.util.Resource
import com.techconsul.searchwordapp.feature_dictionary.domain.model.WordInfo
import kotlinx.coroutines.flow.Flow

interface WordInfoRepository {

    fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>>
}