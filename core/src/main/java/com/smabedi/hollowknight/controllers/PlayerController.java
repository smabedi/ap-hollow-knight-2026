package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.AudioManager;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.enemies.Enemy;
import com.smabedi.hollowknight.models.entities.items.VengefulSpirit;
import com.smabedi.hollowknight.models.entities.items.VfxType;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.npcs.Zote;
import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;
import com.smabedi.hollowknight.views.game.GameUI;

/**
 * Handles all player-driven inputs and translates them into physical actions,
 * spellcasting, and state modifications for the Knight entity.
 */
public class PlayerController {
    private final Knight player;
    private final Inventory inventory;
    private final Vector2 rayEnd = new Vector2();
    private final Array<IDamageable> enemiesHitDuringDash = new Array<>();
    private final Array<IDamageable> enemiesHitDuringAttack = new Array<>();
    private final EventCallback eventCallback;
    private GameUI gameUI;
    private boolean wasZoteNearby = false;

    public PlayerController(Knight player, Inventory inventory, GameUI gameUI, EventCallback eventCallback) {
        this.player = player;
        this.inventory = inventory;
        this.gameUI = gameUI;
        this.eventCallback = eventCallback;

        inventory.addOwnedCharm(CharmType.SOUL_CATCHER);
        inventory.addOwnedCharm(CharmType.DASHMASTER);
        inventory.addOwnedCharm(CharmType.UNBREAKABLE_STRENGTH);
        inventory.addOwnedCharm(CharmType.QUICK_SLASH);
        inventory.addOwnedCharm(CharmType.QUICK_FOCUS);
        inventory.addOwnedCharm(CharmType.HEAVY_BLOW);
        inventory.addOwnedCharm(CharmType.SHARP_SHADOW);
        inventory.addOwnedCharm(CharmType.VOID_HEART);
    }

    /**
     * Main evaluation loop for player inputs. Manages ability cooldowns,
     * environmental interactions, and combat commands.
     *
     * @param dt The scaled delta time for frame-rate independent tracking.
     */
    public void handleInput(float dt) {
        if (player.b2body == null || player.isDead) return;

        player.hasSharpShadow = inventory.isEquipped(CharmType.SHARP_SHADOW);
        player.hasVoidHeart = inventory.isEquipped(CharmType.VOID_HEART);

        // Process spectator flight overrides
        if (player.isNoclip) {
            float flySpeed = Constants.Knight.MAX_SPEED * 2f;
            float vx = 0, vy = 0;

            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT))) vx = -flySpeed;
            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT))) vx = flySpeed;
            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_UP))) vy = flySpeed;
            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_DOWN))) vy = -flySpeed;

            player.b2body.setLinearVelocity(vx, vy);
            return;
        }

        // Decrement local ability cooldown timers
        if (player.dashCooldownTimer > 0) player.dashCooldownTimer -= dt;
        if (player.pogoDurationTimer > 0) player.pogoDurationTimer -= dt;
        if (player.attackCooldownTimer > 0) player.attackCooldownTimer -= dt;

        // Reset mid-air mobility abilities upon safe grounding
        if (player.isGrounded && !player.isDashing) {
            player.canDash = true;
            player.canDoubleJump = true;
            player.isDoubleJumping = false;
        }

        int jumpKey = GameSettings.getKey(GameSettings.KEY_JUMP);
        int attackKey = GameSettings.getKey(GameSettings.KEY_ATTACK);
        int downKey = GameSettings.getKey(GameSettings.KEY_DOWN);
        int dashKey = GameSettings.getKey(GameSettings.KEY_DASH);
        int focusKey = GameSettings.getKey(GameSettings.KEY_FOCUS);
        int upKey = GameSettings.getKey(GameSettings.KEY_UP);
        int leftKey = GameSettings.getKey(GameSettings.KEY_LEFT);
        int rightKey = GameSettings.getKey(GameSettings.KEY_RIGHT);

        // Evaluate proximity for NPC interactions
        float zoteInteractionDistance = Constants.Zote.INTERACTION_DISTANCE;
        Vector2 center = player.b2body.getWorldCenter();
        final Zote[] nearbyZote = {null};
        player.world.QueryAABB(fixture -> {
                if (fixture.getUserData() instanceof Zote) {
                    nearbyZote[0] = (Zote) fixture.getUserData();
                }
                return true;
            },
            center.x - zoteInteractionDistance,
            center.y - zoteInteractionDistance,
            center.x + zoteInteractionDistance,
            center.y + zoteInteractionDistance);

        if (nearbyZote[0] != null && !nearbyZote[0].isAngry()) {
            if (!wasZoteNearby) {
                gameUI.showToast(Assets.getString("press_up_to_listen"));
                AudioManager.playSfx(Constants.Paths.Sounds.SFX_NOTIFICATION);
                wasZoteNearby = true;
            }
        } else {
            wasZoteNearby = false;
        }

        // Manage dynamic dialogue interactions
        if (Gdx.input.isKeyJustPressed(upKey) && player.isGrounded) {
            if (gameUI.isDialogVisible()) {
                gameUI.showDialog("");
                return;
            }

            final boolean[] interacted = {false};
            player.world.QueryAABB(fixture -> {
                    if (fixture.getUserData() instanceof Zote zote) {
                        String dialogText = zote.getNextDialogue();
                        if (dialogText != null) {
                            gameUI.showDialog(dialogText);
                            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y);
                            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_ZOTE, 0.8f, 1.2f);
                            System.out.println("[SFX] Zote grumbles...");
                            interacted[0] = true;
                        }
                    }
                    return true;
                },
                center.x - zoteInteractionDistance,
                center.y - zoteInteractionDistance,
                center.x + zoteInteractionDistance,
                center.y + zoteInteractionDistance);

            if (interacted[0]) return;
        }

        // Suspend horizontal movement during active dialogue
        if (gameUI.isDialogVisible()) {
            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y);
            return;
        }

        // Process active dash state
        if (player.isDashing) {
            player.dashTimer -= dt;

            if (player.dashTimer <= 0) {
                player.isDashing = false;
                player.b2body.setGravityScale(1f);
                player.b2body.setLinearVelocity(0, 0);
            } else {
                float baseDashSpeed = inventory.isEquipped(CharmType.SHARP_SHADOW)
                    ? Constants.Knight.Dash.SPEED * 1.2f
                    : Constants.Knight.Dash.SPEED;

                float dashVelocity = player.facingRight ? baseDashSpeed : -baseDashSpeed;
                player.b2body.setLinearVelocity(dashVelocity, 0);

                if (inventory.isEquipped(CharmType.SHARP_SHADOW)) {
                    executeDashDamage();
                }
                return;
            }
        }

        // Process dash initiation
        if (Gdx.input.isKeyJustPressed(dashKey) && player.canDash && player.dashCooldownTimer <= 0) {
            player.isDashing = true;
            player.canDash = false;
            AudioManager.playSfx(Constants.Paths.Sounds.SFX_DASH);
            if (inventory.isEquipped(CharmType.SHARP_SHADOW)) {
                player.dashTimer = Constants.Knight.Dash.DURATION * 1.5f;
            } else {
                player.dashTimer = Constants.Knight.Dash.DURATION;
            }
            enemiesHitDuringDash.clear();

            player.dashCooldownTimer = inventory.isEquipped(CharmType.DASHMASTER)
                ? Constants.Knight.Dash.COOLDOWN * 0.5f
                : Constants.Knight.Dash.COOLDOWN;
            player.b2body.setGravityScale(0f);

            float baseDashSpeed = inventory.isEquipped(CharmType.SHARP_SHADOW)
                ? Constants.Knight.Dash.SPEED * 1.2f
                : Constants.Knight.Dash.SPEED;

            float dashVelocity = player.facingRight ? baseDashSpeed : -baseDashSpeed;
            player.b2body.setLinearVelocity(dashVelocity, 0);

            if (inventory.isEquipped(CharmType.SHARP_SHADOW)) {
                player.iFrameTimer = Constants.Knight.Dash.DURATION * 1.5f;
                eventCallback.spawnStaticVfx(VfxType.SHADOW_DASH, player.b2body.getPosition().x, player.b2body.getPosition().y, 0, 0, player.facingRight, false);
            } else {
                eventCallback.spawnStaticVfx(VfxType.NORMAL_DASH, player.b2body.getPosition().x, player.b2body.getPosition().y, 0, 0, player.facingRight, false);
            }
            return;
        }

        // Process horizontal movement
        Vector2 vel = player.b2body.getLinearVelocity();
        float targetVelX = 0;
        float targetVelY = vel.y;

        if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT))) {
            targetVelX = -Constants.Knight.MAX_SPEED;
            if (!player.isCasting()) {
                player.facingRight = false;
            }
        } else if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT))) {
            targetVelX = Constants.Knight.MAX_SPEED;
            if (!player.isCasting()) {
                player.facingRight = true;
            }
        }

        // Handle variable jump cutoff
        if (!Gdx.input.isKeyPressed(jumpKey) && targetVelY > 0 && player.isJumping) {
            targetVelY *= Constants.Knight.JUMP_CUTOFF_MULTIPLIER;
            player.isJumping = false;
        }

        // Process wall sliding checks
        boolean pushingLeft =
            Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT)) && player.isTouchingLeftWall;
        boolean pushingRight =
            Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT)) && player.isTouchingRightWall;

        if (!player.isGrounded && targetVelY <= 0 && (pushingLeft || pushingRight)) {
            targetVelY = -Constants.Knight.WALL_SLIDE_SPEED;
        }

        player.b2body.setLinearVelocity(targetVelX, targetVelY);

        // Process standard and double jumps
        if (Gdx.input.isKeyJustPressed(jumpKey)) {
            if (player.isGrounded) {
                player.b2body.setLinearVelocity(player.b2body.getLinearVelocity().x, 0);
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH), player.b2body.getWorldCenter(), true);
                AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_JUMP, 0.9f, 1.1f);
                player.isDoubleJumping = false;
            }
            else if (player.canDoubleJump) {
                player.b2body.setLinearVelocity(player.b2body.getLinearVelocity().x, 0);
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH), player.b2body.getWorldCenter(), true);
                AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_JUMP, 0.9f, 1.1f);
                player.canDoubleJump = false;
                player.isDoubleJumping = true;
            }
        }

        // Process nail attack initiation
        if (Gdx.input.isKeyJustPressed(attackKey) && player.attackCooldownTimer <= 0) {
            AudioManager.playSfxVaried(Constants.Paths.Sounds.SFX_SLASH, 0.85f, 1.15f);
            player.pogoDurationTimer = Constants.Knight.Pogo.ATTACK_DURATION;
            player.attackCooldownTimer = Constants.Knight.ATTACK_COOLDOWN;
            if (inventory.isEquipped(CharmType.QUICK_SLASH)) player.attackCooldownTimer *= 0.5f;

            player.attackDurationTimer = player.attackCooldownTimer;
            player.isAttackingDown = !player.isGrounded && Gdx.input.isKeyPressed(downKey);
            enemiesHitDuringAttack.clear();
        }

        if (player.pogoDurationTimer > 0) {
            if (player.isAttackingDown) {
                executePogoJump(targetVelX);
            } else {
                executeHorizontalAttack();
            }
        }

        // Restrict movement during Howling Wraiths spellcasting
        if (player.wraithsTimer > 0) {
            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y);

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

        // Restrict horizontal movement during Vengeful Spirit spellcasting
        if (player.spritCastTimer > 0) {
            player.b2body.setLinearVelocity(0, 0);
            return;
        }

        // Process spell consumption and deployment
        boolean pressingUp = Gdx.input.isKeyPressed(upKey);
        boolean pressingSide = Gdx.input.isKeyPressed(leftKey) || Gdx.input.isKeyPressed(rightKey);

        if (Gdx.input.isKeyJustPressed(focusKey)) {
            if (pressingUp || pressingSide) {
                if (player.soul >= Constants.Knight.FOCUS_COST) {
                    player.soul -= Constants.Knight.FOCUS_COST;

                    if (pressingUp) {
                        player.wraithsTimer = Constants.Knight.HowlingWraiths.DURATION;
                        player.wraithsTicksFired = 0;
                        VfxType wraithsAnim = player.hasVoidHeart ? VfxType.VOID_WRAITHS : VfxType.WRAITHS;
                        eventCallback.spawnStaticVfx(wraithsAnim, center.x, center.y, 0, Constants.Knight.HEIGHT_HALVED_SCALED * 3f, player.facingRight, true);
                        System.out.println("Howling Wraiths Cast!");
                    } else {
                        player.spritCastTimer = Constants.Knight.VengefulSpirit.DURATION;
                        new VengefulSpirit(player.world, center.x, center.y, player.facingRight, inventory.isEquipped(CharmType.VOID_HEART));
                        VfxType soulAnim = player.hasVoidHeart ? VfxType.VOID_SPIRIT_CAST : VfxType.SPIRIT_CAST;
                        eventCallback.spawnStaticVfx(soulAnim, center.x, center.y, Constants.Knight.WIDTH_HALVED_SCALED, 0, player.facingRight, true);
                        System.out.println("Vengeful Spirit Cast!");
                    }
                    if (player.hasVoidHeart) {
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_VOID_SPELL_CAST);
                    } else {
                        AudioManager.playSfx(Constants.Paths.Sounds.SFX_SPELL_CAST);
                    }
                    return;
                } else {
                    System.out.println("Not enough Soul for spells! Need " + (Constants.Knight.FOCUS_COST - player.soul) + " more.");
                }
            }
        }

        // Process active focus healing
        float focusTargetTime = inventory.isEquipped(CharmType.QUICK_FOCUS)
            ? Constants.Knight.FOCUS_DURATION * 0.6f
            : Constants.Knight.FOCUS_DURATION;

        if (player.isGrounded
            && Gdx.input.isKeyPressed(focusKey)
            && !pressingUp
            && !pressingSide
            && player.health < Constants.Knight.MAX_HEALTH
            && player.soul >= Constants.Knight.FOCUS_COST
        ) {
            player.isFocusing = true;
            player.focusTimer += dt;
            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y);

            if (player.focusTimer >= focusTargetTime) {
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

    /**
     * Executes the hitbox check for the Sharp Shadow dash modifier.
     */
    private void executeDashDamage() {
        Vector2 center = player.b2body.getWorldCenter();

        float reachX = Constants.Knight.WIDTH_HALVED_SCALED * 1.2f;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED;

        QueryCallback dashDamageCallback = fixture -> {
            Object userData = fixture.getUserData();

            if (userData instanceof IDamageable enemy) {
                if (!enemy.isDead() && !enemiesHitDuringDash.contains(enemy, true)) {
                    enemy.applyKnockback(0, 4f);
                    enemy.takeDamage(1);
                    eventCallback.spawnStaticVfx(VfxType.DAMAGE, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);

                    enemiesHitDuringDash.add(enemy);
                }
            }
            return true;
        };

        player.world.QueryAABB(
            dashDamageCallback,
            center.x - reachX,
            center.y - heightY,
            center.x + reachX,
            center.y + heightY
        );
    }

    /**
     * Performs a vertical downward raycast to evaluate pogo strikes against spikes or enemies.
     *
     * @param currentVelX The current horizontal velocity to preserve during the pogo recoil.
     */
    private void executePogoJump(final float currentVelX) {
        Vector2 center = player.b2body.getWorldCenter();
        float halfHeight = Constants.Knight.HEIGHT_HALVED_SCALED;
        rayEnd.set(center.x, center.y - halfHeight - Constants.Knight.Pogo.REACH);

        RayCastCallback pogoCallback = (fixture, _, _, fraction) -> {
            Object userData = fixture.getUserData();
            boolean isSpikes = "spikes".equals(userData);
            boolean isEnemy = userData instanceof IDamageable;

            if (isEnemy) {
                IDamageable enemy = (IDamageable) userData;
                if (enemy.isDead() || enemiesHitDuringAttack.contains(enemy, true)) {
                    return -1;
                }
            }

            if (isSpikes || isEnemy) {
                player.b2body.setLinearVelocity(currentVelX, 0);
                player.b2body.applyLinearImpulse(
                    new Vector2(0, Constants.Knight.Pogo.BOUNCE_STRENGTH),
                    player.b2body.getWorldCenter(),
                    true
                );

                player.canDoubleJump = true;
                player.canDash = true;
                player.isJumping = false;
                player.isDoubleJumping = false;

                if (isEnemy) {
                    IDamageable enemy = (IDamageable) userData;
                    int damage = inventory.isEquipped(CharmType.UNBREAKABLE_STRENGTH) ? 2 : 1;
                    enemy.takeDamage(damage);
                    eventCallback.spawnStaticVfx(VfxType.DAMAGE, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);

                    if (!(enemy instanceof Zote)) {
                        int soulGain = inventory.isEquipped(CharmType.SOUL_CATCHER)
                            ? Constants.Knight.SOUL_PER_HIT + 5
                            : Constants.Knight.SOUL_PER_HIT;
                        player.addSoul(soulGain);
                    }

                    enemiesHitDuringAttack.add(enemy);
                }
                return fraction;
            }
            if ("ground".equals(userData)) return fraction;
            return -1;
        };
        player.world.rayCast(pogoCallback, center, rayEnd);
    }

    /**
     * Executes the bounding box query for a standard horizontal nail slash.
     */
    private void executeHorizontalAttack() {
        Vector2 center = player.b2body.getWorldCenter();
        float direction = player.facingRight ? 1f : -1f;

        float reachX = Constants.Knight.NAIL_REACH;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED * 2f;
        player.isAttackingDown = false;

        float lowerX = player.facingRight ? center.x : center.x - reachX;
        float upperX = player.facingRight ? center.x + reachX : center.x;
        float lowerY = center.y - heightY;
        float upperY = center.y + heightY;

        QueryCallback attackCallback = fixture -> {
            Object userData = fixture.getUserData();
            if (userData instanceof IDamageable enemy) {
                if (!enemy.isDead() && !enemiesHitDuringAttack.contains(enemy, true)) {

                    int damage = inventory.isEquipped(CharmType.UNBREAKABLE_STRENGTH) ? 2 : 1;
                    float knockbackMulti = inventory.isEquipped(CharmType.HEAVY_BLOW) ? 2f : 1f;

                    enemy.applyKnockback(direction * 3f * knockbackMulti, 4f);
                    enemy.takeDamage(damage);
                    eventCallback.spawnStaticVfx(VfxType.DAMAGE, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);

                    if (!(enemy instanceof Zote)) {
                        int soulGain = inventory.isEquipped(CharmType.SOUL_CATCHER)
                            ? Constants.Knight.SOUL_PER_HIT + 5
                            : Constants.Knight.SOUL_PER_HIT;
                        player.addSoul(soulGain);
                    }

                    enemiesHitDuringAttack.add(enemy);
                }
            }
            return true;
        };

        player.world.QueryAABB(attackCallback, lowerX, lowerY, upperX, upperY);
    }

    /**
     * Evaluates spatial damage ticks for the Howling Wraiths spell execution.
     */
    private void executeWraithsHit() {
        Vector2 center = player.b2body.getWorldCenter();

        float width = Constants.Knight.HowlingWraiths.WIDTH;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED;

        float lowerX = center.x - width;
        float upperX = center.x + width;
        float lowerY = center.y + heightY;
        float upperY = center.y + heightY + Constants.Knight.HowlingWraiths.HEIGHT;

        QueryCallback wraithsCallback = fixture -> {
            Object userData = fixture.getUserData();

            if (userData instanceof IDamageable enemy) {
                if (!enemy.isDead()) {
                    int spellDamage = inventory.isEquipped(CharmType.VOID_HEART) ? 2 : 1;
                    if (player.wraithsTicksFired == 2 || (enemy instanceof Enemy && ((Enemy) enemy).getHp() == 1)) {
                        enemy.applyKnockback(0, 4f);
                    }
                    enemy.takeDamage(spellDamage);
                    eventCallback.spawnStaticVfx(VfxType.DAMAGE, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);
                }
            }
            return true;
        };

        player.world.QueryAABB(wraithsCallback, lowerX, lowerY, upperX, upperY);
    }

    public void setGameUI(GameUI gameUI) {
        this.gameUI = gameUI;
    }
}
