package com.androidbase.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.androidbase.core.common.ApiResult
import com.androidbase.domain.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker class SyncWorker @AssistedInject constructor(
    @Assisted context: Context, @Assisted params: WorkerParameters, private val repository: PostRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork() = when (repository.refreshPosts()) {
        is ApiResult.Success -> Result.success()
        is ApiResult.Error -> Result.retry()
    }
}
