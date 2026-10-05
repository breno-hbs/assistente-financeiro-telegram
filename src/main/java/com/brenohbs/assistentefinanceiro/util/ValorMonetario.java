package com.brenohbs.assistentefinanceiro.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Converte texto digitado pelo usuário em valor monetário e formata valores
 * no padrão brasileiro (R$ 1.156,26).
 *
 * Formatos aceitos: "1156,26", "1.156,26", "1156.26", "1156", "R$ 1.156,26".
 * Regras para o ponto:
 *   - se o texto tem vírgula, a vírgula é o decimal e os pontos são milhar;
 *   - se tem só pontos e eles separam grupos de 3 dígitos ("1.156", "1.000.000"),
 *     são separadores de milhar;
 *   - caso contrário, um único ponto é o separador decimal ("1156.26").
 * Rejeita vazio, texto, negativos e mais de 2 casas decimais.
 */
public final class ValorMonetario {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private static final Pattern BR_COM_VIRGULA = Pattern.compile("(\\d{1,3}(\\.\\d{3})+|\\d+),\\d{1,2}");
    private static final Pattern SO_MILHAR_COM_PONTO = Pattern.compile("\\d{1,3}(\\.\\d{3})+");
    private static final Pattern DECIMAL_COM_PONTO = Pattern.compile("\\d+\\.\\d{1,2}");
    private static final Pattern INTEIRO = Pattern.compile("\\d+");

    private ValorMonetario() {
    }

    public static Optional<BigDecimal> parse(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String limpo = texto.replace("R$", "").replace(" ", "").replace(" ", "").trim();
        if (limpo.isEmpty()) {
            return Optional.empty();
        }

        String normalizado;
        if (BR_COM_VIRGULA.matcher(limpo).matches()) {
            normalizado = limpo.replace(".", "").replace(",", ".");
        } else if (SO_MILHAR_COM_PONTO.matcher(limpo).matches()) {
            normalizado = limpo.replace(".", "");
        } else if (DECIMAL_COM_PONTO.matcher(limpo).matches() || INTEIRO.matcher(limpo).matches()) {
            normalizado = limpo;
        } else {
            return Optional.empty();
        }

        return Optional.of(new BigDecimal(normalizado).setScale(2, RoundingMode.UNNECESSARY));
    }

    /** Formata no padrão brasileiro, ex: "R$ 1.156,26" / "-R$ 50,00". */
    public static String formatar(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(PT_BR);
        return formato.format(valor).replace(' ', ' ');
    }
}
