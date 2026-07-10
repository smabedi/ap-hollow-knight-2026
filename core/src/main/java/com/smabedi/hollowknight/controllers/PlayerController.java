package com.smabedi.hollowknight.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.smabedi.hollowknight.config.Assets;
import com.smabedi.hollowknight.config.Constants;
import com.smabedi.hollowknight.config.GameSettings;
import com.smabedi.hollowknight.models.entities.IDamageable;
import com.smabedi.hollowknight.models.entities.enemies.Enemy;
import com.smabedi.hollowknight.models.entities.items.VengefulSpirit;
import com.smabedi.hollowknight.models.entities.knight.Knight;
import com.smabedi.hollowknight.models.entities.npcs.Zote;
import com.smabedi.hollowknight.models.inventory.CharmType;
import com.smabedi.hollowknight.models.inventory.Inventory;
import com.smabedi.hollowknight.views.game.GameUI;

public class PlayerController {
    private final Knight player;
    private final Inventory inventory;
    private final Vector2 rayEnd = new Vector2();
    private final Array<IDamageable> enemiesHitDuringDash = new Array<>();
    private final Array<IDamageable> enemiesHitDuringAttack = new Array<>();
    private final GameUI gameUI;
    private boolean wasZoteNearby = false;
    private final EventCallback eventCallback;
    private final Animation<TextureRegion> damageAnimation;

    public PlayerController(Knight player, Inventory inventory, GameUI gameUI, EventCallback eventCallback, Animation<TextureRegion> damageAnimation) {
        this.player = player;
        this.inventory = inventory;
        this.gameUI = gameUI;
        this.eventCallback = eventCallback;
        this.damageAnimation = damageAnimation;

        // HACK: Added for debug, remove later.
        inventory.addOwnedCharm(CharmType.SOUL_CATCHER);
        inventory.addOwnedCharm(CharmType.DASHMASTER);
        inventory.addOwnedCharm(CharmType.UNBREAKABLE_STRENGTH);
        inventory.addOwnedCharm(CharmType.QUICK_SLASH);
        inventory.addOwnedCharm(CharmType.QUICK_FOCUS);
        inventory.addOwnedCharm(CharmType.HEAVY_BLOW);
        inventory.addOwnedCharm(CharmType.SHARP_SHADOW);
        inventory.addOwnedCharm(CharmType.VOID_HEART);
    }

    public void handleInput(float dt) {
        if (player.b2body == null || player.isDead) return;

        player.hasSharpShadow = inventory.isEquipped(CharmType.SHARP_SHADOW);
        player.hasVoidHeart = inventory.isEquipped(CharmType.VOID_HEART);

        // --- NOCLIP / SPECTATOR OVERRIDE ---
        if (player.isNoclip) {
            float flySpeed = Constants.Knight.MAX_SPEED * 2f;
            float vx = 0, vy = 0;

            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_LEFT))) vx = -flySpeed;
            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_RIGHT))) vx = flySpeed;
            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_UP))) vy = flySpeed;
            if (Gdx.input.isKeyPressed(GameSettings.getKey(GameSettings.KEY_DOWN))) vy = -flySpeed;

            player.b2body.setLinearVelocity(vx, vy);
            return; // Terminate early so normal logic does not override our flight
        }

        // --- TICK TIMERS ---
        // (iFrame, attackDuration, wraiths, and spritCast timers are now strictly handled in Knight.java!)
        if (player.dashCooldownTimer > 0) player.dashCooldownTimer -= dt;
        if (player.pogoDurationTimer > 0) player.pogoDurationTimer -= dt;
        if (player.attackCooldownTimer > 0) player.attackCooldownTimer -= dt;

        // Continuously refresh midair abilities while safely on the ground.
        // We ensure !player.isDashing is checked so that dashing off a ledge
        // correctly consumes the dash ability.
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

        // --- PROXIMITY SENSOR ---
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

        // Toast Hint Logic
        if (nearbyZote[0] != null && !nearbyZote[0].isAngry()) {
            if (!wasZoteNearby) {
                gameUI.showToast(Assets.getString("press_up_to_listen"));
                wasZoteNearby = true;
            }
        } else {
            wasZoteNearby = false;
        }

        // --- DIALOGUE INTERACTION LOGIC ---
        if (Gdx.input.isKeyJustPressed(upKey) && player.isGrounded) {
            // If dialog is currently open/typing, pass input to UI to skip/close
            if (gameUI.isDialogVisible()) {
                gameUI.showDialog(""); // Passing empty string or triggering an advance method closes/skips it
                return;
            }

            // If dialog is closed and Zote is within interaction reach
            final boolean[] interacted = {false};
            player.world.QueryAABB(fixture -> {
                    if (fixture.getUserData() instanceof Zote zote) {
                        String dialogText = zote.getNextDialogue();
                        if (dialogText != null) {
                            gameUI.showDialog(dialogText);
                            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y); // Halt player
                            System.out.println("[SFX] Zote grumbles..."); // SFX Trigger
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

        // Block player movement if dialogue is actively typing/showing
        if (gameUI.isDialogVisible()) {
            player.b2body.setLinearVelocity(0, player.b2body.getLinearVelocity().y);
            return;
        }

        // --- DASH STATE MACHINE ---
        if (player.isDashing) {
            player.dashTimer -= dt;

            if (player.dashTimer <= 0) {
                player.isDashing = false;
                player.b2body.setGravityScale(1f);
                player.b2body.setLinearVelocity(0, 0);
            } else {
                // Maintain the 1.2x speed multiplier for the whole dash
                float baseDashSpeed = inventory.isEquipped(CharmType.SHARP_SHADOW)
                    ? Constants.Knight.Dash.SPEED * 1.2f
                    : Constants.Knight.Dash.SPEED;

                float dashVelocity = player.facingRight ? baseDashSpeed : -baseDashSpeed;
                player.b2body.setLinearVelocity(dashVelocity, 0);

                // Apply damage to enemies we collide with
                if (inventory.isEquipped(CharmType.SHARP_SHADOW)) {
                    executeDashDamage();
                }
                return;
            }
        }

        // --- DASH INITIATION ---
        if (Gdx.input.isKeyJustPressed(dashKey) && player.canDash && player.dashCooldownTimer <= 0) {
            player.isDashing = true;
            player.canDash = false;
            if (inventory.isEquipped(CharmType.SHARP_SHADOW)) {
                player.dashTimer = Constants.Knight.Dash.DURATION * 1.5f;
            } else {
                player.dashTimer = Constants.Knight.Dash.DURATION;
            }
            enemiesHitDuringDash.clear();

            // Dashmaster check to lower cooldown time
            player.dashCooldownTimer = inventory.isEquipped(CharmType.DASHMASTER)
                ? Constants.Knight.Dash.COOLDOWN * 0.5f
                : Constants.Knight.Dash.COOLDOWN;
            player.b2body.setGravityScale(0f);

            // Sharp Shadow check to multiply dash speed by 1.2
            float baseDashSpeed = inventory.isEquipped(CharmType.SHARP_SHADOW)
                ? Constants.Knight.Dash.SPEED * 1.2f
                : Constants.Knight.Dash.SPEED;

            float dashVelocity = player.facingRight ? baseDashSpeed : -baseDashSpeed;
            player.b2body.setLinearVelocity(dashVelocity, 0);

            if (inventory.isEquipped(CharmType.SHARP_SHADOW)) {
                player.iFrameTimer = Constants.Knight.Dash.DURATION * 1.5f;
                eventCallback.spawnStaticVfx(Assets.getShadowDashVfx(), player.b2body.getPosition().x, player.b2body.getPosition().y, 0, 0, player.facingRight, false);
            } else {
                eventCallback.spawnStaticVfx(Assets.getNormalDashVfx(), player.b2body.getPosition().x, player.b2body.getPosition().y, 0, 0, player.facingRight, false);
            }
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
        }

        // Apply calculated velocities
        player.b2body.setLinearVelocity(targetVelX, targetVelY);

        // --- JUMP & DOUBLE JUMP INITIATION ---
        if (Gdx.input.isKeyJustPressed(jumpKey)) {

            // Condition 1: Normal Jump from the ground
            if (player.isGrounded) {
                // Reset Y velocity before jumping to ensure consistent jump heights
                player.b2body.setLinearVelocity(player.b2body.getLinearVelocity().x, 0);
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH), player.b2body.getWorldCenter(), true);

                player.isDoubleJumping = false; // Ensure double jump animation is OFF
            }
            // Condition 2: Double Jump in midair
            else if (player.canDoubleJump) {
                // Reset Y velocity so falling momentum doesn't eat the double jump force
                player.b2body.setLinearVelocity(player.b2body.getLinearVelocity().x, 0);
                player.b2body.applyLinearImpulse(new Vector2(0, Constants.Knight.JUMP_STRENGTH), player.b2body.getWorldCenter(), true);

                player.canDoubleJump = false; // Consume the double jump
                player.isDoubleJumping = true;
            }
        }

        // --- ATTACK LOGIC ---
        if (Gdx.input.isKeyJustPressed(attackKey) && player.attackCooldownTimer <= 0) {
            player.pogoDurationTimer = Constants.Knight.Pogo.ATTACK_DURATION;
            player.attackCooldownTimer = Constants.Knight.ATTACK_COOLDOWN;
            if (inventory.isEquipped(CharmType.QUICK_SLASH)) player.attackCooldownTimer *= 0.5f; // Half the cooldown

            // 1. Sync animation duration to the actual cooldown
            player.attackDurationTimer = player.attackCooldownTimer;

            // 2. Lock in the attack direction based on input, not gravity
            player.isAttackingDown = !player.isGrounded && Gdx.input.isKeyPressed(downKey);

            // 3. Clear the hit tracker for the new swing
            enemiesHitDuringAttack.clear();
        }

        if (player.pogoDurationTimer > 0) {
            if (player.isAttackingDown) {
                // Pogo Attack (Downward)
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
                        player.wraithsTimer = Constants.Knight.HowlingWraiths.DURATION; // Start the animation lock!
                        player.wraithsTicksFired = 0;
                        Animation<TextureRegion> wraithsAnim = player.hasVoidHeart ? Assets.getVoidWraithsVfx() : Assets.getWraithsVfx();
                        eventCallback.spawnStaticVfx(wraithsAnim, center.x, center.y, 0, Constants.Knight.HEIGHT_HALVED_SCALED * 3f, player.facingRight, true);
                        System.out.println("Howling Wraiths Cast!");
                    } else {
                        player.spritCastTimer = Constants.Knight.VengefulSpirit.DURATION;
                        new VengefulSpirit(player.world, center.x, center.y, player.facingRight, inventory.isEquipped(CharmType.VOID_HEART));
                        Animation<TextureRegion> soulAnim = player.hasVoidHeart ? Assets.getVoidSpiritCastVfx() : Assets.getSpiritCastVfx();
                        eventCallback.spawnStaticVfx(soulAnim, center.x, center.y, Constants.Knight.WIDTH_HALVED_SCALED, 0, player.facingRight, true);
                        System.out.println("Vengeful Spirit Cast!");
                    }
                    return; // Exit out, spell successfully cast
                } else {
                    System.out.println("Not enough Soul for spells! Need " + (Constants.Knight.FOCUS_COST - player.soul) + " more.");
                }
            }
        }

        // --- FOCUS (HEALING) STATE MACHINE ---
        float focusTargetTime = inventory.isEquipped(CharmType.QUICK_FOCUS)
            ? Constants.Knight.FOCUS_DURATION * 0.6f // 40% faster
            : Constants.Knight.FOCUS_DURATION;

        // Must be holding the key, grounded, NOT holding a spell direction, and have enough soul
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

    private void executeDashDamage() {
        Vector2 center = player.b2body.getWorldCenter();

        // Create a hitbox slightly wider than the Knight
        float reachX = Constants.Knight.WIDTH_HALVED_SCALED * 1.2f;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED;

        QueryCallback dashDamageCallback = fixture -> {
            Object userData = fixture.getUserData();

            if (userData instanceof IDamageable enemy) {
                // Only damage them if they are alive AND haven't been hit this dash
                if (!enemy.isDead() && !enemiesHitDuringDash.contains(enemy, true)) {
                    // Sharp Shadow deals exactly 1 damage (standard nail damage)
                    enemy.applyKnockback(0, 2f);
                    enemy.takeDamage(1);
                    eventCallback.spawnStaticVfx(damageAnimation, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);

                    // Add them to the list so they don't get hit on the next frame
                    enemiesHitDuringDash.add(enemy);
                }
            }
            return true;
        };

        // Query the physics world around the Knight
        player.world.QueryAABB(
            dashDamageCallback,
            center.x - reachX,
            center.y - heightY,
            center.x + reachX,
            center.y + heightY
        );
    }

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
                // Ignore corpses AND enemies we already hit this swing
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
                    eventCallback.spawnStaticVfx(damageAnimation, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);

                    if (!(enemy instanceof Zote)) {
                        int soulGain = inventory.isEquipped(CharmType.SOUL_CATCHER)
                            ? Constants.Knight.SOUL_PER_HIT + 5
                            : Constants.Knight.SOUL_PER_HIT;
                        player.addSoul(soulGain);
                    }

                    enemiesHitDuringAttack.add(enemy); // Add to exclusion list
                }
                return fraction;
            }
            if ("ground".equals(userData)) return fraction;
            return -1;
        };
        player.world.rayCast(pogoCallback, center, rayEnd);
    }

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
                // Ignore corpses AND enemies already hit this swing
                if (!enemy.isDead() && !enemiesHitDuringAttack.contains(enemy, true)) {

                    int damage = inventory.isEquipped(CharmType.UNBREAKABLE_STRENGTH) ? 2 : 1;
                    float knockbackMulti = inventory.isEquipped(CharmType.HEAVY_BLOW) ? 2f : 1f;

                    enemy.applyKnockback(direction * 3f * knockbackMulti, 1f);
                    enemy.takeDamage(damage);
                    eventCallback.spawnStaticVfx(damageAnimation, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);

                    if (!(enemy instanceof Zote)) {
                        int soulGain = inventory.isEquipped(CharmType.SOUL_CATCHER)
                            ? Constants.Knight.SOUL_PER_HIT + 5
                            : Constants.Knight.SOUL_PER_HIT;
                        player.addSoul(soulGain);
                    }

                    enemiesHitDuringAttack.add(enemy); // Add to exclusion list
                }
            }
            return true;
        };

        player.world.QueryAABB(attackCallback, lowerX, lowerY, upperX, upperY);
        // DELETED: The entire hitSomething[0] block that killed the timer early.
    }

    private void executeWraithsHit() {
        Vector2 center = player.b2body.getWorldCenter();

        float width = Constants.Knight.HowlingWraiths.WIDTH;
        float heightY = Constants.Knight.HEIGHT_HALVED_SCALED;

        float lowerX = center.x - width;
        float upperX = center.x + width;
        float lowerY = center.y + heightY; // Starts at the top of the Knight's head
        float upperY = center.y + heightY + Constants.Knight.HowlingWraiths.HEIGHT;

        QueryCallback wraithsCallback = fixture -> {
            Object userData = fixture.getUserData();

            if (userData instanceof IDamageable enemy) {
                if (!enemy.isDead()) {
                    // Apply Void Heart modifier
                    int spellDamage = inventory.isEquipped(CharmType.VOID_HEART) ? 2 : 1;
                    if (player.wraithsTicksFired == 2 || (enemy instanceof Enemy && ((Enemy) enemy).getHp() == 1)) {
                        enemy.applyKnockback(0, 2f);
                    }
                    enemy.takeDamage(spellDamage);
                    eventCallback.spawnStaticVfx(damageAnimation, fixture.getBody().getPosition().x, fixture.getBody().getPosition().y, 0, 0, player.facingRight, true);
                }
            }
            return true;
        };

        player.world.QueryAABB(wraithsCallback, lowerX, lowerY, upperX, upperY);
    }
}
