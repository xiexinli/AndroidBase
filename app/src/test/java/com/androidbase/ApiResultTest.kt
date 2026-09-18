package com.androidbase

import com.androidbase.core.common.ApiResult
import com.androidbase.core.common.apiCall
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiResultTest {
    @Test fun `apiCall returns success value`() {
        val result = apiCall { 42 }
        assertEquals(ApiResult.Success(42), result)
    }
}
