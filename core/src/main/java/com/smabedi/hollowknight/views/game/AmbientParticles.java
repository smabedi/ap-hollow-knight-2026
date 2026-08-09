package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Procedural particle system generating environmental atmospheric effects.
 * Dynamically adjusts density, color, and trajectory based on the active LocationType.
 */
public class AmbientParticles implements Disposable {
    private final Texture particleTexture;
    private final Array<Particle> particles = new Array<>();
    private final LocationType location;
    private final int maxParticles;

    /**
     * Constructs the particle system and procedurally bakes the particle texture
     * utilizing an RGBA radial gradient to ensure soft blending.
     *
     * @param location The current game location dictating particle behavior and density.
     */
    public AmbientParticles(LocationType location) {
        this.location = location;

        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        float radius = 8f;
        float coreRadius = 4.5f;
        float centerX = 8f;
        float centerY = 8f;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                float dx = (x + 0.5f) - centerX;
                float dy = (y + 0.5f) - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance <= radius) {
                    float alpha = 1f;

                    if (distance > coreRadius) {
                        alpha = 1f - ((distance - coreRadius) / (radius - coreRadius));
                        alpha = (float) Math.pow(alpha, 1.2f);
                    }

                    // Utilize Pixmap's native setColor to correctly process RGBA channels
                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        particleTexture = new Texture(pixmap);
        particleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        if (location == LocationType.GREENPATH) {
            maxParticles = 80;
        } else {
            maxParticles = 50;
        }
    }

    /**
     * Evaluates particle lifespans, calculates kinetic vectors, and renders the active pool.
     */
    public void updateAndRender(float dt, OrthographicCamera camera, SpriteBatch batch) {
        float viewWidth = camera.viewportWidth;
        float viewHeight = camera.viewportHeight;
        float camX = camera.position.x;
        float camY = camera.position.y;

        while (particles.size < maxParticles) {
            Particle p = new Particle();
            p.x = camX + MathUtils.random(-viewWidth, viewWidth);
            p.y = camY + MathUtils.random(-viewHeight, viewHeight);
            p.maxLife = MathUtils.random(3f, 8f);
            p.life = p.maxLife;

            if (location == LocationType.GREENPATH) {
                p.size = MathUtils.random(0.05f, 0.15f);
                p.vx = MathUtils.random(-0.5f, 0.5f);
                p.vy = MathUtils.random(-1.5f, -0.5f);
                p.swayPhase = MathUtils.random(0, MathUtils.PI2);
                p.swaySpeed = MathUtils.random(1f, 3f);
                p.color = new Color(MathUtils.random(0.4f, 0.6f), MathUtils.random(0.8f, 1f), MathUtils.random(0.2f, 0.4f), 0f);
            } else {
                p.size = MathUtils.random(0.03f, 0.08f);
                p.vx = MathUtils.random(-0.2f, 0.2f);
                p.vy = MathUtils.random(0.2f, 0.8f);
                p.swayPhase = MathUtils.random(0, MathUtils.PI2);
                p.swaySpeed = MathUtils.random(0.5f, 1.5f);
                p.color = new Color(0.6f, 0.6f, 0.7f, 0f);
            }
            particles.add(p);
        }

        batch.begin();
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= dt;

            if (p.life <= 0) {
                particles.removeIndex(i);
                continue;
            }

            float currentVx = p.vx;
            if (location == LocationType.GREENPATH) {
                currentVx += MathUtils.sin(p.life * p.swaySpeed + p.swayPhase) * 1.5f;
            } else {
                currentVx += MathUtils.sin(p.life * p.swaySpeed + p.swayPhase) * 0.5f;
            }

            p.x += currentVx * dt;
            p.y += p.vy * dt;

            float alpha = 1f;
            if (p.maxLife - p.life < 1f) {
                alpha = p.maxLife - p.life;
            } else if (p.life < 1f) {
                alpha = p.life;
            }
            p.color.a = alpha * 0.7f;

            // Culling bounds to ensure particles wrap seamlessly around the viewport
            if (p.x > camX + viewWidth) p.x = camX - viewWidth;
            if (p.x < camX - viewWidth) p.x = camX + viewWidth;
            if (p.y > camY + viewHeight) p.y = camY - viewHeight;
            if (p.y < camY - viewHeight) p.y = camY + viewHeight;

            batch.setColor(p.color);
            batch.draw(particleTexture, p.x, p.y, p.size, p.size);
        }
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose() {
        particleTexture.dispose();
    }

    private static class Particle {
        float x, y, size, life, maxLife;
        float vx, vy, swayPhase, swaySpeed;
        Color color;
    }
}
