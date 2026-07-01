package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.spells.VengefulSpirit;

public class PlayerController {
    private final Knight player;
    private final Vector2 rayEnd = new Vector2();

    public PlayerController(Knight player) {
        this.player = player;
    }

    public void handleInput(float dt) {
        if (player.b2body == null || player.isDead) return;

        // --- TICK TIMERS ---
        if (player.dashCooldownTimer > 0) player.dashCooldownTimer -= dt;
        if (player.pogoDurationTimer > 0) player.pogoDurationTimer -= dt;
        if (player.iFrameTimer > 0) player.iFrameTimer -= dt; // Tick invincibility Frames
        if (player.wraithsTimer > 0) player.wraithsTimer -= dt;
        if (player.spritCastTimer > 0) player.spritCastTimer -= dt;

        int jumpKey = GameSettings.getKey(GameSettings.KEY_JUMP);
        int attackKey = GameSettings.getKey(GameSettings.KEY_ATTACK);
        int downKey = GameSettings.getKey(GameSettings.KEY_DOWN);
        int dashKey = GameSettings.getKey(GameSettings.KEY_DASH);
        int focusKey = GameSettings.getKey(GameSettings.KEY_FOCUS);
        int upKey = GameSettings.getKey(GameSettings.KEY_UP);
        int leftKey = GameSettings.getKey(GameSettings.KEY_LEFT);
        int rightKey = GameSettings.getKey(GameSettings.KEY_RIGHT);

        // --- DASH STATE MACHINE ---
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

        // --- DASH INITIATION ---
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

        // --- NORMAL MOVEMENT LOGIC ---
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

        // --- FIXED VARIABLE JUMP HEIGHT ---
        if (!Gdx.input.isKeyPressed(jumpKey) && targetVelY > 0 && player.isJumping) {
            targetVelY *= Constants.Knight.JUMP_CUTOFF_MULTIPLIER;
            player.isJumping = false;
        }

        // --- WALL SLIDE LOGIC ---
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

        // --- JUMP & DOUBLE JUMP INITIATION ---
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

        // --- ATTACK LOGIC ---
        if (Gdx.input.isKeyJustPressed(attackKey)) {
            player.pogoDurationTimer = Constants.Knight.POGO_ATTACK_DURATION;
        }

        if (player.pogoDurationTimer > 0) {
            if (!player.isGrounded && Gdx.input.isKeyPressed(downKey)) {
                // Pogo Attack (Downward) As long as the timer is active, keep firing the RayCast downward
                executePogoJump(targetVelX);
            } else {
                // Normal Attack (Horizontal)
                executeHorizontalAttack();
            }
        }

        // --- WRAITHS STATE MACHINE (Blocks all other input) ---
        if (player.wraithsTimer > 0) {
            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y);

            // Fire 3 ticks of damage over the 0.6 seconds
            if (player.wraithsTimer <= 0.6f && player.wraithsTicksFired == 0) {
                executeWraithsHit();
                player.wraithsTicksFired++;
            } else if (player.wraithsTimer <= 0.4f && player.wraithsTicksFired == 1) {
                executeWraithsHit();
                player.wraithsTicksFired++;
            } else if (player.wraithsTimer <= 0.2f && player.wraithsTicksFired == 2) {
                executeWraithsHit();
                player.wraithsTicksFired++;
            }
            return;
        }

        // --- VENGEFUL SPIRIT STATE MACHINE ---
        if (player.spritCastTimer > 0) {
            // Lock horizontal movement entirely while the cast animation plays!
            player.b2body.setLinearVelocity(0, 0);
            return; // Exit handleInput early!
        }

        // --- SPELL INITIATION ---
        boolean pressingUp = Gdx.input.isKeyPressed(upKey);
        boolean pressingSide = Gdx.input.isKeyPressed(leftKey) || Gdx.input.isKeyPressed(rightKey);

        // Check if they TAP the focus key
        if (Gdx.input.isKeyJustPressed(focusKey)) {
            // Check if they are HOLDING a spell direction
            if (pressingUp || pressingSide) {
                if (player.soul >= Constants.Knight.FOCUS_COST) {
                    player.soul -= Constants.Knight.FOCUS_COST;

                    if (pressingUp) {
                        player.wraithsTimer = Constants.Knight.WRAITHS_DURATION; // Start the animation lock!
                        player.wraithsTicksFired = 0;
                        System.out.println("Howling Wraiths Cast!");
                    } else {
                        player.spritCastTimer = Constants.Knight.SPRIT_CAST_DURATION;
                        Vector2 center = player.b2body.getWorldCenter();
                        new VengefulSpirit(player.world, center.x, center.y, player.facingRight);
                        System.out.println("Vengeful Spirit Cast!");
                    }
                    return; // Exit out, spell successfully cast
                } else {
                    System.out.println("Not enough Soul for spells! Need " + (Constants.Knight.FOCUS_COST - player.soul) + " more.");
                }
            }
        }

        // --- FOCUS (HEALING) STATE MACHINE ---
        // Must be holding the key, grounded, NOT holding a spell direction, and have enough soul
        if (player.isGrounded && Gdx.input.isKeyPressed(focusKey) && !pressingUp && !pressingSide && player.health < Constants.Knight.MAX_HEALTH && player.soul >= Constants.Knight.FOCUS_COST) {
            player.isFocusing = true;
            player.focusTimer += dt;
            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y); // Lock movement

            if (player.focusTimer >= Constants.Knight.FOCUS_DURATION) {
                player.heal(1);
                player.soul -= Constants.Knight.FOCUS_COST;
                player.focusTimer = 0f;
            }
            return;
        } else {
            player.isFocusing = false;
            player.focusTimer = 0f;
        }
    }

    private void executePogoJump(final float currentVelX) {
        Vector2 center = player.b2body.getWorldCenter();
        float halfHeight = Constants.Knight.HEIGHT_HALVED_SCALED;
        rayEnd.set(center.x, center.y - halfHeight - Constants.Knight.POGO_REACH);

        RayCastCallback pogoCallback = (fixture, _, _, _) -> {
            Object userData = fixture.getUserData();

            boolean isSpikes = "spikes".equals(userData);
            boolean isEnemy = userData instanceof IDamageable;

            // If it's an enemy, make sure it's not a corpse
            if (isEnemy) {
                IDamageable enemy = (IDamageable) userData;
                if (enemy.isDead()) {
                    return 1; // Ignore corpses, continue the raycast downward
                }
            }

            if (isSpikes || isEnemy) {
                // Reset falling momentum and apply the bounce
                player.b2body.setLinearVelocity(currentVelX, 0);
                player.b2body.applyLinearImpulse(
                    new Vector2(0, Constants.Knight.POGO_BOUNCE_STRENGTH),
                    player.b2body.getWorldCenter(),
                    true
                );

                // Reset midair abilities
                player.canDoubleJump = true;
                player.canDash = true;
                player.isJumping = false;

                // Kill the attack timer immediately so we don't bounce twice
                player.pogoDurationTimer = 0;

                // Enemy-specific logic (Damage & Soul, NO knockback)
                if (isEnemy) {
                    IDamageable enemy = (IDamageable) userData;
                    enemy.takeDamage(1);
                    player.addSoul(Constants.Knight.SOUL_PER_HIT);
                }

                return 0; // Terminate raycast, we found our target
            }

            return 1; // Not spikes or a living enemy, keep checking
        };

        player.world.rayCast(pogoCallback, center, rayEnd);
    }

    private void executeHorizontalAttack() {
        Vector2 center = player.b2body.getWorldCenter();
        float direction = player.facingRight ? 1f : -1f;

        float reachX = Constants.Knight.NAIL_REACH;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED;

        float lowerX = player.facingRight ? center.x : center.x - reachX;
        float upperX = player.facingRight ? center.x + reachX : center.x;
        float lowerY = center.y - heightY;
        float upperY = center.y + heightY;

        // A single array boolean allows us to apply player recoil only once,
        // even if we slice through 3 enemies simultaneously.
        final boolean[] hitSomething = {false};

        // The Box2D Query Callback
        QueryCallback attackCallback = fixture -> {
            Object userData = fixture.getUserData();

            if (userData instanceof IDamageable enemy) {

                if (!enemy.isDead()) {
                    enemy.takeDamage(1);
                    enemy.applyKnockback(direction * 3f, 1f);
                    player.addSoul(Constants.Knight.SOUL_PER_HIT);
                    hitSomething[0] = true;
                }
            }
            return true;
        };

        player.world.QueryAABB(attackCallback, lowerX, lowerY, upperX, upperY);

        // If we hit anything, apply recoil to the Knight and kill the attack timer
        if (hitSomething[0]) {
            player.pogoDurationTimer = 0;
        }
    }

    private void executeWraithsHit() {
        Vector2 center = player.b2body.getWorldCenter();

        float width = Constants.Knight.WRAITHS_WIDTH;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED;

        float lowerX = center.x - width;
        float upperX = center.x + width;
        float lowerY = center.y + heightY; // Starts at the top of the Knight's head
        float upperY = center.y + heightY + Constants.Knight.WRAITHS_HEIGHT;

        QueryCallback wraithsCallback = fixture -> {
            Object userData = fixture.getUserData();

            if (userData instanceof IDamageable enemy) {
                if (!enemy.isDead()) {
                    enemy.takeDamage(1);
                    // Wraiths don't give soul, but pop enemies upward slightly
                    enemy.applyKnockback(0, 2f);
                }
            }
            return true;
        };

        player.world.QueryAABB(wraithsCallback, lowerX, lowerY, upperX, upperY);
    }
}
