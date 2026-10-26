package com.spe.calculator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    private final Calculator calc = new Calculator();

    @Test
    void addsNumbers() {
        assertEquals(5.0, calc.add(2, 3));
        assertEquals(-1.0, calc.add(2, -3));
        assertEquals(0.3, calc.add(0.1, 0.2), 1e-9);
    }

    @Test
    void subtractsNumbers() {
        assertEquals(-1.0, calc.subtract(2, 3));
        assertEquals(5.0, calc.subtract(2, -3));
    }

    @Test
    void multipliesNumbers() {
        assertEquals(6.0, calc.multiply(2, 3));
        assertEquals(0.0, calc.multiply(0, 99));
        assertEquals(-6.0, calc.multiply(2, -3));
    }

    @Test
    void dividesNumbers() {
        assertEquals(2.5, calc.divide(5, 2));
        assertEquals(-2.0, calc.divide(6, -3));
    }

    @Test
    void divideByZeroThrows() {
        ArithmeticException ex = assertThrows(ArithmeticException.class, () -> calc.divide(1, 0));
        assertEquals("Division by zero is not allowed", ex.getMessage());
    }
}
