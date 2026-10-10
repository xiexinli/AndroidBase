package com.development.base

import com.development.base.core.common.ApiResult
import com.development.base.core.common.apiCall
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiResultTest {
    @Test fun `apiCall returns success value`() {
        val result = apiCall { 42 }
        assertEquals(ApiResult.Success(42), result)
    }
}
