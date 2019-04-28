package com.github.zhurlik.option

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class WarningsTest {
    @Test
    void testMain() {
        assertEquals('[none, default, more, all]', Warnings.values().toArrayString())
    }
}
