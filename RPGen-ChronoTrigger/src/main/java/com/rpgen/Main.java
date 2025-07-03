package com.rpgen;

import com.rpgen.pokemon.web.PokemonBattleServer;
import com.rpgen.pokemon.web.PokemonConfigServer;
import com.rpgen.pokemon.web.PokemonServer;
import com.rpgen.chrono.web.ChronoBattleServer;
import com.rpgen.core.web.BattleServer;

import spark.Spark;

public class Main {
    public static void main(String[] args) {
        // Configurar el puerto
        Spark.port(4567);
        
        // Configurar el directorio de archivos estáticos
        Spark.staticFiles.location("/public");
        
        // Inicializar los servidores
        System.out.println("Iniciando servidores...");
        
        // Inicializar primero el servidor de configuración de Pokémon
        PokemonConfigServer pokemonConfigServer = new PokemonConfigServer();
        pokemonConfigServer.init();
        System.out.println("Servidor de configuración de Pokémon iniciado");
        
        PokemonBattleServer pokemonBattleServer = new PokemonBattleServer();
        pokemonBattleServer.init();
        System.out.println("Servidor de batalla de Pokémon iniciado");
        
        BattleServer battleServer = new BattleServer();
        battleServer.init();
        System.out.println("Servidor de batalla general iniciado");
        
        PokemonServer pokemonServer = new PokemonServer();
        pokemonServer.init();
        System.out.println("Servidor de Pokémon iniciado");
        
        ChronoBattleServer chronoServer = new ChronoBattleServer();
        chronoServer.init();
        System.out.println("Servidor de batalla de Chrono iniciado");
        
        // Configurar manejo de errores
        Spark.exception(Exception.class, (exception, request, response) -> {
            System.err.println("Error en el servidor: " + exception.getMessage());
            System.err.println("Ruta: " + request.pathInfo());
            System.err.println("Método: " + request.requestMethod());
            exception.printStackTrace();
            response.status(500);
            response.body("Error interno del servidor: " + exception.getMessage());
        });
        
        // Configurar manejo de rutas no encontradas
        Spark.notFound((request, response) -> {
            System.err.println("Ruta no encontrada: " + request.pathInfo());
            System.err.println("Método: " + request.requestMethod());
            response.status(404);
            return "Ruta no encontrada: " + request.pathInfo();
        });
        
        System.out.println("Servidor iniciado en http://localhost:4567");
        System.out.println("Rutas disponibles:");
        System.out.println("  - /api/pokemon/natures");
        System.out.println("  - /api/pokemon/items");
        System.out.println("  - /api/pokemon/:id/abilities");
        System.out.println("  - /api/pokemon/:id/configure");
        System.out.println("  - /api/pokemon/:id/stats");
    }
} 