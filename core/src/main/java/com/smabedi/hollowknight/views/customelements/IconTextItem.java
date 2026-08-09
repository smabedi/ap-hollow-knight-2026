package com.smabedi.hollowknight.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

/**
 * A custom Scene2D Table widget designed to display a unified icon and text grouping.
 * Supports dynamic orientations for distinct UI contexts (e.g., vertical for Inventory Charms,
 * horizontal for Achievements lists).
 */
public class IconTextItem extends Table {

    /**
     * Constructs a unified icon and text UI element.
     *
     * @param icon           The visual TextureRegion to display.
     * @param title          The primary label text.
     * @param description    The secondary label text (optional, used in horizontal layouts).
     * @param skin           The active UI Skin for font and styling data.
     * @param verticalLayout If true, stacks text below the icon. If false, aligns text to the right.
     * @param isDisabled     If true, applies a grayscale tint to indicate locked or inactive states.
     */
    public IconTextItem(TextureRegion icon, String title, String description, Skin skin, boolean verticalLayout, boolean isDisabled) {
        Image imageItem = new Image(icon);

        if (verticalLayout) {
            add(imageItem).size(64, 64).padBottom(10).row();

            Label titleLabel = new Label(title, skin, "small");
            if (isDisabled) {
                titleLabel.setColor(Color.GRAY);
            } else {
                titleLabel.setColor(Color.GOLD);
            }
            titleLabel.setAlignment(Align.center);
            titleLabel.setWrap(true);
            add(titleLabel).width(120).center();

        } else {
            add(imageItem).size(64, 64).padRight(20);

            Table textTable = new Table();
            Label titleLabel = new Label(title, skin);

            if (isDisabled) {
                titleLabel.setColor(Color.GRAY);
            } else {
                titleLabel.setColor(Color.GOLD);
            }

            textTable.add(titleLabel).left().padBottom(5).row();

            if (description != null) {
                Label descLabel = new Label(description, skin);
                descLabel.setWrap(true);
                textTable.add(descLabel).width(400).left();
            }

            add(textTable).left().expandX();
        }
    }

    public IconTextItem(TextureRegion icon, String title, String description, Skin skin, boolean verticalLayout) {
        this(icon, title, description, skin, verticalLayout, false);
    }
}
