package com.smabedi.hollowknight.views.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class AmbientParticles implements Disposable {
    private final Texture particleTexture;
    private final Array<Particle> particles = new Array<>();
    private final LocationType location;
    private final int maxParticles;


    public AmbientParticles(LocationType location) {
        this.location = location;


        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        float radius = 8f;
        float coreRadius = 4.5f;
        float centerX = 8f;
        float centerY = 8f;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                // Add 0.5f to target the exact center of the pixel
                float dx = (x + 0.5f) - centerX;
                float dy = (y + 0.5f) - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance <= radius) {
                    float alpha = 1f;

                    // Only apply the fade if we are outside the solid core
                    if (distance > coreRadius) {
                        // Linear fade from the core's edge to the outer radius
                        alpha = 1f - ((distance - coreRadius) / (radius - coreRadius));

                        // Slightly curve the fade so the very edge is softer
                        alpha = (float) Math.pow(alpha, 1.2f);
                    }

                    // THE FIX: Use Pixmap's built-in setColor which correctly handles RGBA
                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }

        particleTexture = new Texture(pixmap);
        particleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        // Configure density based on the environment
        if (location == LocationType.GREENPATH) {
            maxParticles = 80; // Dense leaves/spores
        } else {
            maxParticles = 50; // Sparse dust
        }
    }

    public void updateAndRender(float dt, OrthographicCamera camera, SpriteBatch batch) {
        float viewWidth = camera.viewportWidth;
        float viewHeight = camera.viewportHeight;
        float camX = camera.position.x;
        float camY = camera.position.y;

        // 1. Spawn new particles if we are below the max limit
        while (particles.size < maxParticles) {
            Particle p = new Particle();
            // Spawn randomly inside and slightly outside the camera view
            p.x = camX + MathUtils.random(-viewWidth, viewWidth);
            p.y = camY + MathUtils.random(-viewHeight, viewHeight);
            p.maxLife = MathUtils.random(3f, 8f);
            p.life = p.maxLife;

            if (location == LocationType.GREENPATH) {
                // Greenpath: Falling green leaves/spores
                p.size = MathUtils.random(0.05f, 0.15f); // Scaled for Box2D meters
                p.vx = MathUtils.random(-0.5f, 0.5f);
                p.vy = MathUtils.random(-1.5f, -0.5f); // Falling down
                p.swayPhase = MathUtils.random(0, MathUtils.PI2);
                p.swaySpeed = MathUtils.random(1f, 3f);
                // Mix of light green and yellowish green
                p.color = new Color(MathUtils.random(0.4f, 0.6f), MathUtils.random(0.8f, 1f), MathUtils.random(0.2f, 0.4f), 0f);
            } else {
                // Forgotten Crossroads: Floating grayish/blueish dust
                p.size = MathUtils.random(0.03f, 0.08f);
                p.vx = MathUtils.random(-0.2f, 0.2f);
                p.vy = MathUtils.random(0.2f, 0.8f); // Drifting slowly UP
                p.swayPhase = MathUtils.random(0, MathUtils.PI2);
                p.swaySpeed = MathUtils.random(0.5f, 1.5f);
                // Pale grey/blue
                p.color = new Color(0.6f, 0.6f, 0.7f, 0f);
            }
            particles.add(p);
        }

        // 2. Update and Draw
        batch.begin();
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= dt;

            if (p.life <= 0) {
                particles.removeIndex(i);
                continue;
            }

            // Move particle
            float currentVx = p.vx;
            if (location == LocationType.GREENPATH) {
                // Add a sine wave sway to falling leaves
                currentVx += MathUtils.sin(p.life * p.swaySpeed + p.swayPhase) * 1.5f;
            } else {
                // Gentle drift for dust
                currentVx += MathUtils.sin(p.life * p.swaySpeed + p.swayPhase) * 0.5f;
            }

            p.x += currentVx * dt;
            p.y += p.vy * dt;

            // Fade in and out based on life
            float alpha = 1f;
            if (p.maxLife - p.life < 1f) {
                alpha = p.maxLife - p.life; // Fade in
            } else if (p.life < 1f) {
                alpha = p.life; // Fade out
            }
            p.color.a = alpha * 0.7f; // Max opacity 70%

            // Wrap around the screen so they don't disappear forever if they float too far
            if (p.x > camX + viewWidth) p.x = camX - viewWidth;
            if (p.x < camX - viewWidth) p.x = camX + viewWidth;
            if (p.y > camY + viewHeight) p.y = camY - viewHeight;
            if (p.y < camY - viewHeight) p.y = camY + viewHeight;

            // Render
            batch.setColor(p.color);
            batch.draw(particleTexture, p.x, p.y, p.size, p.size);
        }
        batch.setColor(Color.WHITE); // Reset batch color
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
