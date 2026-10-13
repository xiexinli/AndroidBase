package com.development.app.feature.webview

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [WebViewViewModel] 的状态与事件契约测试。 */
class WebViewViewModelTest {

    private val pageUrl = "https://www.baidu.com"

    private fun viewModel(url: String = pageUrl) =
        WebViewViewModel(SavedStateHandle(mapOf(WebViewDestination.UrlArg to url)))

    @Test
    fun `url is taken from the navigation argument`() {
        assertEquals(pageUrl, viewModel().state.value.url)
    }

    @Test(expected = IllegalStateException::class)
    fun `missing url argument fails fast`() {
        WebViewViewModel(SavedStateHandle())
    }

    @Test
    fun `progress drives loading state`() {
        val viewModel = viewModel()

        viewModel.onProgressChanged(42)
        assertTrue(viewModel.state.value.loading)
        assertEquals(42, viewModel.state.value.progress)

        viewModel.onProgressChanged(100)
        assertFalse(viewModel.state.value.loading)
        assertEquals(100, viewModel.state.value.progress)
    }

    @Test
    fun `blank title keeps the previous one`() {
        val viewModel = viewModel()

        viewModel.onTitleChanged("百度一下")
        assertEquals("百度一下", viewModel.state.value.title)

        viewModel.onTitleChanged(" ")
        assertEquals("百度一下", viewModel.state.value.title)
    }

    @Test
    fun `h5 message is surfaced to the ui`() = runTest {
        val viewModel = viewModel()
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }

        viewModel.onWebMessage("""{"type":"probe"}""")

        assertEquals(WebViewEvent.Notify("""H5 消息：{"type":"probe"}"""), event.await())
    }

    @Test
    fun `h5 close request is forwarded`() = runTest {
        val viewModel = viewModel()
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }

        viewModel.onWebClose()

        assertEquals(WebViewEvent.ClosePage, event.await())
    }

    @Test
    fun `native js call is forwarded to the page`() = runTest {
        val viewModel = viewModel()
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }

        viewModel.callJs("document.title")

        assertEquals(WebViewEvent.EvaluateJs("document.title"), event.await())
    }

    @Test
    fun `js result is reported as a tip`() = runTest {
        val viewModel = viewModel()
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }

        viewModel.onJsResult("\"百度一下，你就知道\"")

        assertEquals(WebViewEvent.Notify("JS 返回：百度一下，你就知道"), event.await())
    }
}
