package com.rpgen.chrono.web;

import static spark.Spark.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rpgen.chrono.battle.ChronoBattleEngine;
import com.rpgen.chrono.data.ChronoDataLoader;
import com.rpgen.chrono.entity.ChronoEntity;
import com.rpgen.chrono.entity.ChronoEquipment;
import com.rpgen.chrono.entity.ChronoMove;
import com.rpgen.chrono.entity.ChronoStatus;
import com.rpgen.chrono.entity.ChronoEntity.StatByLevel;

import java.util.*;

public class ChronoBattleServer {
    private final Gson gson;
    private final ChronoDataLoader dataLoader;
    private ChronoBattleEngine battleEngine;
    private List<ChronoEntity> characters;
    private List<ChronoEntity> enemies;
    private List<ChronoMove> moves;
    private List<ChronoEquipment> equipment;
    private List<ChronoStatus> statuses;
    private List<ChronoEntity> currentAllies;
    private List<ChronoEntity> currentEnemies;
    private boolean battleActive;

    public ChronoBattleServer() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dataLoader = new ChronoDataLoader();
        this.characters = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.moves = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.statuses = new ArrayList<>();
        this.currentAllies = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
        this.battleEngine = new ChronoBattleEngine();
        this.battleActive = false;
        loadAllData();
    }

    private void loadAllData() {
        characters = dataLoader.loadCharacters("src/main/resources/public/chrono-json/chrono-characters.json");
        enemies = dataLoader.loadEnemies("src/main/resources/public/chrono-json/enemies/1000AD.json"); // Ejemplo
        moves = dataLoader.loadMoves("src/main/resources/public/chrono-json/chrono-techs.json");
        //System.out.println("[LOAD] Técnicas cargadas: " + (moves != null ? moves.size() : 0));
        //if (moves != null) System.out.println("[LOAD] Nombres de técnicas: " + moves.stream().map(m -> m.getName()).toList());
        equipment = dataLoader.loadEquipment("src/main/resources/public/chrono-json/chrono-weapons.json");
        statuses = dataLoader.loadStatuses("src/main/resources/public/chrono-json/chrono-status.json");
    }

    public void init() {
        get("/api/chrono/characters", (req, res) -> {
            res.type("application/json");
            return gson.toJson(characters);
        });
        
        get("/api/chrono/equipment/weapons", (req, res) -> {
            res.type("application/json");
            try {
                List<Map<String, Object>> weapons = dataLoader.loadWeapons("src/main/resources/public/chrono-json/chrono-weapons.json");
                return gson.toJson(weapons);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Error cargando armas: " + e.getMessage()));
            }
        });
        
        get("/api/chrono/equipment/armors", (req, res) -> {
            res.type("application/json");
            try {
                List<Map<String, Object>> armors = dataLoader.loadArmors("src/main/resources/public/chrono-json/chrono-armor.json");
                return gson.toJson(armors);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Error cargando armaduras: " + e.getMessage()));
            }
        });
        
        get("/api/chrono/equipment/helmets", (req, res) -> {
            res.type("application/json");
            try {
                List<Map<String, Object>> helmets = dataLoader.loadHelmets("src/main/resources/public/chrono-json/chrono-helmets.json");
                return gson.toJson(helmets);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Error cargando cascos: " + e.getMessage()));
            }
        });
        
        get("/api/chrono/equipment/accessories", (req, res) -> {
            res.type("application/json");
            try {
                List<Map<String, Object>> accessories = dataLoader.loadAccessories("src/main/resources/public/chrono-json/chrono-accesories.json");
                return gson.toJson(accessories);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Error cargando accesorios: " + e.getMessage()));
            }
        });
        get("/api/chrono/enemies", (req, res) -> {
            res.type("application/json");
            return gson.toJson(enemies);
        });
        get("/api/chrono/moves", (req, res) -> {
            res.type("application/json");
            String character = req.queryParams("character");
            if (character != null && !character.isEmpty()) {
                // Cargar solo las técnicas individuales de ese personaje
                Map<String, List<ChronoMove>> techsByChar = dataLoader.loadSingleTechsByCharacter("src/main/resources/public/chrono-json/chrono-techs.json");
                List<ChronoMove> result = techsByChar.getOrDefault(character, new ArrayList<>());
                // Añadir técnicas dobles y triples en las que participa
                List<ChronoMove> allMoves = moves;
                for (ChronoMove move : allMoves) {
                    if (("double".equals(move.getMoveType()) || "triple".equals(move.getMoveType())) && move.getRequiredCharacters() != null && move.getRequiredCharacters().contains(character)) {
                        result.add(move);
                    }
                }
                return gson.toJson(result);
            }
            return gson.toJson(moves);
        });
        get("/api/chrono/equipment", (req, res) -> {
            res.type("application/json");
            return gson.toJson(equipment);
        });
        get("/api/chrono/statuses", (req, res) -> {
            res.type("application/json");
            return gson.toJson(statuses);
        });

        post("/api/chrono/battle/start", (req, res) -> {
            res.type("application/json");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = gson.fromJson(req.body(), Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alliesData = (List<Map<String, Object>>) data.get("allies");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> enemiesData = (List<Map<String, Object>>) data.get("enemies");
            String epoca = data.get("epoca") != null ? data.get("epoca").toString() : "1000AD";
            // Mapeo de época a archivo
            Map<String, String> epocaToFile = Map.of(
                "1000AD", "1000AD.json",
                "600AD", "600AD.json",
                "2300AD", "2300AD.json",
                "12000BC", "12000BC.json",
                "65000000BC", "65000000BC.json",
                "BlackOmen", "BlackOmen.json",
                "lavos-forms", "lavos-forms.json",
                "spekkio-forms", "spekkio-forms.json"
            );
            String file = epocaToFile.getOrDefault(epoca, "1000AD.json");
            // Cargar enemigos de la época seleccionada
            enemies = dataLoader.loadEnemies("src/main/resources/public/chrono-json/enemies/" + file);
            if (alliesData == null || enemiesData == null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Se requieren ambos equipos"));
            }
            currentAllies = new ArrayList<>();
            currentEnemies = new ArrayList<>();
            for (Object allyObj : alliesData) {
                if (allyObj instanceof Map<?, ?>) {
                    Map<?, ?> rawMap = (Map<?, ?>) allyObj;
                    Map<String, Object> allyData = new HashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            allyData.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                    Object nameObj = allyData.get("name");
                    if (nameObj == null) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "Falta el campo 'name' en un aliado."));
                    }
                    ChronoEntity ent = findEntityByName(nameObj.toString(), characters);
                    if (ent != null) {
                        ChronoEntity clonedEnt = cloneEntity(ent);
                        Object hpObj = allyData.get("hp");
                        Object mpObj = allyData.get("mp");
                        clonedEnt.setHp(hpObj != null ? (int) Double.parseDouble(hpObj.toString()) : 1);
                        clonedEnt.setMp(mpObj != null ? (int) Double.parseDouble(mpObj.toString()) : 0);
                        clonedEnt.setStatus(null);
                        if (allyData.get("level") != null) {
                            int lvl = (int) Double.parseDouble(allyData.get("level").toString());
                            clonedEnt.setLevel(lvl);
                        }
                        if (allyData.get("weapon") != null) {
                            ChronoEquipment weapon = findEquipmentByName(allyData.get("weapon").toString(), "weapon");
                            if (weapon != null) clonedEnt.equipWeapon(weapon);
                        }
                        if (allyData.get("armor") != null) {
                            ChronoEquipment armor = findEquipmentByName(allyData.get("armor").toString(), "armor");
                            if (armor != null) clonedEnt.equipArmor(armor);
                        }
                        if (allyData.get("helmet") != null) {
                            ChronoEquipment helmet = findEquipmentByName(allyData.get("helmet").toString(), "helmet");
                            if (helmet != null) clonedEnt.equipHelmet(helmet);
                        }
                        if (allyData.get("accessory") != null) {
                            ChronoEquipment accessory = findEquipmentByName(allyData.get("accessory").toString(), "accessory");
                            if (accessory != null) clonedEnt.equipAccessory(accessory);
                        }
                        currentAllies.add(clonedEnt);
                    }
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Formato de aliados incorrecto. Se esperaban objetos, no índices."));
                }
            }
            for (Object enemyObj : enemiesData) {
                if (enemyObj instanceof Map<?, ?>) {
                    Map<?, ?> rawMap = (Map<?, ?>) enemyObj;
                    Map<String, Object> enemyData = new HashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            enemyData.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                    Object nameObj = enemyData.get("name");
                    if (nameObj == null) {
                        res.status(400);
                        return gson.toJson(Map.of("error", "Falta el campo 'name' en un enemigo."));
                    }
                    ChronoEntity ent = findEntityByName(nameObj.toString(), enemies);
                    if (ent != null) {
                        // Clonar la entidad para no modificar la original
                        ChronoEntity clonedEnt = cloneEntity(ent);
                        Object hpObj = enemyData.get("hp");
                        Object mpObj = enemyData.get("mp");
                        clonedEnt.setHp(hpObj != null ? (int) Double.parseDouble(hpObj.toString()) : 1);
                        clonedEnt.setMp(mpObj != null ? (int) Double.parseDouble(mpObj.toString()) : 0);
                        clonedEnt.setStatus(null);
                        currentEnemies.add(clonedEnt);
                    }
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("error", "Formato de enemigos incorrecto. Se esperaban objetos, no índices."));
                }
            }
            // --- CORRECCIÓN: Siempre crear una nueva instancia de ChronoBattleEngine antes de inicializar ---
            battleEngine = new ChronoBattleEngine();
            battleEngine.initialize(currentAllies, currentEnemies);
            battleEngine.startBattle();
            battleActive = true;
            return gson.toJson(Map.of("status", "battle started"));
        });

        get("/api/chrono/battle/state", (req, res) -> {
            res.type("application/json");
            if (!battleActive || battleEngine == null) {
                return gson.toJson(Map.of("status", "no battle"));
            }
            // Devolver estado simple: aliados, enemigos, ATB de cada uno
            List<Map<String, Object>> alliesState = new ArrayList<>();
            for (ChronoEntity e : currentAllies) {
                Map<String, Object> allyData = new HashMap<>();
                allyData.put("name", e.getCharacter());
                allyData.put("atb", e.getAtbCounter());
                allyData.put("atbMax", e.getAtbMax());
                allyData.put("canAct", e.canAct());
                allyData.put("hp", e.getHp());
                allyData.put("mp", e.getMp());
                
                // Añadir estadísticas si están disponibles
                if (e.getStatsByLevel() != null && !e.getStatsByLevel().isEmpty()) {
                    ChronoEntity.StatByLevel stats = e.getStatsByLevel().get(0);
                    Map<String, Object> statsMap = new HashMap<>();
                    statsMap.put("strength", stats.getStrength());
                    statsMap.put("stamina", stats.getStamina());
                    statsMap.put("accuracy", stats.getAccuracy());
                    statsMap.put("evasion", stats.getEvasion());
                    statsMap.put("magic", stats.getMagic());
                    statsMap.put("magicDefense", stats.getMagicDefense());
                    statsMap.put("speed", stats.getSpeed());
                    allyData.put("stats", statsMap);
                }
                alliesState.add(allyData);
            }
            List<Map<String, Object>> enemiesState = new ArrayList<>();
            for (ChronoEntity e : currentEnemies) {
                if (e.getHp() <= 0) continue; // Eliminar enemigos derrotados
                Map<String, Object> enemyData = new HashMap<>();
                enemyData.put("name", e.getCharacter());
                enemyData.put("atb", e.getAtbCounter());
                enemyData.put("atbMax", e.getAtbMax());
                enemyData.put("canAct", e.canAct());
                enemyData.put("hp", e.getHp());
                enemyData.put("mp", e.getMp());
                
                // Añadir estadísticas si están disponibles
                if (e.getStatsByLevel() != null && !e.getStatsByLevel().isEmpty()) {
                    ChronoEntity.StatByLevel stats = e.getStatsByLevel().get(0);
                    Map<String, Object> statsMap = new HashMap<>();
                    statsMap.put("strength", stats.getStrength());
                    statsMap.put("stamina", stats.getStamina());
                    statsMap.put("accuracy", stats.getAccuracy());
                    statsMap.put("evasion", stats.getEvasion());
                    statsMap.put("magic", stats.getMagic());
                    statsMap.put("magicDefense", stats.getMagicDefense());
                    statsMap.put("speed", stats.getSpeed());
                    enemyData.put("stats", statsMap);
                }
                enemiesState.add(enemyData);
            }
            return gson.toJson(Map.of(
                "allies", alliesState,
                "enemies", enemiesState
            ));
        });

        post("/api/chrono/battle/action", (req, res) -> {
            res.type("application/json");
            if (!battleActive || battleEngine == null) {
                return gson.toJson(Map.of("error", "No hay batalla activa"));
            }
            Map<String, Object> data = gson.fromJson(req.body(), new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
            String actorName = (String) data.get("actor");
            String moveName = (String) data.get("move");
            String targetName = (String) data.get("target");
            ChronoEntity actor = findEntityByName(actorName, currentAllies, currentEnemies);
            ChronoEntity target = findEntityByName(targetName, currentAllies, currentEnemies);
            ChronoMove move = findMoveByName(moveName);
            if (actor == null || target == null || move == null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Actor, movimiento o objetivo no válido"));
            }
            battleEngine.performAction(actor, move, target);
            actor.resetATB();
            // Devolver el nuevo estado de batalla
            List<Map<String, Object>> alliesState = new ArrayList<>();
            for (ChronoEntity e : currentAllies) {
                alliesState.add(Map.of(
                    "name", e.getCharacter() != null ? e.getCharacter() : "",
                    "hp", e.getHp(),
                    "mp", e.getMp(),
                    "status", e.getStatus() != null && e.getStatus().getName() != null ? e.getStatus().getName() : "",
                    "canAct", e.canAct()
                ));
            }
            List<Map<String, Object>> enemiesState = new ArrayList<>();
            String epocaActual = null;
            if (enemies != null && !enemies.isEmpty()) {
                epocaActual = "1000AD";
            } else {
                epocaActual = "1000AD";
            }
            for (ChronoEntity e : currentEnemies) {
                enemiesState.add(Map.of(
                    "name", e.getCharacter() != null ? e.getCharacter() : "",
                    "hp", e.getHp(),
                    "mp", e.getMp(),
                    "status", e.getStatus() != null && e.getStatus().getName() != null ? e.getStatus().getName() : "",
                    "canAct", e.canAct(),
                    "epoca", epocaActual
                ));
            }
            List<String> messages = battleEngine != null && battleEngine.getMessages() != null ? battleEngine.getMessages() : List.of();
            boolean isPlayerTurn = alliesState.stream().anyMatch(a -> Boolean.TRUE.equals(a.get("canAct")));
            return gson.toJson(Map.of(
                "allies", alliesState,
                "enemies", enemiesState,
                "messages", messages,
                "isPlayerTurn", isPlayerTurn
            ));
        });

        get("/api/chrono/battle/status", (req, res) -> {
            res.type("application/json");
            if (!battleActive || battleEngine == null) {
                return gson.toJson(Map.of("status", "no battle"));
            }
            List<Map<String, Object>> alliesState = new ArrayList<>();
            for (ChronoEntity e : currentAllies) {
                alliesState.add(Map.of(
                    "name", e.getCharacter() != null ? e.getCharacter() : "",
                    "hp", e.getHp(),
                    "mp", e.getMp(),
                    "status", e.getStatus() != null && e.getStatus().getName() != null ? e.getStatus().getName() : "",
                    "canAct", e.canAct(),
                    "atb", e.getAtbCounter(),
                    "atbMax", e.getAtbMax()
                ));
            }
            List<Map<String, Object>> enemiesState = new ArrayList<>();
            // Determinar la época actual (la última usada para cargar enemigos)
            String epocaActual = null;
            if (enemies != null && !enemies.isEmpty()) {
                // Buscar la época según el archivo cargado
                // (Puedes guardar la época en una variable de instancia cuando cargas los enemigos)
                epocaActual = (String) req.session().attribute("epoca");
                if (epocaActual == null) epocaActual = "1000AD";
            } else {
                epocaActual = "1000AD";
            }
            for (ChronoEntity e : currentEnemies) {
                enemiesState.add(Map.of(
                    "name", e.getCharacter() != null ? e.getCharacter() : "",
                    "hp", e.getHp(),
                    "mp", e.getMp(),
                    "status", e.getStatus() != null && e.getStatus().getName() != null ? e.getStatus().getName() : "",
                    "canAct", e.canAct(),
                    "epoca", epocaActual,
                    "atb", e.getAtbCounter(),
                    "atbMax", e.getAtbMax()
                ));
            }
            // Mensajes de batalla (puedes mejorar esto según tu lógica)
            List<String> messages = battleEngine != null && battleEngine.getMessages() != null ? battleEngine.getMessages() : List.of();
            boolean isPlayerTurn = alliesState.stream().anyMatch(a -> Boolean.TRUE.equals(a.get("canAct")));
            return gson.toJson(Map.of(
                "allies", alliesState,
                "enemies", enemiesState,
                "messages", messages,
                "isPlayerTurn", isPlayerTurn
            ));
        });

        post("/api/chrono/battle/reset", (req, res) -> {
            res.type("application/json");
            // --- CORRECCIÓN: Crear una nueva instancia de ChronoBattleEngine para evitar null pointer ---
            battleEngine = new ChronoBattleEngine();
            currentAllies = new ArrayList<>();
            currentEnemies = new ArrayList<>();
            battleActive = false;
            return gson.toJson(Map.of("status", "reset"));
        });

        // Endpoint para servir archivos de enemigos por época
        get("/chrono-json/enemies/:file", (req, res) -> {
            res.type("application/json");
            String file = req.params(":file");
            // Validar nombre de archivo para evitar path traversal
            if (!file.matches("[\\w\\-]+\\.json")) {
                res.status(400);
                return gson.toJson(Map.of("error", "Archivo no permitido"));
            }
            java.nio.file.Path path = java.nio.file.Paths.get("chrono-json/enemies", file);
            if (!java.nio.file.Files.exists(path)) {
                res.status(404);
                return gson.toJson(Map.of("error", "Archivo no encontrado"));
            }
            try {
                String content = java.nio.file.Files.readString(path);
                return content;
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Error al leer archivo: " + e.getMessage()));
            }
        });

        // --- NUEVO: Endpoint para usar objeto en combate ---
        post("/api/chrono/battle/use-item", (req, res) -> {
            res.type("application/json");
            if (!battleActive || battleEngine == null) {
                return gson.toJson(Map.of("error", "No hay batalla activa"));
            }
            Map<String, Object> data = gson.fromJson(req.body(), new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType());
            String userName = (String) data.get("user");
            String itemName = (String) data.get("item");
            String targetName = (String) data.get("target");
            ChronoEntity user = findEntityByName(userName, currentAllies, currentEnemies);
            ChronoEntity target = findEntityByName(targetName, currentAllies, currentEnemies);
            if (user == null || target == null) {
                res.status(400);
                return gson.toJson(Map.of("error", "Usuario u objetivo no válido"));
            }
            java.nio.file.Path itemsPath = java.nio.file.Paths.get("src/main/resources/public/chrono-json/chrono-items.json");
            final List<Map<String, Object>> itemsList;
            try {
                String itemsJson = java.nio.file.Files.readString(itemsPath);
                itemsList = gson.fromJson(itemsJson, new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType());
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "No se pudo leer la base de datos de objetos: " + e.getMessage()));
            }
            String msg = battleEngine.useItemOnTarget(itemName, user, target, currentAllies, (iname) -> itemsList.stream().filter(i -> iname.equalsIgnoreCase((String)i.get("name"))).findFirst().orElse(null));
            if (battleEngine.getMessages() != null) battleEngine.getMessages().add(msg);
            // Devolver el nuevo estado de batalla
            List<Map<String, Object>> alliesState = new ArrayList<>();
            for (ChronoEntity e : currentAllies) {
                alliesState.add(Map.of(
                    "name", e.getCharacter() != null ? e.getCharacter() : "",
                    "hp", e.getHp(),
                    "mp", e.getMp(),
                    "status", e.getStatus() != null && e.getStatus().getName() != null ? e.getStatus().getName() : "",
                    "canAct", e.canAct()
                ));
            }
            List<Map<String, Object>> enemiesState = new ArrayList<>();
            String epocaActual = null;
            if (enemies != null && !enemies.isEmpty()) {
                epocaActual = "1000AD";
            } else {
                epocaActual = "1000AD";
            }
            for (ChronoEntity e : currentEnemies) {
                enemiesState.add(Map.of(
                    "name", e.getCharacter() != null ? e.getCharacter() : "",
                    "hp", e.getHp(),
                    "mp", e.getMp(),
                    "status", e.getStatus() != null && e.getStatus().getName() != null ? e.getStatus().getName() : "",
                    "canAct", e.canAct(),
                    "epoca", epocaActual
                ));
            }
            List<String> messages = battleEngine != null && battleEngine.getMessages() != null ? battleEngine.getMessages() : List.of();
            boolean isPlayerTurn = alliesState.stream().anyMatch(a -> Boolean.TRUE.equals(a.get("canAct")));
            return gson.toJson(Map.of(
                "allies", alliesState,
                "enemies", enemiesState,
                "messages", messages,
                "isPlayerTurn", isPlayerTurn
            ));
        });
    }

    @SafeVarargs
    private final ChronoEntity findEntityByName(String name, List<ChronoEntity>... lists) {
        String normalizedName = normalizeName(name);
        for (List<ChronoEntity> l : lists) {
            for (ChronoEntity e : l) {
                if (e.getCharacter() != null && normalizeName(e.getCharacter()).equals(normalizedName)) {
                    return e;
                }
            }
        }
        return null;
    }

    private ChronoMove findMoveByName(String name) {
        String normalizedName = normalizeName(name);
        for (ChronoMove m : moves) {
            if (m.getName() != null && normalizeName(m.getName()).equals(normalizedName)) {
                return m;
            }
        }
        return null;
    }

    // Normalizar nombres para comparación insensible a mayúsculas y espacios
    private String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().toLowerCase();
    }

    // Clonar una entidad para evitar modificar la original
    private ChronoEntity cloneEntity(ChronoEntity original) {
        ChronoEntity clone = new ChronoEntity();
        clone.setCharacter(original.getCharacter());
        clone.setStatsByLevel(original.getStatsByLevel());
        clone.setSpeed(original.getSpeed());
        clone.setHp(original.getHp());
        clone.setMp(original.getMp());
        clone.setStatus(original.getStatus());
        clone.setAtbMax(original.getAtbMax());
        clone.setAtbCounter(original.getAtbCounter());
        clone.setCanAct(original.canAct());
        clone.setLevel(original.getLevel());
        return clone;
    }

    private ChronoEquipment findEquipmentByName(String name, String type) {
        for (ChronoEquipment eq : equipment) {
            if (eq.getName() != null && eq.getType() != null &&
                eq.getName().equalsIgnoreCase(name) && eq.getType().equalsIgnoreCase(type)) {
                return eq;
            }
        }
        return null;
    }
} 