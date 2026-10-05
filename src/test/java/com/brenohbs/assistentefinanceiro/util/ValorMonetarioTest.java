package com.brenohbs.assistentefinanceiro.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValorMonetarioTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "1156,26        | 1156.26",
            "1.156,26       | 1156.26",
            "1156.26        | 1156.26",
            "1156           | 1156.00",
            "0              | 0.00",
            "0,5            | 0.50",
            "45.9           | 45.90",
            "1.156          | 1156.00",
            "1.000.000,01   | 1000000.01",
            "R$ 1.156,26    | 1156.26",
            "'  1156,26  '  | 1156.26",
    })
    void aceitaFormatosValidos(String entrada, String esperado) {
        assertEquals(new BigDecimal(esperado), ValorMonetario.parse(entrada).orElseThrow());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "   ", "abc", "12a", "-10", "-1.156,26", "1,2,3", "1.15,26", "1156,267",
            "1156.267.1", "1,156.26", ",50", "10,", "R$", "1.1567"
    })
    void rejeitaEntradasInvalidas(String entrada) {
        assertTrue(ValorMonetario.parse(entrada).isEmpty(), () -> "deveria rejeitar: " + entrada);
    }

    @Test
    void formataNoPadraoBrasileiro() {
        assertEquals("R$ 1.156,26", ValorMonetario.formatar(new BigDecimal("1156.26")));
        assertEquals("R$ 0,00", ValorMonetario.formatar(BigDecimal.ZERO));
        assertEquals("-R$ 50,50", ValorMonetario.formatar(new BigDecimal("-50.5")));
    }
}
