package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.knight.Knight;

public class PlayerController {
    private final Knight player;
    private final Vector2 rayEnd = new Vector2();

    public PlayerController(Knight player) {
        this.player = player;
    }

    public void handleInput(float dt) {
        if (player.b2body == null) return;

        // --- TICK TIMERS ---
        if (player.dashCooldownTimer > 0) player.dashCooldownTimer -= dt;
        if (player.pogoDurationTimer > 0) player.pogoDurationTimer -= dt;

        int jumpKey = GameSettings.getKey(GameSettings.KEY_JUMP);
        int attackKey = GameSettings.getKey(GameSettings.KEY_ATTACK);
        int downKey = GameSettings.getKey(GameSettings.KEY_DOWN);
        int dashKey = GameSettings.getKey(GameSettings.KEY_DASH);

        // --- 1. DASH STATE MACHINE ---
        if (player.isDashing) {
            player.dashTimer -= dt;

            if (player.dashTimer <= 0) {
                player.isDashing = false;
                player.b2body.setGravityScale(1f);
                player.b2body.setLinearVelocity(0, 0);
            } else {
                float dashVelocity = player.facingRight ? Constants.Knight.DASH_SPEED : -Constants.Knight.DASH_SPEED;
                player.b2body.setLinearVelocity(dashVelocity, 0);
                return;
            }
        }

        // --- 2. DASH INITIATION (Now with Cooldown!) ---
        if (Gdx.input.isKeyJustPressed(dashKey) && player.canDash && player.dashCooldownTimer <= 0) {
            player.isDashing = true;
            player.canDash = false;
            player.dashTimer = Constants.Knight.DASH_DURATION;
            player.dashCooldownTimer = Constants.Knight.DASH_COOLDOWN; // Start the cooldown
            player.b2body.setGravityScale(0f);

            float dashVelocity = player.facingRight ? Constants.Knight.DASH_SPEED : -Constants.Knight.DASH_SPEED;
            player.b2body.setLinearVelocity(dashVelocity, 0);
            return;
        }

        // --- 3. NORMAL MOVEMENT LOGIC ---
        Vector2 vel = player.b2body.getLinearVelocity();
        float targetVelX = 0;
        float targetVelY = vel.y;

        if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT))) {
            targetVelX = -Constants.Knight.MAX_SPEED;
            player.facingRight = false;
        } else if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT))) {
            targetVelX = Constants.Knight.MAX_SPEED;
            player.facingRight = true;
        }

        // --- 4. FIXED VARIABLE JUMP HEIGHT ---
        if (!Gdx.input.isKeyPressed(jumpKey) && targetVelY > 0 && player.isJumping) {
            targetVelY *= Constants.Knight.JUMP_CUTOFF_MULTIPLIER;
            player.isJumping = false;
        }

        // --- 5. WALL SLIDE LOGIC ---
        boolean pushingLeft =
            Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT)) && player.isTouchingLeftWall;
        boolean pushingRight =
            Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT)) && player.isTouchingRightWall;

        if (!player.isGrounded && targetVelY <= 0 && (pushingLeft || pushingRight)) {
            // Force the exact slide speed, ignoring any Box2D friction
            targetVelY = -Constants.Knight.WALL_SLIDE_SPEED;

            // TODO: Trigger Mantis Claw animation state here later
        }

        // Apply calculated velocities
        player.b2body.setLinearVelocity(targetVelX, targetVelY);

        // --- 6. JUMP & DOUBLE JUMP INITIATION ---
        if (Gdx.input.isKeyJustPressed(jumpKey)) {
            if (player.isGrounded) {
                player.isJumping = true;
                player.b2body.setLinearVelocity(targetVelX, 0);
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH), player.b2body.getWorldCenter(), true);
            } else if (player.canDoubleJump) {
                player.canDoubleJump = false;
                player.isJumping = true;
                player.b2body.setLinearVelocity(targetVelX, 0);
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH), player.b2body.getWorldCenter(), true);
            }
        }

        // --- 7. POGO JUMP LOGIC (Now with Hitbox Linger!) ---
        // If they press the keys, activate the attack timer
        if (!player.isGrounded && Gdx.input.isKeyPressed(downKey) && Gdx.input.isKeyJustPressed(attackKey)) {
            player.pogoDurationTimer = Constants.Knight.POGO_ATTACK_DURATION;
        }

        // As long as the timer is active, keep firing the RayCast downward
        if (player.pogoDurationTimer > 0) {
            executePogoJump(targetVelX);
        }
    }

    private void executePogoJump(final float currentVelX) {
        Vector2 center = player.b2body.getWorldCenter();
        float halfHeight = Constants.Knight.HEIGHT_HALVED_SCALED;
        rayEnd.set(center.x, center.y - halfHeight - Constants.Knight.POGO_REACH);

        RayCastCallback pogoCallback = (fixture, _, _, _) -> {
            Object userData = fixture.getUserData();

            if ("spikes".equals(userData) || "enemy".equals(userData)) {
                player.b2body.setLinearVelocity(currentVelX, 0);
                player.b2body.applyLinearImpulse(
                    new Vector2(0, Constants.Knight.POGO_BOUNCE_STRENGTH),
                    player.b2body.getWorldCenter(),
                    true
                );

                player.canDoubleJump = true;
                player.canDash = true;
                player.isJumping = false;

                // Kill the attack timer immediately so we don't bounce twice on the same spike
                player.pogoDurationTimer = 0;

                // TODO: Should deal damage to the enemy here.

                return 0;
            }
            return 1;
        };

        player.world.rayCast(pogoCallback, center, rayEnd);
    }
}
