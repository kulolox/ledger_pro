package com.example.back_end.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.back_end.common.Result;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void ok_withData_shouldReturn200() {
        Result<String> r = Result.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("成功", r.getMessage());
        assertEquals("hello", r.getData());
    }

    @Test
    void ok_withMessageAndData_shouldReturn200() {
        Result<Integer> r = Result.ok("操作成功", 42);
        assertEquals(200, r.getCode());
        assertEquals("操作成功", r.getMessage());
        assertEquals(42, r.getData());
    }

    @Test
    void ok_void_shouldReturn200WithNullData() {
        Result<Void> r = Result.ok();
        assertEquals(200, r.getCode());
        assertEquals("成功", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void fail_shouldReturnCustomCodeAndMessage() {
        Result<Void> r = Result.fail(401, "未登录");
        assertEquals(401, r.getCode());
        assertEquals("未登录", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    void setters_shouldUpdateFields() {
        Result<String> r = Result.ok("old");
        r.setCode(500);
        r.setMessage("error");
        r.setData("new");
        assertEquals(500, r.getCode());
        assertEquals("error", r.getMessage());
        assertEquals("new", r.getData());
    }
}
