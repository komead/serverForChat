package org.example;

import org.example.controller.MainController;
import org.flywaydb.core.Flyway;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306";
        String user = "root";
        String password = "root";

        // Настройка Flyway
        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .schemas("chat")
                .createSchemas(true)
                .locations("classpath:db/migration")
                .load();

        // Запуск миграций
        flyway.migrate();

        System.out.println("Сервер запущен");

        new MainController().start();
    }
}
