package com.brenohbs.assistentefinanceiro.controller;

import com.brenohbs.assistentefinanceiro.model.CatalogoGastos;
import com.brenohbs.assistentefinanceiro.model.Categoria;
import com.brenohbs.assistentefinanceiro.model.ItemGasto;
import com.brenohbs.assistentefinanceiro.service.GoogleSheetsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoints só pra testar a integração com o Google Sheets manualmente
 * (via Postman, curl ou navegador) antes de plugar o WhatsApp.
 *
 * Exemplo de teste com curl, já na VM:
 *   curl http://localhost:8080/categorias
 *   curl -X POST http://localhost:8080/gastos \
 *        -H "Content-Type: application/json" \
 *        -d '{"categoria":"Despesas Essenciais","item":"Gasolina","valor":50}'
 */
@RestController
public class TesteController {

    private final GoogleSheetsService sheetsService;

    public TesteController(GoogleSheetsService sheetsService) {
        this.sheetsService = sheetsService;
    }

    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return CatalogoGastos.CATEGORIAS;
    }

    @PostMapping("/gastos")
    public String registrarGasto(@RequestBody RegistrarGastoRequest request) throws Exception {
        Optional<ItemGasto> item = CatalogoGastos.CATEGORIAS.stream()
                .filter(c -> c.nome().equalsIgnoreCase(request.categoria()))
                .flatMap(c -> c.itens().stream())
                .filter(i -> i.nome().equalsIgnoreCase(request.item()))
                .findFirst();

        if (item.isEmpty()) {
            return "Categoria/item não encontrado: " + request.categoria() + " > " + request.item();
        }

        double novoTotal = sheetsService.registrarGasto(item.get().linha(), request.valor());
        return "Gasto registrado! Novo total em '%s': R$ %.2f".formatted(item.get().nome(), novoTotal);
    }

    public record RegistrarGastoRequest(String categoria, String item, double valor) {
    }
}
