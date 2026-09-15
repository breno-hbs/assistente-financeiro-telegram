package com.brenohbs.assistentefinanceiro.config;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.util.Collections;

/**
 * Monta o cliente autenticado da Google Sheets API, usando a chave da
 * Service Account (ver application.properties: google.sheets.credentials-path).
 *
 * Lembrete: a planilha precisa estar compartilhada com o e-mail
 * "client_email" que está dentro desse arquivo JSON, com permissão de Editor.
 * Sem isso, toda chamada à API retorna erro 403.
 */
@Configuration
public class GoogleSheetsConfig {

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    @Bean
    public Sheets sheetsService() throws Exception {
        GoogleCredentials credentials;
        try (FileInputStream in = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(in)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        }

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Sheets.Builder(
                com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Assistente Financeiro")
                .build();
    }
}
