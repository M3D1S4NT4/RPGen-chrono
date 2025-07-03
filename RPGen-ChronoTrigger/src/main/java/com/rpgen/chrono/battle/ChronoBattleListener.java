package com.rpgen.chrono.battle;

import com.rpgen.chrono.entity.ChronoEntity;
import com.rpgen.chrono.entity.ChronoMove;
import com.rpgen.chrono.entity.ChronoStatus;

public interface ChronoBattleListener {
    void onBattleStart();
    void onBattleEnd(boolean playerVictory);
    void onEntityReady(ChronoEntity entity);
    void onActionExecuted(ChronoEntity actor, ChronoMove move, ChronoEntity target);
    void onStatusChanged(ChronoEntity entity, ChronoStatus status, boolean applied);
} 