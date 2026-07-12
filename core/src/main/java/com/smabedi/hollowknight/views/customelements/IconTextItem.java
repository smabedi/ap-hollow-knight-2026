package com.smabedi.hollowknight.views.customelements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class IconTextItem extends Table {

    public IconTextItem(TextureRegion icon, String title, String description, Skin skin, boolean verticalLayout, boolean isDisabled) {
        Image imageItem = new Image(icon);

        if (verticalLayout) {
            // CHARMS LAYOUT: Icon top, Text bottom
            add(imageItem).size(64, 64).padBottom(10).row(); // Adjust size as needed

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
            // ACHIEVEMENTS LAYOUT: Icon left, Titles right
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
                textTable.add(descLabel).width(400).left(); // Wraps long achievement descriptions
            }

            add(textTable).left().expandX();
        }
    }

    public IconTextItem(TextureRegion icon, String title, String description, Skin skin, boolean verticalLayout) {
        this(icon, title, description, skin, verticalLayout, false);
    }
}
