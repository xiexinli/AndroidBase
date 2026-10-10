package com.development.base

import com.development.base.core.common.ApiResult
import com.development.base.core.network.model.ApiResponse
import com.development.base.core.network.model.BizCode
import com.development.base.core.network.model.toApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiResponseTest {

    @Test fun `success envelope maps to success`() {
        val result = ApiResponse(state = BizCode.SUCCESS, data = 42).toApiResult()
        assertEquals(ApiResult.Success(42), result)
    }

    @Test fun `error envelope maps to error carrying bizCode and message`() {
        val result = ApiResponse<Int>(state = BizCode.INVALID_SESSION, message = "登录已失效").toApiResult()
        assertTrue(result is ApiResult.Error)
        result as ApiResult.Error
        assertEquals(BizCode.INVALID_SESSION, result.bizCode)
        assertEquals("登录已失效", result.message)
    }

    @Test fun `success envelope with null data is treated as error`() {
        val result = ApiResponse<Int>(state = BizCode.SUCCESS).toApiResult()
        assertTrue(result is ApiResult.Error)
    }

    @Test fun `transform is applied to data on success`() {
        val result = ApiResponse(state = BizCode.SUCCESS, data = "hello").toApiResult { it.length }
        assertEquals(ApiResult.Success(5), result)
    }
}
