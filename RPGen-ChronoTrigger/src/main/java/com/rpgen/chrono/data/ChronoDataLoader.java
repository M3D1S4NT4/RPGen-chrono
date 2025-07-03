package com.rpgen.chrono.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rpgen.chrono.entity.ChronoEntity;
import com.rpgen.chrono.entity.ChronoEquipment;
import com.rpgen.chrono.entity.ChronoMove;
import com.rpgen.chrono.entity.ChronoStatus;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ChronoDataLoader {
    private final Gson gson = new Gson();

    public List<ChronoEntity> loadCharacters(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            List<ChronoEntity> entities = gson.fromJson(reader, new TypeToken<List<ChronoEntity>>(){}.getType());
            if (entities != null) {
                for (ChronoEntity e : entities) {
                    e.initializeATBFromStats();
                }
            }
            return entities;
        } catch (Exception e) {
            System.err.println("Error cargando personajes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ChronoEntity> loadEnemies(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            // Leer como Object para detectar el formato
            Object raw = gson.fromJson(reader, Object.class);
            List<ChronoEntity> result = new ArrayList<>();
            if (raw instanceof List) {
                List<?> list = (List<?>) raw;
                if (!list.isEmpty() && list.get(0) instanceof java.util.Map) {
                    // Formato: [{ "enemies_1000_ad": [ ... ] }]
                    java.util.Map<?,?> map = (java.util.Map<?,?>) list.get(0);
                    Object enemiesArr = map.values().stream().findFirst().orElse(null);
                    if (enemiesArr instanceof List) {
                        for (Object obj : (List<?>) enemiesArr) {
                            ChronoEntity ent = mapEnemyJsonToEntity(obj);
                            result.add(ent);
                        }
                        return result;
                    }
                } else {
                    // Formato: [ {...}, {...} ]
                    for (Object obj : list) {
                        ChronoEntity ent = mapEnemyJsonToEntity(obj);
                        result.add(ent);
                    }
                    return result;
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error cargando enemigos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Mapea un objeto JSON de enemigo a ChronoEntity
    private ChronoEntity mapEnemyJsonToEntity(Object obj) {
        ChronoEntity ent = new ChronoEntity();
        if (obj instanceof java.util.Map) {
            java.util.Map<?,?> map = (java.util.Map<?,?>) obj;
            // Nombre
            if (map.get("name") != null) ent.setCharacter(map.get("name").toString());
            // HP
            if (map.get("hp") != null) try { ent.setHp(Integer.parseInt(map.get("hp").toString())); } catch (Exception ignored) {}
            // MP
            if (map.get("mp") != null) try { ent.setMp(Integer.parseInt(map.get("mp").toString())); } catch (Exception ignored) {}
            // Velocidad
            if (map.get("speed") != null) try { ent.setSpeed(Integer.parseInt(map.get("speed").toString())); } catch (Exception ignored) {}
        }
        ent.setStatus(null);
        return ent;
    }

    public List<ChronoMove> loadMoves(String filePath) {
        List<ChronoMove> allMoves = new ArrayList<>();
        try (Reader reader = new FileReader(filePath)) {
            com.google.gson.JsonArray rootArray = gson.fromJson(reader, com.google.gson.JsonArray.class);
            if (rootArray.size() == 0) return allMoves;
            com.google.gson.JsonObject root = rootArray.get(0).getAsJsonObject();
            com.google.gson.JsonObject techs = root.getAsJsonObject("techs");
            
            // Cargar técnicas individuales
            com.google.gson.JsonObject singleTechs = techs.getAsJsonObject("singleTechs");
            for (String character : singleTechs.keySet()) {
                com.google.gson.JsonArray techArray = singleTechs.getAsJsonArray(character);
                for (com.google.gson.JsonElement elem : techArray) {
                    com.google.gson.JsonObject obj = elem.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : null;
                    String type = obj.has("element") && !obj.get("element").isJsonNull() ? obj.get("element").getAsString() : null;
                    int power = obj.has("TP") ? obj.get("TP").getAsInt() : (obj.has("tp") ? obj.get("tp").getAsInt() : 0);
                    int cost = obj.has("MP") ? obj.get("MP").getAsInt() : 0;
                    String target = obj.has("target") ? obj.get("target").getAsString() : "One enemy";
                    //System.out.println("[DEBUG] Cargando técnica individual: " + name + ", power=" + power + ", cost=" + cost);
                    ChronoMove move = new ChronoMove(name, type, power, cost, target);
                    move.setType("single");
                    move.setOwner(character);
                    allMoves.add(move);
                }
            }
            
            // Cargar técnicas dobles
            if (techs.has("doubleTechs")) {
                com.google.gson.JsonArray doubleTechs = techs.getAsJsonArray("doubleTechs");
                for (com.google.gson.JsonElement elem : doubleTechs) {
                    com.google.gson.JsonObject obj = elem.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : null;
                    String target = obj.has("target") ? obj.get("target").getAsString() : "One enemy";
                    String description = obj.has("description") ? obj.get("description").getAsString() : "";
                    int power = obj.has("TP") ? obj.get("TP").getAsInt() : (obj.has("tp") ? obj.get("tp").getAsInt() : 0);
                    int cost = 0; // Las técnicas dobles pueden tener coste combinado, pero aquí solo se pone 0 por defecto
                    //System.out.println("[DEBUG] Cargando técnica doble: " + name + ", power=" + power + ", cost=" + cost);
                    List<String> characters = new ArrayList<>();
                    if (obj.has("characters")) {
                        for (com.google.gson.JsonElement ch : obj.getAsJsonArray("characters")) {
                            characters.add(ch.getAsString());
                        }
                    }
                    ChronoMove move = new ChronoMove(name, null, power, cost, target);
                    move.setType("double");
                    move.setRequiredCharacters(characters);
                    move.setDescription(description);
                    allMoves.add(move);
                }
            }
            
            // Cargar técnicas triples
            if (techs.has("tripleTechs")) {
                com.google.gson.JsonArray tripleTechs = techs.getAsJsonArray("tripleTechs");
                for (com.google.gson.JsonElement elem : tripleTechs) {
                    com.google.gson.JsonObject obj = elem.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : null;
                    String target = obj.has("target") ? obj.get("target").getAsString() : "One enemy";
                    String description = obj.has("description") ? obj.get("description").getAsString() : "";
                    int power = obj.has("TP") ? obj.get("TP").getAsInt() : (obj.has("tp") ? obj.get("tp").getAsInt() : 0);
                    int cost = 0; // Las técnicas triples pueden tener coste combinado, pero aquí solo se pone 0 por defecto
                    //System.out.println("[DEBUG] Cargando técnica triple: " + name + ", power=" + power + ", cost=" + cost);
                    List<String> characters = new ArrayList<>();
                    if (obj.has("characters")) {
                        for (com.google.gson.JsonElement ch : obj.getAsJsonArray("characters")) {
                            characters.add(ch.getAsString());
                        }
                    }
                    ChronoMove move = new ChronoMove(name, null, power, cost, target);
                    move.setType("triple");
                    move.setRequiredCharacters(characters);
                    move.setDescription(description);
                    allMoves.add(move);
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando técnicas: " + e.getMessage());
        }
        return allMoves;
    }

    public List<ChronoEquipment> loadEquipment(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<ChronoEquipment>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Error cargando equipo: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> loadWeapons(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Error cargando armas: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> loadArmors(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Error cargando armaduras: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> loadHelmets(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Error cargando cascos: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public List<Map<String, Object>> loadAccessories(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Error cargando accesorios: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ChronoStatus> loadStatuses(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, new TypeToken<List<ChronoStatus>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Error cargando estados: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, List<ChronoMove>> loadSingleTechsByCharacter(String filePath) {
        Map<String, List<ChronoMove>> techsByChar = new HashMap<>();
        try (Reader reader = new FileReader(filePath)) {
            com.google.gson.JsonArray rootArray = gson.fromJson(reader, com.google.gson.JsonArray.class);
            if (rootArray.size() == 0) return techsByChar;
            com.google.gson.JsonObject root = rootArray.get(0).getAsJsonObject();
            com.google.gson.JsonObject techs = root.getAsJsonObject("techs");
            com.google.gson.JsonObject singleTechs = techs.getAsJsonObject("singleTechs");
            for (String character : singleTechs.keySet()) {
                List<ChronoMove> moves = new ArrayList<>();
                com.google.gson.JsonArray techArray = singleTechs.getAsJsonArray(character);
                for (com.google.gson.JsonElement elem : techArray) {
                    com.google.gson.JsonObject obj = elem.getAsJsonObject();
                    String name = obj.has("name") ? obj.get("name").getAsString() : null;
                    String type = obj.has("element") && !obj.get("element").isJsonNull() ? obj.get("element").getAsString() : null;
                    int power = obj.has("TP") ? obj.get("TP").getAsInt() : (obj.has("tp") ? obj.get("tp").getAsInt() : 0);
                    int cost = obj.has("MP") ? obj.get("MP").getAsInt() : 0;
                    String target = obj.has("target") ? obj.get("target").getAsString() : "One enemy";
                    //System.out.println("[DEBUG] Cargando técnica individual: " + name + ", power=" + power + ", cost=" + cost);
                    ChronoMove move = new ChronoMove(name, type, power, cost, target);
                    move.setType("single");
                    move.setOwner(character);
                    moves.add(move);
                }
                techsByChar.put(character, moves);
            }
        } catch (Exception e) {
            System.err.println("Error cargando técnicas individuales: " + e.getMessage());
        }
        return techsByChar;
    }
} 