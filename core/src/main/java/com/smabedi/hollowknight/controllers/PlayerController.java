package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class PlayerController {
    private final Knight player;

    public PlayerController(Knight player) {
        this.player = player;
    }

    public void handleInput(float dt) {
        if (player.b2body == null) return;

        Vector2 vel = player.b2body.getLinearVelocity();
        float targetVelocity = 0;

        if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT))) {
            targetVelocity = -Constants.Knight.MAX_SPEED;
        } else if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT))) {
            targetVelocity = Constants.Knight.MAX_SPEED;
        }

        player.b2body.setLinearVelocity(targetVelocity, vel.y);

        if (Gdx.input.isKeyJustPressed(GameSettings.getKey(GameSettings.KEY_JUMP))) {
            if (player.isGrounded) {
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH),
                    player.b2body.getWorldCenter(),
                    true);
            }
        }
    }
}
