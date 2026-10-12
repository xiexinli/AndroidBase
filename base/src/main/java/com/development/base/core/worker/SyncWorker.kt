package com.development.base.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.development.base.core.common.ApiResult
import com.development.base.core.common.DefaultPageSize
import com.development.base.domain.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** 后台同步拉取第 1 页，页大小与首页列表保持一致。 */
private const val SyncFirstPage = 1

@HiltWorker class SyncWorker @AssistedInject constructor(
    @Assisted context: Context, @Assisted params: WorkerParameters, private val repository: PostRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork() = when (repository.loadPage(SyncFirstPage, DefaultPageSize)) {
        is ApiResult.Success -> Result.success()
        is ApiResult.Error -> Result.retry()
    }
}
