package com.development.app.feature.home

import com.development.base.core.common.ApiResult
import com.development.base.core.common.DefaultPageSize
import com.development.base.domain.model.Post
import com.development.base.domain.repository.PostRepository
import com.development.base.domain.usecase.GetPostsUseCase
import com.development.app.feature.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** 首页列表「下拉刷新 / 上拉加载更多」状态机的单元测试。 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelPaginationTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `first load takes one page and is not ended`() = runTest {
        val viewModel = HomeViewModel(GetPostsUseCase(FakePostRepository(total = 45)))

        val state = viewModel.state.value
        assertEquals(DefaultPageSize, state.posts.size)
        assertEquals(1, state.posts.first().id)
        assertFalse(state.loading)
        assertFalse(state.endReached)
    }

    @Test fun `load more appends the next page`() = runTest {
        val viewModel = HomeViewModel(GetPostsUseCase(FakePostRepository(total = 45)))

        viewModel.loadMore()

        val state = viewModel.state.value
        assertEquals(DefaultPageSize * 2, state.posts.size)
        assertEquals(DefaultPageSize * 2, state.posts.last().id)
        assertFalse(state.loadingMore)
        assertFalse(state.endReached)
    }

    @Test fun `last short page marks the list ended and stops requesting`() = runTest {
        val repository = FakePostRepository(total = 45)
        val viewModel = HomeViewModel(GetPostsUseCase(repository))

        viewModel.loadMore()
        viewModel.loadMore()
        val loadedRequests = repository.requests
        viewModel.loadMore()

        val state = viewModel.state.value
        assertEquals(45, state.posts.size)
        assertTrue(state.endReached)
        assertEquals(loadedRequests, repository.requests)
    }

    @Test fun `refresh failure keeps the current list and reports the error`() = runTest {
        val repository = FakePostRepository(total = 45)
        val viewModel = HomeViewModel(GetPostsUseCase(repository))

        repository.fail = true
        viewModel.refresh()

        val state = viewModel.state.value
        assertEquals(DefaultPageSize, state.posts.size)
        assertFalse(state.refreshing)
        assertEquals("网络不可用", state.error)
    }

    @Test fun `first load failure falls back to the local cache`() = runTest {
        val repository = FakePostRepository(total = 45, cached = listOf(postOf(1)))
        repository.fail = true

        val state = HomeViewModel(GetPostsUseCase(repository)).state.value

        assertEquals(listOf(postOf(1)), state.posts)
        assertTrue(state.endReached)
        assertEquals("网络不可用", state.error)
    }
}

private fun postOf(id: Int) = Post(id, "title $id", "body $id", "image $id")

private class FakePostRepository(
    private val total: Int,
    private val cached: List<Post> = emptyList()
) : PostRepository {

    /** 记录调用过的页码，用于断言「已到底后不再请求」。 */
    val requests = mutableListOf<Int>()
    var fail = false

    override suspend fun loadPage(page: Int, pageSize: Int): ApiResult<List<Post>> {
        requests += page
        if (fail) return ApiResult.Error("网络不可用")
        val from = (page - 1) * pageSize
        if (from >= total) return ApiResult.Success(emptyList())
        val to = minOf(from + pageSize, total)
        return ApiResult.Success((from until to).map { postOf(it + 1) })
    }

    override suspend fun cachedPosts(): List<Post> = cached

    override suspend fun post(id: Int): Post? = cached.firstOrNull { it.id == id }
}
