package org.example;

import org.flywaydb.core.Flyway;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/chat";
        String user = "root";
        String password = "root";

        // Настройка Flyway
        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();

        // Запуск миграций
        flyway.migrate();

        new Server().start();
    }
}
